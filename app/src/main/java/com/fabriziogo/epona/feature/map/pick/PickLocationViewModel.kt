package com.fabriziogo.epona.feature.map.pick

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import com.fabriziogo.epona.core.domain.usecase.location.GetCurrentLocationUseCase
import com.fabriziogo.epona.feature.map.navigation.PICK_LAT_ARG
import com.fabriziogo.epona.feature.map.navigation.PICK_LNG_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PickLocationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val locationRepo: LocationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PickLocationUiState())
    val state: StateFlow<PickLocationUiState> = _state.asStateFlow()

    private val _navEvents = Channel<PickLocationNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    private var geocodeJob: Job? = null

    init {
        val lat = savedStateHandle.get<String>(PICK_LAT_ARG)?.toDoubleOrNull()
        val lng = savedStateHandle.get<String>(PICK_LNG_ARG)?.toDoubleOrNull()
        if (lat != null && lng != null) {
            _state.update {
                it.copy(
                    isResolvingCamera = false,
                    cameraLat = lat,
                    cameraLng = lng
                )
            }
        } else {
            resolveInitialCamera()
        }
    }

    fun onEvent(event: PickLocationEvent) {
        when (event) {
            is PickLocationEvent.CameraSettled -> onCameraSettled(event.latitude, event.longitude)
            PickLocationEvent.UseCurrentLocation -> locate()
            PickLocationEvent.ConfirmClicked -> confirm()
            PickLocationEvent.BackClicked -> emitNav(PickLocationNavEvent.Back)
            PickLocationEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun resolveInitialCamera() {
        viewModelScope.launch {
            getCurrentLocation()
                .onSuccess { loc ->
                    _state.update {
                        it.copy(
                            isResolvingCamera = false,
                            cameraLat = loc.latitude,
                            cameraLng = loc.longitude
                        )
                    }
                }
                .onFailure {
                    // The user can still pan manually, so fall back silently.
                    _state.update { it.copy(isResolvingCamera = false) }
                }
        }
    }

    private fun onCameraSettled(latitude: Double, longitude: Double) {
        geocodeJob?.cancel()
        geocodeJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    target = Location(latitude, longitude),
                    address = "",
                    isGeocoding = true
                )
            }
            // Debounce: the camera settles after every flick and Geocoder is
            // rate-limited — without this a slow lookup can land after the user
            // moved on and mislabel the pin. Cancellation above drops the stale one.
            delay(400)
            locationRepo.reverseGeocode(latitude, longitude)
                .onSuccess { addr ->
                    _state.update { it.copy(address = addr, isGeocoding = false) }
                }
                .onFailure {
                    _state.update { it.copy(address = "", isGeocoding = false) }
                }
        }
    }

    private fun locate() {
        viewModelScope.launch {
            _state.update { it.copy(isLocating = true, error = null) }
            getCurrentLocation()
                .onSuccess { loc ->
                    _state.update {
                        it.copy(
                            isLocating = false,
                            cameraLat = loc.latitude,
                            cameraLng = loc.longitude
                        )
                    }
                    // The camera settle re-enters CameraSettled, so the geocode
                    // happens through the one path.
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLocating = false,
                            error = "Could not get location"
                        )
                    }
                }
        }
    }

    private fun confirm() {
        val t = state.value.target ?: return
        // ifBlank -> null is load-bearing: the consuming ViewModels re-geocode only when
        // address == null. Sending "" would strand the card on its placeholder forever.
        emitNav(PickLocationNavEvent.Confirmed(t.copy(address = state.value.address.ifBlank { null })))
    }

    private fun emitNav(event: PickLocationNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }
}
