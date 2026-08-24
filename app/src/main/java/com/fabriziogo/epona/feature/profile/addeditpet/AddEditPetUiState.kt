package com.fabriziogo.epona.feature.profile.addeditpet

import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species

data class AddEditPetUiState(
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val name: String = "",
    val species: Species = Species.DOG,
    val breed: String = "",
    val color: String = "",
    val size: PetSize = PetSize.MEDIUM,
    val ageYears: String = "",
    val gender: PetGender = PetGender.UNKNOWN,
    val microchipId: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)
