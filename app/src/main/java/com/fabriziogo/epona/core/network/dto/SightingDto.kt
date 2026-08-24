package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SightingDto(
    val id: String? = null,
    @SerialName("alert_id")
    val alertId: String,
    @SerialName("reporter_id")
    val reporterId: String? = null,
    val address: String? = null,
    @SerialName("photo_urls")
    val photoUrls: List<String> = emptyList(),
    val note: String? = null,
    @SerialName("spotted_at")
    val spottedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class SightingInsertParams(
    @SerialName("p_alert_id")
    val alertId: String,
    @SerialName("p_reporter_id")
    val reporterId: String,
    @SerialName("p_lat")
    val lat: Double,
    @SerialName("p_lng")
    val lng: Double,
    @SerialName("p_address")
    val address: String? = null,
    @SerialName("p_photo_urls")
    val photoUrls: List<String> = emptyList(),
    @SerialName("p_note")
    val note: String? = null,
    @SerialName("p_spotted_at")
    val spottedAt: String? = null
)