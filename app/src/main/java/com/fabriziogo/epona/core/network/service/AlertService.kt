package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.AlertDetailDto
import com.fabriziogo.epona.core.network.dto.AlertDto
import com.fabriziogo.epona.core.network.dto.AlertInsertParams
import com.fabriziogo.epona.core.network.dto.FoundAlertInsertParams
import com.fabriziogo.epona.core.network.dto.NearbyAlertDto
import com.fabriziogo.epona.core.network.dto.UserToNotifyDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val table get() = supabase.client.postgrest["alerts"]

    suspend fun getNearbyAlerts(
        lat: Double,
        lng: Double,
        radiusMeters: Int,
        type: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<NearbyAlertDto> =
        supabase.client.postgrest.rpc(
            "get_nearby_alerts",
            buildJsonObject {
                put("p_lat", lat)
                put("p_lng", lng)
                put("p_radius_meters", radiusMeters)
                type?.let { put("p_type", it) }
                put("p_limit", limit)
                put("p_offset", offset)
            }
        ).decodeList()

    suspend fun getAlertDetail(alertId: String): AlertDetailDto =
        supabase.client.postgrest.rpc(
            "get_alert_detail",
            buildJsonObject {
                put("p_alert_id", alertId)
            }
        ).decodeSingle()

    suspend fun getMyAlerts(userId: String): List<AlertDto> =
        table.select(Columns.raw("*,last_seen_lat,last_seen_lng")) {
            filter { eq("user_id", userId) }
            order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
        }.decodeList()

    suspend fun createAlert(params: AlertInsertParams): AlertDto =
        supabase.client.postgrest.rpc(
            "create_alert",
            params
        ).decodeSingle()

    suspend fun createFoundAlert(params: FoundAlertInsertParams): AlertDto =
        supabase.client.postgrest.rpc(
            "create_found_alert",
            params
        ).decodeSingle()

    suspend fun resolveAlert(alertId: String) {
        table.update(
            buildMap<String, Any> {
                put("status", "resolved")
            }
        ) {
            filter { eq("id", alertId) }
        }
    }

    suspend fun deleteAlert(alertId: String) {
        table.delete {
            filter { eq("id", alertId) }
        }
    }

    suspend fun getUsersToNotify(
        lat: Double,
        lng: Double,
        excludeUserId: String? = null
    ): List<UserToNotifyDto> =
        supabase.client.postgrest.rpc(
            "get_users_to_notify",
            buildJsonObject {
                put("p_alert_lat", lat)
                put("p_alert_lng", lng)
                excludeUserId?.let { put("p_exclude_user_id", it) }
            }
        ).decodeList()

    /**
     * Observe realtime changes to the alerts table.
     * Returns a Flow of PostgresAction (INSERT, UPDATE, DELETE).
     *
     * The channel has to be joined for anything to arrive, and it can only be joined once
     * the change flow exists -- `subscribe()` is what sends the server the set of
     * postgres_changes bindings registered on the channel so far. Both are tied to the
     * lifetime of the collector, so the channel is torn down when collection stops.
     */
    fun observeAlertChanges(): Flow<PostgresAction> = flow {
        val channel = supabase.client.realtime.channel("alerts-realtime")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "alerts"
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