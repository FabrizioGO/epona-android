package com.fabriziogo.epona.core.domain.model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val alertId: String? = null,
    val type: NotificationType,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)