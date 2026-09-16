package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.UserDto
import com.fabriziogo.epona.core.network.dto.UserLocationUpdateDto
import com.fabriziogo.epona.core.network.dto.UserStatsDto
import com.fabriziogo.epona.core.network.dto.UserUpdateDto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val table get() = supabase.client.postgrest["users"]

    suspend fun getUser(userId: String): UserDto =
        table.select {
            filter { eq("id", userId) }
        }.decodeSingle()

    suspend fun updateProfile(
        userId: String,
        update: UserUpdateDto
    ): UserDto =
        table.update(update) {
            filter { eq("id", userId) }
            // Without this the request goes out as return=minimal, PostgREST answers
            // with an empty body, and decodeSingle fails with
            // "Expected start of the array '[', but had 'EOF' instead".
            // Same pattern as PetService.updatePet.
            select()
        }.decodeSingle()

    suspend fun updateLocation(
        userId: String,
        location: UserLocationUpdateDto
    ) {
        supabase.client.postgrest.rpc(
            "update_user_location",
            buildJsonObject {
                put("p_user_id", userId)
                put("p_lat", location.lat)
                put("p_lng", location.lng)
            }
        )
    }

    suspend fun updateFcmToken(userId: String, token: String) {
        table.update(
            UserUpdateDto(fcmToken = token)
        ) {
            filter { eq("id", userId) }
        }
    }

    suspend fun getUserStats(userId: String): UserStatsDto =
        supabase.client.postgrest.rpc(
            "get_user_stats",
            buildJsonObject {
                put("p_user_id", userId)
            }
        ).decodeSingle()
}