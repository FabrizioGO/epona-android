package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.SightingInsertParams
import com.fabriziogo.epona.core.network.dto.SightingDto
import com.fabriziogo.epona.core.network.dto.SightingTrailDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SightingService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val table get() = supabase.client.postgrest["sightings"]

    suspend fun getSightingTrail(alertId: String): List<SightingTrailDto> =
        supabase.client.postgrest["v_sighting_trail"]
            .select {
                filter { eq("alert_id", alertId) }
                order("spotted_at", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }.decodeList()

    suspend fun reportSighting(params: SightingInsertParams): SightingDto =
        supabase.client.postgrest.rpc(
            "create_sighting",
            params
        ).decodeSingle()

    suspend fun deleteSighting(sightingId: String) {
        table.delete {
            filter { eq("id", sightingId) }
        }
    }

    /**
     * Observe realtime sighting inserts for a specific alert.
     */
    fun observeSightings(alertId: String): Flow<PostgresAction> {
        val channel = supabase.client.realtime.channel("sightings-$alertId")
        return channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "sightings"
            //filter = "alert_id=eq.$alertId"
        }
    }
}