package com.fabriziogo.epona.feature.alert.create

import com.fabriziogo.epona.core.ui.text.UiText

import androidx.annotation.StringRes
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.FoundCustody
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
    val locationError: Int? = null,
    /** FOUND only: where the pet is now. No default — the finder has to answer. */
    val custody: FoundCustody? = null,

    // Step 4 (shared) — nearby alerts that might be the same animal
    val matches: List<AlertWithDetails> = emptyList(),
    val isLoadingMatches: Boolean = false,
    val matchesError: UiText? = null,

    // Step 5 (shared)
    val contactPhone: String = "",
    val reward: String = "",

    // Submission
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: UiText? = null,

    /** Set when Continue is tapped on a blocked step; shows [stepBlocker] under the step. */
    val showStepError: Boolean = false
) {
    /** Why the current step cannot advance yet, or null when it can. */
    @get:StringRes
    val stepBlocker: Int?
        get() = when (currentStep) {
            2 -> if (alertType == AlertType.LOST && selectedPet == null) {
                R.string.create_error_select_pet
            } else null
            3 -> when {
                location == null -> R.string.create_error_set_location
                alertType == AlertType.FOUND && custody == null -> R.string.create_error_custody
                else -> null
            }
            else -> null
        }

    val canProceed: Boolean
        get() = if (currentStep == totalSteps) !isSubmitting else stepBlocker == null

    val progress: Float
        get() = currentStep.toFloat() / totalSteps.toFloat()
}
