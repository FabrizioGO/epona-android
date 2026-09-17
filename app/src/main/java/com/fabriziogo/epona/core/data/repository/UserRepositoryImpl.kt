package com.fabriziogo.epona.core.data.repository

import android.content.Context
import androidx.core.os.ConfigurationCompat
import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.data.mapper.toEntity
import com.fabriziogo.epona.core.data.media.PhotoUploader
import com.fabriziogo.epona.core.database.dao.UserDao
import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.model.UserStats
import com.fabriziogo.epona.core.domain.repository.UserRepository
import com.fabriziogo.epona.core.network.dto.UserLocationUpdateDto
import com.fabriziogo.epona.core.network.dto.UserUpdateDto
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.StorageService
import com.fabriziogo.epona.core.network.service.UserService
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val userService: UserService,
    private val photoUploader: PhotoUploader,
    private val userDao: UserDao,
    private val firebaseMessaging: FirebaseMessaging,
    @ApplicationContext private val context: Context
) : UserRepository {

    private suspend fun currentUserId(): String = authService.requireUserId()

    override fun observeCurrentUser(): Flow<User?> = flow {
        emitAll(userDao.observeUser(currentUserId()).map { it?.toDomain() })
    }

    override suspend fun getUser(userId: String): Result<User> =
        runCatching {
            authService.awaitReady()
            val dto = userService.getUser(userId)
            userDao.insertUser(dto.toEntity())
            dto.toDomain()
        }.recoverCatching {
            // Fallback to cache
            userDao.getUser(userId)?.toDomain()
                ?: throw IllegalStateException("User not found")
        }

    override suspend fun updateProfile(
        displayName: String?,
        phone: String?,
        avatarUrl: String?
    ): Result<User> = runCatching {
        val userId = currentUserId()
        val dto = userService.updateProfile(
            userId,
            UserUpdateDto(
                displayName = displayName,
                phone = phone,
                avatarUrl = avatarUrl
            )
        )
        userDao.insertUser(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun updateLocation(
        latitude: Double,
        longitude: Double
    ): Result<Unit> = runCatching {
        val userId = currentUserId()
        userService.updateLocation(
            userId,
            UserLocationUpdateDto(lat = latitude, lng = longitude)
        )
        userDao.updateLocation(userId, latitude, longitude)
    }

    override suspend fun updateAlertRadius(radiusKm: Int): Result<Unit> =
        runCatching {
            val userId = currentUserId()
            userService.updateProfile(
                userId,
                UserUpdateDto(alertRadiusKm = radiusKm)
            )
            userDao.updateAlertRadius(userId, radiusKm)
        }

    override suspend fun updateFcmToken(token: String): Result<Unit> =
        runCatching {
            val userId = currentUserId()
            userService.updateFcmToken(userId, token)
            userDao.updateFcmToken(userId, token)
        }

    override suspend fun syncFcmToken(): Result<Unit> = runCatching {
        val userId = currentUserId()
        val token = firebaseMessaging.token.await()
        userService.updateFcmToken(userId, token)
        userDao.updateFcmToken(userId, token)
    }

    override suspend fun syncLocale(): Result<Unit> = runCatching {
        userService.updateLocale(currentUserId(), deviceLanguage())
    }

    /**
     * The app's own resolved language rather than the raw system default, so a
     * per-app language override is honoured. Bare subtag, not a full tag: the server
     * matches "es", not "es-419".
     */
    private fun deviceLanguage(): String =
        ConfigurationCompat.getLocales(context.resources.configuration)
            .get(0)
            ?.language
            ?.takeIf { it.isNotBlank() }
            ?: Locale.getDefault().language

    /**
     * Play Services Tasks, awaited the way [LocationRepositoryImpl] does it -- the
     * project does not depend on kotlinx-coroutines-play-services.
     */
    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
        addOnCanceledListener { cont.cancel() }
    }

    override suspend fun uploadAvatar(localUri: String): Result<String> = runCatching {
        photoUploader.uploadAll(StorageService.BUCKET_AVATARS, listOf(localUri)).first()
    }

    override suspend fun getUserStats(userId: String): Result<UserStats> =
        runCatching {
            val dto = userService.getUserStats(userId)
            UserStats(
                totalPets = dto.totalPets,
                activeAlerts = dto.activeAlerts,
                resolvedAlerts = dto.resolvedAlerts,
                sightingsReported = dto.sightingsReported
            )
        }
}