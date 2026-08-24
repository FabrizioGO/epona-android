package com.fabriziogo.epona.core.domain.usecase.alert

import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.repository.AlertRepository
import javax.inject.Inject

class CreateAlertUseCase @Inject constructor(
    private val alertRepo: AlertRepository
) {
    suspend operator fun invoke(alert: Alert): Result<Alert> {
        require(alert.petId.isNotBlank()) { "Pet must be selected" }
        require(alert.lastSeenLocation != Location.EMPTY) {
            "Last seen location is required"
        }
        return alertRepo.createAlert(alert)
    }
}