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

    /**
     * Fetches the current FCM registration token and writes it to the user row.
     *
     * Distinct from [updateFcmToken], which records a token the messaging service
     * has just been handed. `onNewToken` normally fires at first launch, before any
     * sign-in, so the token that matters is almost always one nobody ever pushed.
     */
    suspend fun syncFcmToken(): Result<Unit>

    /**
     * Writes the device language to `users.locale`. Notification titles and bodies are
     * rendered server-side from this column, so a stale value means the user is told
     * about their lost pet in the wrong language.
     */
    suspend fun syncLocale(): Result<Unit>

    /**
     * Uploads a locally picked avatar image and returns its public URL, in the
     * same way [com.fabriziogo.epona.core.domain.repository.PetRepository.uploadPetPhotos]
     * does for pets. Writes nothing to the profile — the caller decides when the
     * URL ends up on the user row, which lets validation run before any upload.
     */
    suspend fun uploadAvatar(localUri: String): Result<String>

    suspend fun getUserStats(userId: String): Result<UserStats>
}