package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toAlertEntity
import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.data.mapper.toEntity
import com.fabriziogo.epona.core.data.mapper.toInsertParams
import com.fabriziogo.epona.core.database.dao.AlertDao
import com.fabriziogo.epona.core.database.dao.PetDao
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import com.fabriziogo.epona.core.network.service.AlertService
import com.fabriziogo.epona.core.network.service.AuthService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val alertService: AlertService,
    private val alertDao: AlertDao,
    private val petDao: PetDao
) : AlertRepository {

    private fun currentUserId(): String =
        authService.getCurrentUserId()
            ?: throw IllegalStateException("Not authenticated")

    override fun observeNearbyAlerts(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        type: AlertType?
    ): Flow<List<AlertWithDetails>> {
        // Observe from local cache; refresh triggered separately
        return if (type != null) {
            alertDao.observeActiveAlertsByType(type.value)
        } else {
            alertDao.observeActiveAlerts()
        }.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getNearbyAlerts(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        type: AlertType?,
        limit: Int,
        offset: Int
    ): Result<List<AlertWithDetails>> = runCatching {
        val dtos = alertService.getNearbyAlerts(
            lat = latitude,
            lng = longitude,
            radiusMeters = radiusMeters,
            type = type?.value,
            limit = limit,
            offset = offset
        )
        dtos.map { it.toDomain() }
    }.recoverCatching {
        // Fallback: serve from cache without geo-filtering
        alertDao.getActiveAlerts(limit, offset).map { it.toDomain() }
    }

    override suspend fun getAlertDetail(
        alertId: String
    ): Result<AlertWithDetails> = runCatching {
        val dto = alertService.getAlertDetail(alertId)
        // Cache alert and pet entities
        alertDao.insertAlert(dto.toAlertEntity())
        dto.toDomain()
    }.recoverCatching {
        alertDao.getAlertWithPet(alertId)?.toDomain()
            ?: throw IllegalStateException("Alert not found")
    }

    override suspend fun getMyAlerts(): Result<List<AlertWithDetails>> =
        runCatching {
            val userId = currentUserId()
            val dtos = alertService.getMyAlerts(userId)
            // Cache locally
            dtos.forEach { dto ->
                alertDao.insertAlert(dto.toEntity())
            }
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
            val entity = dto.toEntity(
                lat = alert.lastSeenLocation.latitude,
                lng = alert.lastSeenLocation.longitude
            )
            alertDao.insertAlert(entity)
            alert.copy(id = dto.id ?: "")
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
}