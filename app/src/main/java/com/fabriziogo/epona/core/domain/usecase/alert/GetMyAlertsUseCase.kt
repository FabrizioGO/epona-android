package com.fabriziogo.epona.core.domain.usecase.alert

import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import javax.inject.Inject

class GetMyAlertsUseCase @Inject constructor(
    private val alertRepo: AlertRepository
) {
    suspend operator fun invoke(): Result<List<AlertWithDetails>> =
        alertRepo.getMyAlerts()
}