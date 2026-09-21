package com.fabriziogo.epona.feature.map.pick

import com.fabriziogo.epona.core.domain.model.Location

sealed interface PickLocationEvent {
    data class CameraSettled(val latitude: Double, val longitude: Double) : PickLocationEvent
    data object UseCurrentLocation : PickLocationEvent
    data object ConfirmClicked : PickLocationEvent
    data object BackClicked : PickLocationEvent
    data object ErrorDismissed : PickLocationEvent
}

sealed interface PickLocationNavEvent {
    data class Confirmed(val location: Location) : PickLocationNavEvent
    data object Back : PickLocationNavEvent
}
