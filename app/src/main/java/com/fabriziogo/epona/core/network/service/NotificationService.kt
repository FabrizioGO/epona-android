package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.NotificationDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.concurrent.atomic.AtomicInteger
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
     * Distinguishes one subscription's topic from the next.
     *
     * [io.github.jan.supabase.realtime.Realtime.channel] hands back the *existing*
     * channel when a topic is already registered, and `removeChannel` unsubscribes
     * that shared instance. Two collectors on one topic would therefore re-send JOIN
     * on an already-joined channel and tear each other's stream down on teardown. A
     * per-subscription suffix makes the teardown below correct no matter how many
     * collectors exist at once.
     */
    private val subscriptionSeq = AtomicInteger(0)

    /**
     * Observes realtime changes to this user's rows in `notifications`.
     * Emits a bare [PostgresAction]; the payload is never decoded -- see
     * `NotificationRepositoryImpl.observeNotifications`, which treats each emission
     * as a tick and re-runs the normal fetch.
     *
     * The channel has to be joined for anything to arrive, and it can only be joined
     * once the change flow exists -- `subscribe()` is what sends the server the set of
     * postgres_changes bindings registered on the channel so far. Both are tied to the
     * lifetime of the collector, so the channel is torn down when collection stops.
     *
     * The `user_id` binding is not decoration: without it the server streams every
     * row of the table to every client. RLS filters what a client may *read*, not what
     * realtime *delivers*, so this is the only thing keeping other users' notification
     * titles off the wire.
     */
    fun observeNotifications(userId: String): Flow<PostgresAction> = flow {
        val channel = supabase.client.realtime.channel(
            "notifications-$userId-${subscriptionSeq.incrementAndGet()}"
        )
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "notifications"
            // `filter` has a private setter in supabase-kt 3.x; this is the public form.
            filter("user_id", FilterOperator.EQ, userId)
        }
        channel.subscribe()
        try {
            emitAll(changes)
        } finally {
            withContext(NonCancellable) {
                supabase.client.realtime.removeChannel(channel)
            }
        }
    }
}