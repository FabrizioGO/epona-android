package com.fabriziogo.epona.core.domain.model

data class UserStats(
    val totalPets: Int = 0,
    val activeAlerts: Int = 0,
    val resolvedAlerts: Int = 0,
    val sightingsReported: Int = 0
)