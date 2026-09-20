package com.fabriziogo.epona.core.domain.usecase.alert

import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.isLocalPhotoUri
import com.fabriziogo.epona.core.domain.model.withUploadedPhotos
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import com.fabriziogo.epona.core.domain.repository.PetRepository
import javax.inject.Inject

/**
 * Publishes a FOUND alert for a pet the reporter does not own. Mirrors
 * CreatePetUseCase: local photos are uploaded first via
 * PetRepository.uploadPetPhotos (the bucket path is keyed on the user, not the
 * pet, so it needs no pet id), then swapped in with withUploadedPhotos.
 *
 * Guards the location; deliberately does NOT require a pet id or a name — the
 * server creates the ownerless pet row in the same transaction.
 */
class CreateFoundAlertUseCase @Inject constructor(
    private val alertRepo: AlertRepository,
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(pet: Pet, alert: Alert): Result<Alert> {
        require(alert.type == AlertType.FOUND) { "CreateFoundAlertUseCase is for FOUND alerts" }
        require(alert.lastSeenLocation != Location.EMPTY) {
            "Last seen location is required"
        }
        // Not defaulted anywhere on the way here, and the RPC rejects a null too.
        // Guessing this either strands an animal nobody may report seeing, or has a
        // neighbourhood watching for a dog that is asleep in someone's flat.
        requireNotNull(alert.foundCustody) {
            "Where the pet is now is required for a found alert"
        }
        require(pet.photoUrls.size <= MAX_PHOTOS_PER_ENTITY) {
            "At most $MAX_PHOTOS_PER_ENTITY photos per pet"
        }

        val localUris = pet.photoUrls.filter { it.isLocalPhotoUri() }
        if (localUris.isEmpty()) return alertRepo.createFoundAlert(pet, alert)

        // Upload first. A failure here leaves no half-made pet behind, and the
        // create_found_alert RPC stays a single transaction server-side.
        val uploaded = petRepository.uploadPetPhotos(localUris)
            .getOrElse { return Result.failure(it) }

        return alertRepo.createFoundAlert(
            pet.copy(photoUrls = pet.photoUrls.withUploadedPhotos(uploaded)),
            alert
        )
    }
}
