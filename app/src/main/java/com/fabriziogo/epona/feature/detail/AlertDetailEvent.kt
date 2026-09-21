package com.fabriziogo.epona.feature.detail

sealed interface AlertDetailEvent {
    data object BackClicked : AlertDetailEvent
    data object ShareClicked : AlertDetailEvent
    data object ContactOwnerClicked : AlertDetailEvent
    data object ReportSightingClicked : AlertDetailEvent
    data object ViewOnMapClicked : AlertDetailEvent
    data object ResolveClicked : AlertDetailEvent
    data object ResolveConfirmed : AlertDetailEvent
    data object ResolveDismissed : AlertDetailEvent
    data object DeleteClicked : AlertDetailEvent
    data object DeleteConfirmed : AlertDetailEvent
    data object DeleteDismissed : AlertDetailEvent
    data object RetryLoad : AlertDetailEvent
    data object ErrorDismissed : AlertDetailEvent
}

sealed interface DetailNavEvent {
    data object NavigateBack : DetailNavEvent
    data class NavigateToReportSighting(val alertId: String) : DetailNavEvent
    data class NavigateToMap(val lat: Double, val lng: Double) : DetailNavEvent
    data class ShareAlert(val text: String, val url: String) : DetailNavEvent
    data class DialPhone(val phone: String) : DetailNavEvent
}