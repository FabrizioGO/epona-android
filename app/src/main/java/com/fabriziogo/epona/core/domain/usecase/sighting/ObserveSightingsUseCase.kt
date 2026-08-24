package com.fabriziogo.epona.core.domain.usecase.sighting

import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.fabriziogo.epona.core.domain.repository.SightingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSightingsUseCase @Inject constructor(
    private val sightingRepo: SightingRepository
) {
    operator fun invoke(alertId: String): Flow<List<SightingWithReporter>> =
        sightingRepo.observeSightings(alertId)
}