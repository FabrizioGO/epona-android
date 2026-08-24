package com.fabriziogo.epona.core.domain.usecase.sighting

import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.fabriziogo.epona.core.domain.repository.SightingRepository
import javax.inject.Inject

class GetSightingTrailUseCase @Inject constructor(
    private val sightingRepo: SightingRepository
) {
    suspend operator fun invoke(
        alertId: String
    ): Result<List<SightingWithReporter>> {
        require(alertId.isNotBlank()) { "Alert ID cannot be empty" }
        return sightingRepo.getSightingTrail(alertId)
    }
}