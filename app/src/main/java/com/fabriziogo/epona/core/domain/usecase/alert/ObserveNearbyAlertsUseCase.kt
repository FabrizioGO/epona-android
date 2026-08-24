package com.fabriziogo.epona.core.domain.usecase.alert

import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveNearbyAlertsUseCase @Inject constructor(
    private val alertRepo: AlertRepository
) {
    operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 10_000,
        type: AlertType? = null
    ): Flow<List<AlertWithDetails>> =
        alertRepo.observeNearbyAlerts(
            latitude, longitude, radiusMeters, type
        )
}