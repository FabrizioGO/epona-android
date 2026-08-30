package com.fabriziogo.epona.core.domain.usecase.pet

import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.isLocalPhotoUri
import com.fabriziogo.epona.core.domain.model.withUploadedPhotos
import com.fabriziogo.epona.core.domain.repository.PetRepository
import javax.inject.Inject

class CreatePetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    /**
     * [pet]'s photoUrls may hold local device URIs; those are uploaded and swapped
     * for their public URLs before the pet is written.
     */
    suspend operator fun invoke(pet: Pet): Result<Pet> {
        require(pet.name.isNotBlank()) { "Pet name is required" }
        require(pet.photoUrls.size <= MAX_PHOTOS_PER_ENTITY) {
            "At most $MAX_PHOTOS_PER_ENTITY photos per pet"
        }

        val localUris = pet.photoUrls.filter { it.isLocalPhotoUri() }
        if (localUris.isEmpty()) return petRepository.registerPet(pet)

        // Upload first. The bucket path names the owner rather than the pet, so the
        // bytes do not need a pet id — and a failure here leaves no half-made pet
        // behind for the user to find later.
        val uploaded = petRepository.uploadPetPhotos(localUris)
            .getOrElse { return Result.failure(it) }

        return petRepository.registerPet(
            pet.copy(photoUrls = pet.photoUrls.withUploadedPhotos(uploaded))
        )
    }
}
