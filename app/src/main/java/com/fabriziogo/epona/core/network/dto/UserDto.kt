package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("display_name")
    val displayName: String,
    val email: String,
    val phone: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("alert_radius_km")
    val alertRadiusKm: Int = 10,
    @SerialName("fcm_token")
    val fcmToken: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class UserUpdateDto(
    @SerialName("display_name")
    val displayName: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url")
    val avatarUrl: String? = null,
    @SerialName("alert_radius_km")
    val alertRadiusKm: Int? = null,
    @SerialName("fcm_token")
    val fcmToken: String? = null
)

/**
 * PostGIS POINT as WKT text for location updates.
 * Supabase accepts geography via `ST_MakePoint(lng, lat)` in RPC
 * or as GeoJSON. We use RPC for location updates.
 */
@Serializable
data class UserLocationUpdateDto(
    val lat: Double,
    val lng: Double
)