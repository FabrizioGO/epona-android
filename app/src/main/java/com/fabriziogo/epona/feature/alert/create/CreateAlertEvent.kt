package com.fabriziogo.epona.feature.alert.create

import android.net.Uri
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.media.PhotoItem


sealed interface CreateAlertEvent {
    data object NextStep : CreateAlertEvent
    data object PreviousStep : CreateAlertEvent
    data object CloseClicked : CreateAlertEvent

    data class AlertTypeSelected(val type: AlertType) : CreateAlertEvent

    data class PetSelected(val pet: Pet) : CreateAlertEvent
    data object AddNewPetClicked : CreateAlertEvent

    // Step 2 (FOUND) — the stray as described by the finder
    data class FoundPetSpeciesChanged(val species: Species) : CreateAlertEvent
    data class FoundPetBreedChanged(val breed: String) : CreateAlertEvent
    data class FoundPetColorChanged(val color: String) : CreateAlertEvent
    data class FoundPetSizeChanged(val size: PetSize) : CreateAlertEvent
    data class FoundPetDescriptionChanged(val description: String) : CreateAlertEvent
    data class FoundPhotosPicked(val uris: List<Uri>) : CreateAlertEvent
    data class FoundPhotoRemoved(val photo: PhotoItem) : CreateAlertEvent

    data object UseCurrentLocation : CreateAlertEvent
    data object LocationPermissionDenied : CreateAlertEvent
    data class LocationPicked(val location: Location) : CreateAlertEvent
    data class DescriptionChanged(val text: String) : CreateAlertEvent

    // Step 4 — a nearby post that might be the same animal
    data class MatchSelected(val alertId: String) : CreateAlertEvent

    data class PhoneChanged(val phone: String) : CreateAlertEvent
    data class RewardChanged(val reward: String) : CreateAlertEvent
    data object PublishClicked : CreateAlertEvent

    data object ErrorDismissed : CreateAlertEvent
}

sealed interface CreateAlertNavEvent {
    data object NavigateBack : CreateAlertNavEvent
    data object NavigateToAddPet : CreateAlertNavEvent
    data class NavigateToSuccess(val alertId: String) : CreateAlertNavEvent

    /**
     * A match tapped on step 4. Opens the alert detail screen with the wizard
     * popped — detail already carries photos to confirm against and a prominent
     * "Report Sighting" button, a better landing point than dropping the user
     * into the sighting form for a pet they have not verified yet.
     */
    data class NavigateToMatch(val alertId: String) : CreateAlertNavEvent
}
