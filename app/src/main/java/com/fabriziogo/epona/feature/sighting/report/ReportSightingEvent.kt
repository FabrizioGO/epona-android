package com.fabriziogo.epona.feature.sighting.report

import android.net.Uri
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.media.PhotoItem

sealed interface ReportSightingEvent {
    data class PhotosPicked(val uris: List<Uri>) : ReportSightingEvent
    data class PhotoRemoved(val photo: PhotoItem) : ReportSightingEvent
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