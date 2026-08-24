package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.NotificationDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val table get() = supabase.client.postgrest["notifications"]

    suspend fun getNotifications(
        userId: String,
        limit: Int = 50,
        offset: Int = 0
    ): List<NotificationDto> =
        table.select {
            filter { eq("user_id", userId) }
            order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            range(offset.toLong(), (offset + limit - 1).toLong())
        }.decodeList()

    suspend fun getUnreadCount(userId: String): Int =
        table.select {
            filter {
                eq("user_id", userId)
                eq("is_read", false)
            }
            count(io.github.jan.supabase.postgrest.query.Count.EXACT)
            //head()
        }.countOrNull()?.toInt() ?: 0

    suspend fun markAllAsRead(userId: String) {
        supabase.client.postgrest.rpc(
            "mark_notifications_read",
            buildJsonObject {
                put("p_user_id", userId)
            }
        )
    }

    suspend fun markAsRead(notificationId: String) {
        table.update(
            buildMap<String, Any> {
                put("is_read", true)
            }
        ) {
            filter { eq("id", notificationId) }
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        table.delete {
            filter { eq("id", notificationId) }
        }
    }

    /**
     * Observe new notifications in realtime.
     */
    fun observeNotifications(userId: String): Flow<PostgresAction> {
        val channel = supabase.client.realtime.channel("notifications-$userId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
            //filter = "user_id=eq.$userId"
        }
    }
}