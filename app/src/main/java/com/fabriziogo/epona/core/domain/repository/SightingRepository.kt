package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.Sighting
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import kotlinx.coroutines.flow.Flow

interface SightingRepository {

    fun observeSightings(alertId: String): Flow<List<SightingWithReporter>>

    suspend fun getSightingTrail(alertId: String): Result<List<SightingWithReporter>>

    suspend fun reportSighting(sighting: Sighting): Result<Sighting>

    /**
     * Uploads locally picked images and returns their public URLs, in the order
     * they were given. Runs before the sighting row exists — see
     * [PetRepository.uploadPetPhotos].
     */
    suspend fun uploadSightingPhotos(localUris: List<String>): Result<List<String>>

    suspend fun deleteSighting(sightingId: String): Result<Unit>
}
