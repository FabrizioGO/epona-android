package com.fabriziogo.epona.core.domain.usecase.location

import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepo: LocationRepository
) {
    suspend operator fun invoke(): Result<Location> =
        locationRepo.getCurrentLocation()
}