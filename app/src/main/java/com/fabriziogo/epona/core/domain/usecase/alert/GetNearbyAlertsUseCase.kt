package com.fabriziogo.epona.core.domain.usecase.alert

import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import javax.inject.Inject

class GetNearbyAlertsUseCase @Inject constructor(
    private val alertRepo: AlertRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 10_000,
        type: AlertType? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<AlertWithDetails>> =
        alertRepo.getNearbyAlerts(
            latitude, longitude, radiusMeters, type, limit, offset
        )
}