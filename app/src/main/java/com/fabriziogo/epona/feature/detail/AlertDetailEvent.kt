package com.fabriziogo.epona.feature.detail

import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.ReportReason

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
    data object FlagClicked : AlertDetailEvent
    data class FlagSubmitted(val reason: ReportReason, val details: String?) : AlertDetailEvent
    data object FlagDismissed : AlertDetailEvent
    data object FlagSuccessMessageShown : AlertDetailEvent
    data object RetryLoad : AlertDetailEvent
    data object ErrorDismissed : AlertDetailEvent
}

sealed interface DetailNavEvent {
    data object NavigateBack : DetailNavEvent
    data class NavigateToReportSighting(val alertId: String) : DetailNavEvent
    data class NavigateToMap(val lat: Double, val lng: Double) : DetailNavEvent
    data class ShareAlert(val detail: AlertWithDetails, val url: String) : DetailNavEvent
    data class DialPhone(val phone: String) : DetailNavEvent
}
