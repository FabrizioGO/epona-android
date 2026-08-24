package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.model.UserStats
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    fun observeCurrentUser(): Flow<User?>

    suspend fun getUser(userId: String): Result<User>

    suspend fun updateProfile(
        displayName: String? = null,
        phone: String? = null,
        avatarUrl: String? = null
    ): Result<User>

    suspend fun updateLocation(
        latitude: Double,
        longitude: Double
    ): Result<Unit>

    suspend fun updateAlertRadius(radiusKm: Int): Result<Unit>

    suspend fun updateFcmToken(token: String): Result<Unit>

    suspend fun getUserStats(userId: String): Result<UserStats>
}