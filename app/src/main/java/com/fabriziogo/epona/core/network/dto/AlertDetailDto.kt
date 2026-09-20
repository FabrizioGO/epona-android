package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Maps to the result of RPC `get_alert_detail`
@Serializable
data class AlertDetailDto(
    @SerialName("alert_id")
    val alertId: String,
    val type: String,
    val status: String,
    @SerialName("last_seen_lat")
    val lastSeenLat: Double,
    @SerialName("last_seen_lng")
    val lastSeenLng: Double,
    @SerialName("last_seen_address")
    val lastSeenAddress: String? = null,
    @SerialName("last_seen_at")
    val lastSeenAt: String? = null,
    @SerialName("alert_description")
    val alertDescription: String? = null,
    val reward: Double? = null,
    @SerialName("contact_phone")
    val contactPhone: String? = null,
    @SerialName("sighting_count")
    val sightingCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("resolved_at")
    val resolvedAt: String? = null,
    @SerialName("pet_id")
    val petId: String,
    @SerialName("pet_name")
    val petName: String? = null,
    val species: String,
    val breed: String? = null,
    val color: String? = null,
    val size: String? = null,
    val gender: String? = null,
    @SerialName("microchip_id")
    val microchipId: String? = null,
    @SerialName("pet_description")
    val petDescription: String? = null,
    @SerialName("pet_photos")
    val petPhotos: List<String> = emptyList(),
    @SerialName("pet_owner_id")
    val petOwnerId: String? = null,
    @SerialName("owner_id")
    val ownerId: String,
    @SerialName("owner_name")
    val ownerName: String,
    @SerialName("owner_avatar")
    val ownerAvatar: String? = null
)