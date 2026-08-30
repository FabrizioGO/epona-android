package com.fabriziogo.epona.core.domain.usecase.pet

import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.isLocalPhotoUri
import com.fabriziogo.epona.core.domain.model.withUploadedPhotos
import com.fabriziogo.epona.core.domain.repository.PetRepository
import javax.inject.Inject

class UpdatePetUseCase @Inject constructor(
    private val petRepo: PetRepository
) {
    /**
     * [pet]'s photoUrls mix URLs already on the server with newly picked local
     * URIs. Only the local ones are uploaded, and each is substituted in place so
     * the order the user arranged survives the save.
     */
    suspend operator fun invoke(pet: Pet): Result<Pet> {
        require(pet.id.isNotBlank()) { "Pet ID required for update" }
        require(pet.name.isNotBlank()) { "Pet name cannot be empty" }
        require(pet.photoUrls.size <= MAX_PHOTOS_PER_ENTITY) {
            "At most $MAX_PHOTOS_PER_ENTITY photos per pet"
        }

        val localUris = pet.photoUrls.filter { it.isLocalPhotoUri() }
        if (localUris.isEmpty()) return petRepo.updatePet(pet)

        val uploaded = petRepo.uploadPetPhotos(localUris)
            .getOrElse { return Result.failure(it) }

        return petRepo.updatePet(
            pet.copy(photoUrls = pet.photoUrls.withUploadedPhotos(uploaded))
        )
    }
}
