package com.fabriziogo.epona.core.domain.usecase.alert

import com.fabriziogo.epona.core.domain.repository.AlertRepository
import javax.inject.Inject

class DeleteAlertUseCase @Inject constructor(
    private val alertRepo: AlertRepository
) {
    suspend operator fun invoke(alertId: String): Result<Unit> {
        require(alertId.isNotBlank()) { "Alert ID cannot be empty" }
        return alertRepo.deleteAlert(alertId)
    }
}
