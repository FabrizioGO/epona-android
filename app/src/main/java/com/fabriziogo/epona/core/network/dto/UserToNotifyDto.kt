package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Maps to the result of RPC `get_users_to_notify`
@Serializable
data class UserToNotifyDto(
    @SerialName("user_id")
    val userId: String,
    @SerialName("fcm_token")
    val fcmToken: String,
    @SerialName("distance_km")
    val distanceKm: Double
)