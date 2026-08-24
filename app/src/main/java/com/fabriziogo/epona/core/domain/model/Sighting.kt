package com.fabriziogo.epona.core.domain.model

data class Sighting(
    val id: String = "",
    val alertId: String,
    val reporterId: String = "",
    val location: Location,
    val address: String? = null,
    val photoUrls: List<String> = emptyList(),
    val note: String? = null,
    val spottedAt: Long = 0L,
    val createdAt: Long = 0L
)

data class SightingWithReporter(
    val sighting: Sighting,
    val reporterName: String,
    val reporterAvatar: String? = null
)