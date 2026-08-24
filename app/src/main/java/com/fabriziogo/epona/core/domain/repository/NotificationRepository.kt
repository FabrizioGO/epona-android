package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    fun observeNotifications(userId: String): Flow<List<Notification>>

    fun observeUnreadCount(userId: String): Flow<Int>

    suspend fun getNotifications(
        limit: Int = 50,
        offset: Int = 0
    ): Result<List<Notification>>

    suspend fun markAllAsRead(): Result<Unit>

    suspend fun markAsRead(notificationId: String): Result<Unit>

    suspend fun deleteNotification(notificationId: String): Result<Unit>
}