package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("alert_id")
    val alertId: String? = null,
    val type: String,
    val title: String,
    val body: String,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
)