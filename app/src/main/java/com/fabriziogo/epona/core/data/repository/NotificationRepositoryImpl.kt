package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.data.mapper.toEntity
import com.fabriziogo.epona.core.database.dao.NotificationDao
import com.fabriziogo.epona.core.domain.model.Notification
import com.fabriziogo.epona.core.domain.repository.NotificationRepository
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.NotificationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val notificationService: NotificationService,
    private val notificationDao: NotificationDao
) : NotificationRepository {

    private fun currentUserId(): String =
        authService.getCurrentUserId()
            ?: throw IllegalStateException("Not authenticated")

    override fun observeNotifications(userId: String): Flow<List<Notification>> =
        notificationDao.observeNotifications(userId).map { list ->
            list.map { it.toDomain() }
        }

    override fun observeUnreadCount(userId: String): Flow<Int> =
        notificationDao.observeUnreadCount(userId)

    override suspend fun getNotifications(
        limit: Int,
        offset: Int
    ): Result<List<Notification>> = runCatching {
        val userId = currentUserId()
        val dtos = notificationService.getNotifications(userId, limit, offset)
        val entities = dtos.map { it.toEntity() }
        notificationDao.insertNotifications(entities)
        dtos.map { it.toDomain() }
    }.recoverCatching {
        notificationDao.getNotifications(currentUserId())
            .map { it.toDomain() }
    }

    override suspend fun markAllAsRead(): Result<Unit> = runCatching {
        val userId = currentUserId()
        notificationService.markAllAsRead(userId)
        notificationDao.markAllAsRead(userId)
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> =
        runCatching {
            notificationService.markAsRead(notificationId)
            notificationDao.markAsRead(notificationId)
        }

    override suspend fun deleteNotification(
        notificationId: String
    ): Result<Unit> = runCatching {
        notificationService.deleteNotification(notificationId)
        notificationDao.deleteNotification(notificationId)
    }
}