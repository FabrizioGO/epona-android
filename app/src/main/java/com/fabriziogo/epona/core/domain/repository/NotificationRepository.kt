package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {

    /**
     * The signed-in user's notifications, newest first, kept fresh from the server.
     *
     * Reads Room, refreshes on first collection, and re-fetches whenever the realtime
     * channel reports a change. Emits an empty list rather than failing when there is
     * no session: three UI surfaces collect the unread count app-wide and none of them
     * can survive an exception on this flow.
     */
    fun observeNotifications(): Flow<List<Notification>>

    /**
     * Unread count straight from Room. Deliberately does *not* open a realtime channel
     * or hit the network -- see the KDoc on the implementation.
     */
    fun observeUnreadCount(): Flow<Int>

    /** Pulls the server list into Room. For pull-to-refresh and push-triggered sync. */
    suspend fun refreshNotifications(): Result<Unit>

    suspend fun markAllAsRead(): Result<Unit>

    suspend fun markAsRead(notificationId: String): Result<Unit>

    suspend fun deleteNotification(notificationId: String): Result<Unit>
}
