package com.fabriziogo.epona.feature.profile.addeditpet

import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species

sealed interface AddEditPetEvent {
    data class NameChanged(val value: String) : AddEditPetEvent
    data class SpeciesSelected(val value: Species) : AddEditPetEvent
    data class BreedChanged(val value: String) : AddEditPetEvent
    data class ColorChanged(val value: String) : AddEditPetEvent
    data class SizeSelected(val value: PetSize) : AddEditPetEvent
    data class AgeChanged(val value: String) : AddEditPetEvent
    data class GenderSelected(val value: PetGender) : AddEditPetEvent
    data class MicrochipChanged(val value: String) : AddEditPetEvent
    data class DescriptionChanged(val value: String) : AddEditPetEvent
    data class PhotoAdded(val uri: String) : AddEditPetEvent
    data object PhotoRemoved : AddEditPetEvent
    data object SaveClicked : AddEditPetEvent
    data object BackClicked : AddEditPetEvent
    data object ErrorDismissed : AddEditPetEvent
}

sealed interface AddEditPetNavEvent {
    data object NavigateBack : AddEditPetNavEvent
    data object NavigateBackWithSuccess : AddEditPetNavEvent
}
