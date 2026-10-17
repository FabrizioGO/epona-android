package com.fabriziogo.epona.core.domain.usecase.adoption

import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.isLocalPhotoUri
import com.fabriziogo.epona.core.domain.model.withUploadedPhotos
import com.fabriziogo.epona.core.domain.repository.AdoptionRepository
import com.fabriziogo.epona.core.domain.repository.PetRepository
import javax.inject.Inject

const val ADOPTION_TITLE_MAX_LENGTH = 120
const val ADOPTION_TITLE_MIN_LENGTH = 3

/**
 * Publishes an adoption post. Local photos are uploaded first through
 * [PetRepository.uploadPetPhotos] (the bucket path is keyed on the user, so no row
 * id is needed), then swapped in with [withUploadedPhotos] — same approach as
 * CreateFoundAlertUseCase. Validation failures throw like the other use cases'
 * `require` guards, before anything is uploaded.
 */
class CreateAdoptionPostUseCase @Inject constructor(
    private val adoptionRepo: AdoptionRepository,
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(post: AdoptionPost): Result<AdoptionPost> {
        val title = post.title.trim()
        require(title.length in ADOPTION_TITLE_MIN_LENGTH..ADOPTION_TITLE_MAX_LENGTH) {
            "Title must be $ADOPTION_TITLE_MIN_LENGTH-$ADOPTION_TITLE_MAX_LENGTH characters"
        }
        require(post.contactPhone.isNotBlank()) { "Contact phone is required" }
        require(post.price == null || (post.price.isFinite() && post.price >= 0.0)) {
            "Price cannot be negative"
        }
        require(post.photoUrls.size <= MAX_PHOTOS_PER_ENTITY) {
            "At most $MAX_PHOTOS_PER_ENTITY photos per post"
        }

        val clean = post.copy(title = title, contactPhone = post.contactPhone.trim())
        val localUris = clean.photoUrls.filter { it.isLocalPhotoUri() }
        if (localUris.isEmpty()) return adoptionRepo.createAdoptionPost(clean)

        val uploaded = petRepository.uploadPetPhotos(localUris)
            .getOrElse { return Result.failure(it) }

        return adoptionRepo.createAdoptionPost(
            clean.copy(photoUrls = clean.photoUrls.withUploadedPhotos(uploaded))
        )
    }
}
