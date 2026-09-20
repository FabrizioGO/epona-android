package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlertDto(
    val id: String? = null,
    @SerialName("pet_id")
    val petId: String,
    @SerialName("user_id")
    val userId: String? = null,
    val type: String,
    val status: String = "active",
    // Null on every lost alert, and on found alerts written before this column
    // existed, so FoundCustody.fromValue maps it to null rather than a default.
    @SerialName("found_custody")
    val foundCustody: String? = null,
    // Computed columns, so they only arrive when the select names them
    // (AlertService.getMyAlerts). Absent from RPC responses, hence nullable.
    @SerialName("last_seen_lat")
    val lastSeenLat: Double? = null,
    @SerialName("last_seen_lng")
    val lastSeenLng: Double? = null,
    @SerialName("last_seen_address")
    val lastSeenAddress: String? = null,
    @SerialName("last_seen_at")
    val lastSeenAt: String? = null,
    val description: String? = null,
    val reward: Double? = null,
    @SerialName("contact_phone")
    val contactPhone: String? = null,
    @SerialName("sighting_count")
    val sightingCount: Int = 0,
    @SerialName("resolved_at")
    val resolvedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Insert DTO uses RPC to handle PostGIS POINT creation
 * because Postgrest can't directly insert geography types.
 */
@Serializable
data class FoundAlertInsertParams(
    @SerialName("p_user_id")
    val userId: String,
    @SerialName("p_species")
    val species: String,
    @SerialName("p_lat")
    val lat: Double,
    @SerialName("p_lng")
    val lng: Double,
    /** "with_finder" | "at_location". Required — the RPC rejects anything else. */
    @SerialName("p_custody")
    val custody: String,
    @SerialName("p_breed")
    val breed: String? = null,
    @SerialName("p_color")
    val color: String? = null,
    @SerialName("p_size")
    val size: String? = null,
    @SerialName("p_pet_description")
    val petDescription: String? = null,
    @SerialName("p_photo_urls")
    val photoUrls: List<String> = emptyList(),
    @SerialName("p_address")
    val address: String? = null,
    @SerialName("p_last_seen_at")
    val lastSeenAt: String? = null,
    @SerialName("p_alert_description")
    val alertDescription: String? = null,
    @SerialName("p_contact_phone")
    val contactPhone: String? = null
)

@Serializable
data class AlertInsertParams(
    @SerialName("p_pet_id")
    val petId: String,
    @SerialName("p_user_id")
    val userId: String,
    @SerialName("p_type")
    val type: String,
    @SerialName("p_lat")
    val lat: Double,
    @SerialName("p_lng")
    val lng: Double,
    @SerialName("p_address")
    val address: String? = null,
    @SerialName("p_last_seen_at")
    val lastSeenAt: String? = null,
    @SerialName("p_description")
    val description: String? = null,
    @SerialName("p_reward")
    val reward: Double? = null,
    @SerialName("p_contact_phone")
    val contactPhone: String? = null
)