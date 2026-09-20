package com.fabriziogo.epona.feature.alert.create

import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.media.PhotoItem


/**
 * The stray as the finder describes it. Deliberately no name, gender, age or
 * microchip — a finder cannot know those, and the server row stays ownerless.
 */
data class FoundPetForm(
    val species: Species = Species.DOG,
    val breed: String = "",
    val color: String = "",
    val size: PetSize = PetSize.MEDIUM,
    val description: String = "",
    val photos: List<PhotoItem> = emptyList(),
    val isProcessingPhotos: Boolean = false
)

data class CreateAlertUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 5,

    // Step 1
    val alertType: AlertType = AlertType.LOST,

    // Step 2 (LOST) — the owner's registered pet
    val myPets: List<Pet> = emptyList(),
    val selectedPet: Pet? = null,
    val isLoadingPets: Boolean = true,

    // Step 2 (FOUND) — the stray as described by the finder
    val foundPet: FoundPetForm = FoundPetForm(),

    // Step 3 (shared)
    val location: Location? = null,
    val address: String = "",
    val description: String = "",
    val isLoadingLocation: Boolean = false,
    val locationError: String? = null,

    // Step 4 (shared) — nearby alerts that might be the same animal
    val matches: List<AlertWithDetails> = emptyList(),
    val isLoadingMatches: Boolean = false,
    val matchesError: String? = null,

    // Step 5 (shared)
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
            2 -> if (alertType == AlertType.LOST) selectedPet != null else true
            3 -> location != null
            4 -> true
            5 -> !isSubmitting
            else -> false
        }

    val progress: Float
        get() = currentStep.toFloat() / totalSteps.toFloat()
}
