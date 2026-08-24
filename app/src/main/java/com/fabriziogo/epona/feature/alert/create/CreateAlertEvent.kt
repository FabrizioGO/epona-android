package com.fabriziogo.epona.feature.alert.create

import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet


sealed interface CreateAlertEvent {
    data object NextStep : CreateAlertEvent
    data object PreviousStep : CreateAlertEvent
    data object CloseClicked : CreateAlertEvent

    data class AlertTypeSelected(val type: AlertType) : CreateAlertEvent

    data class PetSelected(val pet: Pet) : CreateAlertEvent
    data object AddNewPetClicked : CreateAlertEvent

    data class PhotoAdded(val uri: String) : CreateAlertEvent
    data class PhotoRemoved(val index: Int) : CreateAlertEvent
    data object UseCurrentLocation : CreateAlertEvent
    data object LocationPermissionDenied : CreateAlertEvent
    data class LocationPicked(val location: Location) : CreateAlertEvent
    data class DescriptionChanged(val text: String) : CreateAlertEvent

    data class PhoneChanged(val phone: String) : CreateAlertEvent
    data class RewardChanged(val reward: String) : CreateAlertEvent
    data object PublishClicked : CreateAlertEvent

    data object ErrorDismissed : CreateAlertEvent
}

sealed interface CreateAlertNavEvent {
    data object NavigateBack : CreateAlertNavEvent
    data object NavigateToAddPet : CreateAlertNavEvent
    data class NavigateToSuccess(val alertId: String) : CreateAlertNavEvent
}