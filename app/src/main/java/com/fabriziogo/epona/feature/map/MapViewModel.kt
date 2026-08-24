package com.fabriziogo.epona.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.usecase.alert.GetNearbyAlertsUseCase
import com.fabriziogo.epona.core.domain.usecase.auth.GetCurrentUserUseCase
import com.fabriziogo.epona.core.domain.usecase.location.GetCurrentLocationUseCase
import com.fabriziogo.epona.core.domain.usecase.sighting.GetSightingTrailUseCase
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
class MapViewModel @Inject constructor(
    private val getNearbyAlerts: GetNearbyAlertsUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getSightingTrail: GetSightingTrailUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    private val _navEvents = Channel<MapNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init { loadMapData() }

    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.MarkerClicked -> selectAlert(event.alertId)
            MapEvent.SheetDismissed -> dismissSheet()
            MapEvent.ViewDetailClicked -> navigateToDetail()
            is MapEvent.FilterToggled -> toggleFilter(event.type)
            MapEvent.ToggleRadiusCircle -> toggleRadius()
            MapEvent.RecenterClicked -> recenter()
            MapEvent.LocationPermissionGranted -> onLocationPermissionGranted()
            MapEvent.CreateAlertClicked -> emitNav(MapNavEvent.NavigateToCreateAlert)
            MapEvent.RetryLoad -> loadMapData()
            MapEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadMapData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Load user info for radius
            getCurrentUser().onSuccess { user ->
                _state.update {
                    it.copy(
                        alertRadiusKm = user.alertRadiusKm,
                        userLocation = user.location
                    )
                }
            }

            // Get current location
            val location = getCurrentLocation().getOrElse {
                _state.value.userLocation ?: Location(40.7128, -74.0060)
            }

            _state.update {
                it.copy(
                    userLocation = location,
                    cameraLat = location.latitude,
                    cameraLng = location.longitude
                )
            }

            fetchAlertsAround(location)
        }
    }

    /**
     * The first load runs before the location permission has been answered, so it
     * falls back to the last known — or default — point. Once the user grants it,
     * redo just the part that needed the permission, without flipping isLoading and
     * tearing the map back down.
     */
    private fun onLocationPermissionGranted() {
        viewModelScope.launch {
            val location = getCurrentLocation().getOrNull() ?: return@launch
            _state.update {
                it.copy(
                    userLocation = location,
                    cameraLat = location.latitude,
                    cameraLng = location.longitude
                )
            }
            fetchAlertsAround(location)
        }
    }

    private suspend fun fetchAlertsAround(location: Location) {
        val radiusMeters = _state.value.alertRadiusKm * 1000
        getNearbyAlerts(
            latitude = location.latitude,
            longitude = location.longitude,
            radiusMeters = radiusMeters
        ).onSuccess { alerts ->
            _state.update {
                it.copy(
                    isLoading = false,
                    alerts = alerts,
                    filteredAlerts = applyFilters(alerts)
                )
            }
        }.onFailure { err ->
            _state.update {
                it.copy(
                    isLoading = false,
                    error = err.message ?: "Failed to load alerts"
                )
            }
        }
    }

    private fun selectAlert(alertId: String) {
        val alert = _state.value.alerts.find { it.alert.id == alertId }
        _state.update {
            it.copy(
                selectedAlert = alert,
                showSightingTrail = false,
                sightingTrail = emptyList()
            )
        }

        // Load sighting trail for selected alert
        alert?.let { loadSightingTrail(it.alert.id) }
    }

    private fun loadSightingTrail(alertId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isSightingsLoading = true) }
            getSightingTrail(alertId)
                .onSuccess { sightings ->
                    _state.update {
                        it.copy(
                            sightingTrail = sightings,
                            showSightingTrail = sightings.isNotEmpty(),
                            isSightingsLoading = false
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isSightingsLoading = false) }
                }
        }
    }

    private fun dismissSheet() {
        _state.update {
            it.copy(
                selectedAlert = null,
                showSightingTrail = false,
                sightingTrail = emptyList()
            )
        }
    }

    private fun navigateToDetail() {
        val alertId = _state.value.selectedAlert?.alert?.id ?: return
        emitNav(MapNavEvent.NavigateToDetail(alertId))
    }

    private fun toggleFilter(type: AlertType) {
        _state.update { s ->
            val newState = when (type) {
                AlertType.LOST -> s.copy(showLost = !s.showLost)
                AlertType.FOUND -> s.copy(showFound = !s.showFound)
            }
            newState.copy(filteredAlerts = applyFilters(newState.alerts, newState))
        }
    }

    private fun toggleRadius() {
        _state.update { it.copy(showRadiusCircle = !it.showRadiusCircle) }
    }

    private fun recenter() {
        val loc = _state.value.userLocation ?: return
        _state.update {
            it.copy(
                cameraLat = loc.latitude,
                cameraLng = loc.longitude,
                cameraZoom = 13f
            )
        }
    }

    private fun applyFilters(
        alerts: List<com.fabriziogo.epona.core.domain.model.AlertWithDetails>,
        s: MapUiState = _state.value
    ): List<com.fabriziogo.epona.core.domain.model.AlertWithDetails> = alerts.filter { a ->
        when (a.alert.type) {
            AlertType.LOST -> s.showLost
            AlertType.FOUND -> s.showFound
        }
    }

    private fun emitNav(event: MapNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }
}