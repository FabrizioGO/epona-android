package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {

    suspend fun getCurrentLocation(): Result<Location>

    fun observeLocation(): Flow<Location>

    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Result<String>  // Returns address string

    fun calculateDistance(
        from: Location,
        to: Location
    ): Double  // Returns meters
}