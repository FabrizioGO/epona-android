package com.fabriziogo.epona.core.domain.model

data class Alert(
    val id: String = "",
    val petId: String = "",
    val userId: String = "",
    val type: AlertType,
    val status: AlertStatus = AlertStatus.ACTIVE,
    val lastSeenLocation: Location,
    val lastSeenAddress: String? = null,
    val lastSeenAt: Long = 0L,
    val description: String? = null,
    val reward: Double? = null,
    val contactPhone: String? = null,
    val sightingCount: Int = 0,
    val resolvedAt: Long? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

/**
 * Alert with joined pet and owner data.
 * Used for list/detail display without extra queries.
 */
data class AlertWithDetails(
    val alert: Alert,
    val pet: Pet,
    val ownerName: String,
    val ownerAvatar: String? = null,
    val distanceMeters: Double? = null
)