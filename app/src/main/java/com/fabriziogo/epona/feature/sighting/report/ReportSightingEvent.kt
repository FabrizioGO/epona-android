package com.fabriziogo.epona.feature.sighting.report

import com.fabriziogo.epona.core.domain.model.Location

sealed interface ReportSightingEvent {
    data class PhotoAdded(val uri: String) : ReportSightingEvent
    data class PhotoRemoved(val index: Int) : ReportSightingEvent
    data object UseCurrentLocation : ReportSightingEvent
    data object LocationPermissionDenied : ReportSightingEvent
    data class LocationPicked(val location: Location) : ReportSightingEvent
    data class NoteChanged(val note: String) : ReportSightingEvent
    data object SubmitClicked : ReportSightingEvent
    data object BackClicked : ReportSightingEvent
    data object ErrorDismissed : ReportSightingEvent
}

sealed interface SightingNavEvent {
    data object NavigateBack : SightingNavEvent
    data object NavigateBackWithSuccess : SightingNavEvent
}