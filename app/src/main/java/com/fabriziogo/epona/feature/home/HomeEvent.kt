package com.fabriziogo.epona.feature.home

sealed interface HomeEvent {
    data class FilterChanged(val filter: AlertFilter) : HomeEvent
    data class SearchQueryChanged(val query: String) : HomeEvent
    data object Refresh : HomeEvent
    data object RetryLoad : HomeEvent
    data object SearchClicked : HomeEvent
    data object NotificationsClicked : HomeEvent
    data object CreateAlertClicked : HomeEvent
    data object ViewMapClicked : HomeEvent
    data class AlertClicked(val alertId: String) : HomeEvent
    data object ErrorDismissed : HomeEvent
}

sealed interface HomeNavigationEvent {
    data class NavigateToDetail(val alertId: String) : HomeNavigationEvent
    data object NavigateToSearch : HomeNavigationEvent
    data object NavigateToNotifications : HomeNavigationEvent
    data object NavigateToCreateAlert : HomeNavigationEvent
    data object NavigateToMap : HomeNavigationEvent
}