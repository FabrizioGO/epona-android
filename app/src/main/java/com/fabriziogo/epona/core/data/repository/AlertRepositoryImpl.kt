package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toAlertEntity
import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.data.mapper.toEntity
import com.fabriziogo.epona.core.data.mapper.toFoundAlertInsertParams
import com.fabriziogo.epona.core.data.mapper.toInsertParams
import com.fabriziogo.epona.core.data.mapper.toOwnerEntity
import com.fabriziogo.epona.core.data.mapper.toPetEntity
import com.fabriziogo.epona.core.database.dao.AlertDao
import com.fabriziogo.epona.core.database.dao.PetDao
import com.fabriziogo.epona.core.database.dao.UserDao
import com.fabriziogo.epona.core.database.entity.PetEntity
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import com.fabriziogo.epona.core.network.dto.AlertDetailDto
import com.fabriziogo.epona.core.network.service.AlertService
import com.fabriziogo.epona.core.network.service.AuthService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val alertService: AlertService,
    private val alertDao: AlertDao,
    private val petDao: PetDao,
    private val userDao: UserDao
) : AlertRepository {

    private suspend fun currentUserId(): String = authService.requireUserId()

    /**
     * Re-runs the nearby query whenever the `alerts` table changes.
     *
     * This deliberately does not observe Room. `get_nearby_alerts` returns a denormalised
     * projection with no `pet_id` or `user_id`, so its rows cannot be written to the local
     * schema at all — observing the cache here only ever emitted an empty list, which then
     * overwrote the alerts the feed had just fetched from the server.
     *
     * A failed refresh is dropped rather than emitted: a transient network error must not
     * blank out a list that is still valid on screen.
     */
    override fun observeNearbyAlerts(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        type: AlertType?
    ): Flow<List<AlertWithDetails>> =
        alertService.observeAlertChanges()
            .mapNotNull {
                getNearbyAlerts(latitude, longitude, radiusMeters, type)
                    .onFailure { e -> Timber.w(e, "Realtime alert refresh failed") }
                    .getOrNull()
            }
            // Realtime is an enhancement here, not the source of the feed: if the channel
            // cannot be joined -- the table is not in the publication, the socket is down --
            // the collector must simply stop receiving updates, not take its caller down.
            .catch { e -> Timber.w(e, "Realtime alert channel closed") }

    override suspend fun getNearbyAlerts(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        type: AlertType?,
        limit: Int,
        offset: Int
    ): Result<List<AlertWithDetails>> = runCatching {
        authService.awaitReady()
        val dtos = alertService.getNearbyAlerts(
            lat = latitude,
            lng = longitude,
            radiusMeters = radiusMeters,
            type = type?.value,
            limit = limit,
            offset = offset
        )
        dtos.map { it.toDomain() }
    }.recoverCatching { networkError ->
        // Fallback: serve from cache without geo-filtering. An empty cache is not an
        // answer — reporting it as one is what made a failed request look like "no alerts
        // near you" instead of surfacing the error.
        alertDao.getActiveAlerts(limit, offset)
            .map { it.toDomain() }
            .ifEmpty { throw networkError }
    }

    override suspend fun getAlertDetail(
        alertId: String
    ): Result<AlertWithDetails> = runCatching {
        authService.awaitReady()
        val dto = alertService.getAlertDetail(alertId)
        cacheAlertDetail(dto)
        dto.toDomain()
    }.recoverCatching { networkError ->
        alertDao.getAlertWithPet(alertId)?.toDomain()
            ?: throw networkError
    }

    override suspend fun getMyAlerts(): Result<List<AlertWithDetails>> =
        runCatching {
            val userId = currentUserId()
            val dtos = alertService.getMyAlerts(userId)
            cache { alertDao.insertAlerts(dtos.map { it.toEntity() }) }
            alertDao.getMyAlerts(userId).map { it.toDomain() }
        }.recoverCatching {
            alertDao.getMyAlerts(currentUserId()).map { it.toDomain() }
        }

    override suspend fun createAlert(alert: Alert): Result<Alert> =
        runCatching {
            val userId = currentUserId()
            val dto = alertService.createAlert(
                alert.toInsertParams(userId)
            )
            cache {
                alertDao.insertAlert(
                    dto.toEntity(
                        lat = alert.lastSeenLocation.latitude,
                        lng = alert.lastSeenLocation.longitude
                    )
                )
            }
            alert.copy(id = dto.id ?: "")
        }

    override suspend fun createFoundAlert(pet: Pet, alert: Alert): Result<Alert> =
        runCatching {
            val userId = currentUserId()
            val dto = alertService.createFoundAlert(
                toFoundAlertInsertParams(pet, alert, userId)
            )
            // Cache the ownerless pet first: alerts.pet_id is an enforced foreign
            // key, and unlike the LOST path the pet is not already in the cache.
            // A null owner_id skips SQLite foreign-key enforcement, so this never
            // adopts the stray into anyone's My Pets (observeMyPets matches
            // WHERE owner_id = :ownerId, which never matches NULL).
            cache {
                petDao.insertPet(
                    PetEntity(
                        id = dto.petId,
                        ownerId = null,
                        name = null,
                        species = pet.species.value,
                        breed = pet.breed,
                        color = pet.color,
                        size = pet.size.value,
                        gender = pet.gender.value,
                        microchipId = null,
                        description = pet.description,
                        photoUrls = pet.photoUrls
                    )
                )
                alertDao.insertAlert(
                    dto.toEntity(
                        lat = alert.lastSeenLocation.latitude,
                        lng = alert.lastSeenLocation.longitude
                    )
                )
            }
            alert.copy(id = dto.id ?: "", petId = dto.petId)
        }

    override suspend fun resolveAlert(alertId: String): Result<Unit> =
        runCatching {
            alertService.resolveAlert(alertId)
            alertDao.resolveAlert(alertId)
        }

    override suspend fun deleteAlert(alertId: String): Result<Unit> =
        runCatching {
            alertService.deleteAlert(alertId)
            alertDao.deleteAlert(alertId)
        }

    /**
     * Writes a detail payload to the cache parent-first. `alerts.pet_id` and
     * `alerts.user_id` are enforced foreign keys, so the pet and the owner have to exist
     * locally before the alert row can be inserted at all.
     */
    private suspend fun cacheAlertDetail(dto: AlertDetailDto) = cache {
        userDao.insertUserIfAbsent(dto.toOwnerEntity())
        petDao.insertPet(dto.toPetEntity())
        alertDao.insertAlert(dto.toAlertEntity())
    }

    /**
     * Runs a cache write for its side effect only. The cache is an optimisation; a write
     * that fails — a foreign key we cannot satisfy from the payload at hand, say — must
     * never turn a response the server answered correctly into an error on screen.
     */
    private suspend inline fun cache(block: () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Failed to cache alerts locally")
        }
    }
}
