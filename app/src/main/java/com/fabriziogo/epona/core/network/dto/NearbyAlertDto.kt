package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Maps to the result of RPC `get_nearby_alerts`
@Serializable
data class NearbyAlertDto(
    @SerialName("alert_id")
    val alertId: String,
    val type: String,
    // Nullable so the feed still decodes against an older get_nearby_alerts that
    // does not select these; the mapper then falls back as before.
    @SerialName("last_seen_lat")
    val lastSeenLat: Double? = null,
    @SerialName("last_seen_lng")
    val lastSeenLng: Double? = null,
    @SerialName("last_seen_address")
    val lastSeenAddress: String? = null,
    @SerialName("last_seen_at")
    val lastSeenAt: String? = null,
    val reward: Double? = null,
    @SerialName("sighting_count")
    val sightingCount: Int = 0,
    @SerialName("distance_meters")
    val distanceMeters: Double = 0.0,
    @SerialName("pet_name")
    val petName: String? = null,
    val species: String,
    val breed: String? = null,
    val color: String? = null,
    @SerialName("pet_photos")
    val petPhotos: List<String> = emptyList(),
    @SerialName("owner_name")
    val ownerName: String,
    @SerialName("created_at")
    val createdAt: String? = null
)