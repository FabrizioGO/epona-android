package com.fabriziogo.epona.core.domain.usecase.location

import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import javax.inject.Inject

class CalculateDistanceUseCase @Inject constructor(
    private val locationRepo: LocationRepository
) {
    /** Distance in meters between two points. */
    operator fun invoke(from: Location, to: Location): Double =
        locationRepo.calculateDistance(from, to)
}
