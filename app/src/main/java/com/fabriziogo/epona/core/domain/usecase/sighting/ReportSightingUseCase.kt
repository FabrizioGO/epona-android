package com.fabriziogo.epona.core.domain.usecase.sighting

import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Sighting
import com.fabriziogo.epona.core.domain.repository.SightingRepository
import javax.inject.Inject

class ReportSightingUseCase @Inject constructor(
    private val sightingRepo: SightingRepository
) {
    suspend operator fun invoke(sighting: Sighting): Result<Sighting> {
        require(sighting.alertId.isNotBlank()) { "Alert ID is required" }
        require(sighting.location != Location.EMPTY) {
            "Sighting location is required"
        }
        return sightingRepo.reportSighting(sighting)
    }
}