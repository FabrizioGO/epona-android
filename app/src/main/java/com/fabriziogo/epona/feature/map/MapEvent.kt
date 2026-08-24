package com.fabriziogo.epona.feature.map

import com.fabriziogo.epona.core.domain.model.AlertType

sealed interface MapEvent {
    data class MarkerClicked(val alertId: String) : MapEvent
    data object SheetDismissed : MapEvent
    data object ViewDetailClicked : MapEvent
    data class FilterToggled(val type: AlertType) : MapEvent
    data object ToggleRadiusCircle : MapEvent
    data object RecenterClicked : MapEvent
    data object LocationPermissionGranted : MapEvent
    data object CreateAlertClicked : MapEvent
    data object RetryLoad : MapEvent
    data object ErrorDismissed : MapEvent
}

sealed interface MapNavEvent {
    data class NavigateToDetail(val alertId: String) : MapNavEvent
    data object NavigateToCreateAlert : MapNavEvent
}