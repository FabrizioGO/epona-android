package com.fabriziogo.epona.feature.sighting.report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Sighting
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import com.fabriziogo.epona.core.domain.usecase.alert.GetAlertDetailUseCase
import com.fabriziogo.epona.core.domain.usecase.sighting.ReportSightingUseCase
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
    private val locationRepo: LocationRepository
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
            is ReportSightingEvent.PhotoAdded -> addPhoto(event.uri)
            is ReportSightingEvent.PhotoRemoved -> removePhoto(event.index)
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

    private fun addPhoto(uri: String) {
        if (_state.value.photoUris.size >= 3) {
            _state.update { it.copy(error = "Maximum 3 photos allowed") }
            return
        }
        _state.update { it.copy(photoUris = it.photoUris + uri) }
    }

    private fun removePhoto(index: Int) {
        _state.update {
            it.copy(photoUris = it.photoUris.toMutableList().apply { removeAt(index) })
        }
    }

    private fun submit() {
        val s = _state.value
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
                photoUrls = s.photoUris,  // Will be uploaded by repository
                note = s.note.ifBlank { null }
            )

            reportSighting(sighting)
                .onSuccess {
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
}