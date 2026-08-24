package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Maps to the result of RPC `get_user_stats`
@Serializable
data class UserStatsDto(
    @SerialName("total_pets")
    val totalPets: Int = 0,
    @SerialName("active_alerts")
    val activeAlerts: Int = 0,
    @SerialName("resolved_alerts")
    val resolvedAlerts: Int = 0,
    @SerialName("sightings_reported")
    val sightingsReported: Int = 0
)