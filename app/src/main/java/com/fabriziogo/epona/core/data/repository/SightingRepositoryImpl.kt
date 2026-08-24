package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.data.mapper.toEntity
import com.fabriziogo.epona.core.data.mapper.toInsertParams
import com.fabriziogo.epona.core.database.dao.AlertDao
import com.fabriziogo.epona.core.database.dao.SightingDao
import com.fabriziogo.epona.core.domain.model.Sighting
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.fabriziogo.epona.core.domain.repository.SightingRepository
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.SightingService
import com.fabriziogo.epona.core.network.service.StorageService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SightingRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val sightingService: SightingService,
    private val storageService: StorageService,
    private val sightingDao: SightingDao,
    private val alertDao: AlertDao
) : SightingRepository {

    private fun currentUserId(): String =
        authService.getCurrentUserId()
            ?: throw IllegalStateException("Not authenticated")

    override fun observeSightings(
        alertId: String
    ): Flow<List<SightingWithReporter>> =
        sightingDao.observeSightingTrail(alertId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getSightingTrail(
        alertId: String
    ): Result<List<SightingWithReporter>> = runCatching {
        val dtos = sightingService.getSightingTrail(alertId)
        val entities = dtos.map { it.toEntity() }
        sightingDao.insertSightings(entities)
        dtos.map { it.toDomain() }
    }.recoverCatching {
        sightingDao.getSightingTrail(alertId).map { it.toDomain() }
    }

    override suspend fun reportSighting(
        sighting: Sighting
    ): Result<Sighting> = runCatching {
        val reporterId = currentUserId()
        val dto = sightingService.reportSighting(
            sighting.toInsertParams(reporterId)
        )
        // Increment local sighting count
        alertDao.incrementSightingCount(sighting.alertId)
        sighting.copy(id = dto.id ?: "", reporterId = reporterId)
    }

    override suspend fun uploadSightingPhoto(
        sightingId: String,
        imageBytes: ByteArray,
        fileName: String
    ): Result<String> = runCatching {
        storageService.uploadSightingPhoto(sightingId, imageBytes, fileName)
    }

    override suspend fun deleteSighting(sightingId: String): Result<Unit> =
        runCatching {
            sightingService.deleteSighting(sightingId)
            sightingDao.deleteSighting(sightingId)
        }
}