package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.Sighting
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import kotlinx.coroutines.flow.Flow

interface SightingRepository {

    fun observeSightings(alertId: String): Flow<List<SightingWithReporter>>

    suspend fun getSightingTrail(alertId: String): Result<List<SightingWithReporter>>

    suspend fun reportSighting(sighting: Sighting): Result<Sighting>

    suspend fun uploadSightingPhoto(
        sightingId: String,
        imageBytes: ByteArray,
        fileName: String
    ): Result<String>  // Returns photo URL

    suspend fun deleteSighting(sightingId: String): Result<Unit>
}