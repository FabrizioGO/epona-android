package com.fabriziogo.epona.feature.alert.create

import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet


data class CreateAlertUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 4,

    // Step 1
    val alertType: AlertType = AlertType.LOST,

    // Step 2
    val myPets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val isLoadingPets: Boolean = true,

    // Step 3
    val photoUris: List<String> = emptyList(),
    val location: Location? = null,
    val address: String = "",
    val description: String = "",
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null,

    // Step 4
    val contactPhone: String = "",
    val reward: String = "",

    // Submission
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val canProceed: Boolean
        get() = when (currentStep) {
            1 -> true
            2 -> selectedPet != null
            3 -> location != null
            4 -> true
            else -> false
        }

    val progress: Float
        get() = currentStep.toFloat() / totalSteps.toFloat()
}