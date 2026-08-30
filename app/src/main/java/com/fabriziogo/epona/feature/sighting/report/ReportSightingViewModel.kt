package com.fabriziogo.epona.feature.sighting.report

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.Sighting
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import com.fabriziogo.epona.core.domain.usecase.alert.GetAlertDetailUseCase
import com.fabriziogo.epona.core.domain.usecase.sighting.ReportSightingUseCase
import com.fabriziogo.epona.core.media.ImageProcessor
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.media.localUris
import com.fabriziogo.epona.feature.sighting.navigation.SIGHTING_ALERT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportSightingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reportSighting: ReportSightingUseCase,
    private val getAlertDetail: GetAlertDetailUseCase,
    private val locationRepo: LocationRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val alertId: String = checkNotNull(savedStateHandle[SIGHTING_ALERT_ID_ARG])

    private val _state = MutableStateFlow(ReportSightingUiState(alertId = alertId))
    val state: StateFlow<ReportSightingUiState> = _state.asStateFlow()

    private val _navEvents = Channel<SightingNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init {
        loadAlertInfo()
        autoDetectLocation()
    }

    fun onEvent(event: ReportSightingEvent) {
        when (event) {
            is ReportSightingEvent.PhotosPicked -> addPhotos(event.uris)
            is ReportSightingEvent.PhotoRemoved -> removePhoto(event.photo)
            ReportSightingEvent.UseCurrentLocation -> autoDetectLocation()
            ReportSightingEvent.LocationPermissionDenied -> _state.update {
                it.copy(
                    isLoadingLocation = false,
                    locationError = "Location permission denied — set the location " +
                            "manually, or enable it in Settings."
                )
            }
            is ReportSightingEvent.LocationPicked -> setLocation(event.location)
            is ReportSightingEvent.NoteChanged -> _state.update { it.copy(note = event.note) }
            ReportSightingEvent.SubmitClicked -> submit()
            ReportSightingEvent.BackClicked -> emitNav(SightingNavEvent.NavigateBack)
            ReportSightingEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadAlertInfo() {
        viewModelScope.launch {
            getAlertDetail(alertId).onSuccess { detail ->
                _state.update { it.copy(petName = detail.pet.name) }
            }
        }
    }

    private fun autoDetectLocation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLocation = true, locationError = null) }

            locationRepo.getCurrentLocation()
                .onSuccess { loc ->
                    _state.update {
                        it.copy(
                            location = loc,
                            isLoadingLocation = false
                        )
                    }
                    // Reverse geocode
                    locationRepo.reverseGeocode(loc.latitude, loc.longitude)
                        .onSuccess { addr ->
                            _state.update { it.copy(address = addr) }
                        }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoadingLocation = false,
                            locationError = "Could not get location. Please set it manually."
                        )
                    }
                }
        }
    }

    private fun setLocation(location: Location) {
        _state.update {
            it.copy(
                location = location,
                address = location.address ?: it.address,
                locationError = null
            )
        }
        // Reverse geocode if no address provided
        if (location.address == null) {
            viewModelScope.launch {
                locationRepo.reverseGeocode(location.latitude, location.longitude)
                    .onSuccess { addr ->
                        _state.update { it.copy(address = addr) }
                    }
            }
        }
    }

    /**
     * Decoding happens here rather than in the picker: it is disk work, and a scope
     * tied to the composition would drop the photos on a rotation mid-decode.
     */
    private fun addPhotos(uris: List<Uri>) {
        val free = MAX_PHOTOS_PER_ENTITY - _state.value.photos.size
        if (free <= 0 || uris.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isProcessingPhotos = true) }
            val wanted = uris.take(free)
            val added = wanted.mapNotNull { imageProcessor.process(it).getOrNull() }
            _state.update { s ->
                s.copy(
                    photos = s.photos + added.map { PhotoItem.Local(it) },
                    isProcessingPhotos = false,
                    // One unreadable pick should not sink the rest of the selection.
                    error = if (added.size < wanted.size) PHOTO_ERROR else s.error
                )
            }
        }
    }

    private fun removePhoto(photo: PhotoItem) {
        if (photo is PhotoItem.Local) imageProcessor.delete(listOf(photo.image.uri))
        _state.update { s -> s.copy(photos = s.photos.filterNot { it.key == photo.key }) }
    }

    private fun submit() {
        val s = _state.value

        // Submitting now carries up to three uploads, so the window a second tap can
        // land in went from milliseconds to seconds. canSubmit disables the button;
        // this covers everything that is not the button.
        if (s.isSubmitting) return

        val loc = s.location
        if (loc == null) {
            _state.update { it.copy(locationError = "Please set the sighting location") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            val sighting = Sighting(
                alertId = alertId,
                location = loc,
                address = s.address.ifBlank { null },
                // Local URIs. ReportSightingUseCase uploads them and substitutes the
                // public URLs before the create_sighting RPC.
                photoUrls = s.photos.map { it.model },
                note = s.note.ifBlank { null }
            )

            reportSighting(sighting)
                .onSuccess {
                    // Uploaded and persisted, so the cache copies are dead weight.
                    // Only on success: a failed submit keeps them for the retry.
                    imageProcessor.delete(s.photos.localUris())
                    _state.update { it.copy(isSubmitting = false, isSuccess = true) }
                    _navEvents.send(SightingNavEvent.NavigateBackWithSuccess)
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = err.message ?: "Failed to report sighting"
                        )
                    }
                }
        }
    }

    private fun emitNav(event: SightingNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }

    private companion object {
        const val PHOTO_ERROR = "Some photos could not be added"
    }
}