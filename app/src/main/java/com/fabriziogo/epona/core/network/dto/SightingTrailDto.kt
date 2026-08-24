package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Maps to the view `v_sighting_trail`
@Serializable
data class SightingTrailDto(
    @SerialName("sighting_id")
    val sightingId: String,
    @SerialName("alert_id")
    val alertId: String,
    // Nullable so the trail still decodes against an older v_sighting_trail.
    val lat: Double? = null,
    val lng: Double? = null,
    val address: String? = null,
    @SerialName("sighting_photos")
    val sightingPhotos: List<String> = emptyList(),
    val note: String? = null,
    @SerialName("spotted_at")
    val spottedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("reporter_id")
    val reporterId: String,
    @SerialName("reporter_name")
    val reporterName: String,
    @SerialName("reporter_avatar")
    val reporterAvatar: String? = null
)