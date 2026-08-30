package com.fabriziogo.epona.core.domain.usecase.sighting

import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.Sighting
import com.fabriziogo.epona.core.domain.model.isLocalPhotoUri
import com.fabriziogo.epona.core.domain.model.withUploadedPhotos
import com.fabriziogo.epona.core.domain.repository.SightingRepository
import javax.inject.Inject

class ReportSightingUseCase @Inject constructor(
    private val sightingRepo: SightingRepository
) {
    /**
     * Photos are uploaded before the sighting is created: `create_sighting` already
     * takes the URL array, so one round trip carries the whole report.
     */
    suspend operator fun invoke(sighting: Sighting): Result<Sighting> {
        require(sighting.alertId.isNotBlank()) { "Alert ID is required" }
        require(sighting.location != Location.EMPTY) {
            "Sighting location is required"
        }
        require(sighting.photoUrls.size <= MAX_PHOTOS_PER_ENTITY) {
            "At most $MAX_PHOTOS_PER_ENTITY photos per sighting"
        }

        val localUris = sighting.photoUrls.filter { it.isLocalPhotoUri() }
        if (localUris.isEmpty()) return sightingRepo.reportSighting(sighting)

        val uploaded = sightingRepo.uploadSightingPhotos(localUris)
            .getOrElse { return Result.failure(it) }

        return sightingRepo.reportSighting(
            sighting.copy(photoUrls = sighting.photoUrls.withUploadedPhotos(uploaded))
        )
    }
}
