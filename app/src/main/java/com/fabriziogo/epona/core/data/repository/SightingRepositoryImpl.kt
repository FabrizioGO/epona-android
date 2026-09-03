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
import com.fabriziogo.epona.core.data.media.PhotoUploader
import com.fabriziogo.epona.core.network.service.StorageService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SightingRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val sightingService: SightingService,
    private val photoUploader: PhotoUploader,
    private val sightingDao: SightingDao,
    private val alertDao: AlertDao
) : SightingRepository {

    private suspend fun currentUserId(): String = authService.requireUserId()

    /**
     * Re-reads the trail from the server whenever the `sightings` table changes.
     *
     * Observing Room here does not work: `sightings.alert_id` is an enforced foreign key,
     * so the trail can only be cached once its alert has been cached, and until then this
     * emitted an empty list straight over the trail the screen had already loaded.
     */
    override fun observeSightings(
        alertId: String
    ): Flow<List<SightingWithReporter>> =
        sightingService.observeSightings(alertId)
            .mapNotNull {
                getSightingTrail(alertId)
                    .onFailure { e -> Timber.w(e, "Realtime sighting refresh failed") }
                    .getOrNull()
            }
            // A channel that cannot be joined stops the updates, nothing more.
            .catch { e -> Timber.w(e, "Realtime sighting channel closed") }

    override suspend fun getSightingTrail(
        alertId: String
    ): Result<List<SightingWithReporter>> = runCatching {
        authService.awaitReady()
        val dtos = sightingService.getSightingTrail(alertId)
        // Best-effort: the foreign key onto `alerts` may not be satisfiable yet, and that
        // is no reason to discard a trail the server just returned.
        try {
            sightingDao.insertSightings(dtos.map { it.toEntity() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Failed to cache sighting trail locally")
        }
        dtos.map { it.toDomain() }
    }.recoverCatching { networkError ->
        sightingDao.getSightingTrail(alertId)
            .map { it.toDomain() }
            .ifEmpty { throw networkError }
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

    override suspend fun uploadSightingPhotos(
        localUris: List<String>
    ): Result<List<String>> = runCatching {
        photoUploader.uploadAll(StorageService.BUCKET_SIGHTINGS, localUris)
    }

    override suspend fun deleteSighting(sightingId: String): Result<Unit> =
        runCatching {
            sightingService.deleteSighting(sightingId)
            sightingDao.deleteSighting(sightingId)
        }
}