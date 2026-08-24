package com.fabriziogo.epona.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.usecase.alert.GetNearbyAlertsUseCase
import com.fabriziogo.epona.core.domain.usecase.alert.ObserveNearbyAlertsUseCase
import com.fabriziogo.epona.core.domain.usecase.auth.GetCurrentUserUseCase
import com.fabriziogo.epona.core.domain.usecase.location.GetCurrentLocationUseCase
import com.fabriziogo.epona.core.domain.usecase.notification.ObserveUnreadCountUseCase
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
class HomeViewModel @Inject constructor(
    private val getNearbyAlerts: GetNearbyAlertsUseCase,
    private val observeNearbyAlerts: ObserveNearbyAlertsUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val observeUnreadCount: ObserveUnreadCountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _navEvents = Channel<HomeNavigationEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init {
        loadUserProfile()
        observeUnreadNotifications()
        loadInitialData()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.FilterChanged -> applyFilter(event.filter)
            is HomeEvent.Refresh -> refreshAlerts()
            is HomeEvent.RetryLoad -> loadInitialData()
            is HomeEvent.SearchClicked -> emitNav(HomeNavigationEvent.NavigateToSearch)
            is HomeEvent.NotificationsClicked -> emitNav(HomeNavigationEvent.NavigateToNotifications)
            is HomeEvent.CreateAlertClicked -> emitNav(HomeNavigationEvent.NavigateToCreateAlert)
            is HomeEvent.ViewMapClicked -> emitNav(HomeNavigationEvent.NavigateToMap)
            is HomeEvent.AlertClicked -> emitNav(HomeNavigationEvent.NavigateToDetail(event.alertId))
            is HomeEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            getCurrentUser().onSuccess { user ->
                _state.update {
                    it.copy(
                        userName = user.displayName,
                        userAvatar = user.avatarUrl,
                        userLocation = user.location,
                        alertRadiusKm = user.alertRadiusKm
                    )
                }
            }
        }
    }

    private fun observeUnreadNotifications() {
        viewModelScope.launch {
            observeUnreadCount().collect { count ->
                _state.update { it.copy(unreadNotificationCount = count) }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            // Get user location first
            val location = resolveLocation()
            _state.update { it.copy(userLocation = location) }

            // Fetch nearby alerts
            fetchAlerts(location)

            // Start observing realtime updates
            observeRealtimeAlerts(location)
        }
    }

    private fun refreshAlerts() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val location = _state.value.userLocation ?: resolveLocation()
            fetchAlerts(location)
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private suspend fun resolveLocation(): Location {
        return getCurrentLocation().getOrElse {
            // Default fallback location — will be replaced by user's saved location
            _state.value.userLocation ?: Location(40.7128, -74.0060)
        }
    }

    private suspend fun fetchAlerts(location: Location) {
        val radiusMeters = _state.value.alertRadiusKm * 1000
        val filter = _state.value.selectedFilter

        getNearbyAlerts(
            latitude = location.latitude,
            longitude = location.longitude,
            radiusMeters = radiusMeters,
            type = filter.toAlertType()
        ).onSuccess { alerts ->
            val lostCount = alerts.count { it.alert.type == AlertType.LOST }
            val foundCount = alerts.count { it.alert.type == AlertType.FOUND }

            _state.update {
                it.copy(
                    isLoading = false,
                    alerts = alerts,
                    filteredAlerts = applyFilterToList(alerts, filter),
                    totalActiveAlerts = alerts.size,
                    lostCount = lostCount,
                    foundCount = foundCount,
                    error = null
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

    private fun observeRealtimeAlerts(location: Location) {
        val radiusMeters = _state.value.alertRadiusKm * 1000
        viewModelScope.launch {
            observeNearbyAlerts(
                latitude = location.latitude,
                longitude = location.longitude,
                radiusMeters = radiusMeters
            ).collect { alerts ->
                val filter = _state.value.selectedFilter
                val lostCount = alerts.count { it.alert.type == AlertType.LOST }
                val foundCount = alerts.count { it.alert.type == AlertType.FOUND }

                _state.update {
                    it.copy(
                        alerts = alerts,
                        filteredAlerts = applyFilterToList(alerts, filter),
                        totalActiveAlerts = alerts.size,
                        lostCount = lostCount,
                        foundCount = foundCount
                    )
                }
            }
        }
    }

    private fun applyFilter(filter: AlertFilter) {
        _state.update {
            it.copy(
                selectedFilter = filter,
                filteredAlerts = applyFilterToList(it.alerts, filter)
            )
        }
    }

    private fun applyFilterToList(
        alerts: List<AlertWithDetails>,
        filter: AlertFilter
    ) = when (filter) {
        AlertFilter.ALL -> alerts
        AlertFilter.LOST -> alerts.filter { it.alert.type == AlertType.LOST }
        AlertFilter.FOUND -> alerts.filter { it.alert.type == AlertType.FOUND }
    }

    private fun emitNav(event: HomeNavigationEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }
}

