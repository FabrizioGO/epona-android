package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface AlertRepository {

    fun observeNearbyAlerts(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 10_000,
        type: AlertType? = null
    ): Flow<List<AlertWithDetails>>

    suspend fun getNearbyAlerts(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 10_000,
        type: AlertType? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<AlertWithDetails>>

    suspend fun getAlertDetail(alertId: String): Result<AlertWithDetails>

    suspend fun getMyAlerts(): Result<List<AlertWithDetails>>

    suspend fun createAlert(alert: Alert): Result<Alert>

    /**
     * Publishes a FOUND alert backed by an ownerless pet row (no owner id, no
     * name). The pet's photos must already be remote URLs; see
     * CreateFoundAlertUseCase, which uploads them first.
     */
    suspend fun createFoundAlert(pet: Pet, alert: Alert): Result<Alert>

    suspend fun resolveAlert(alertId: String): Result<Unit>

    suspend fun deleteAlert(alertId: String): Result<Unit>
}