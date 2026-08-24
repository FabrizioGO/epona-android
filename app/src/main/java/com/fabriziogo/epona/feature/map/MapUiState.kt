package com.fabriziogo.epona.feature.map

import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.SightingWithReporter

data class MapUiState(
    val isLoading: Boolean = true,
    val alerts: List<AlertWithDetails> = emptyList(),
    val filteredAlerts: List<AlertWithDetails> = emptyList(),
    val selectedAlert: AlertWithDetails? = null,
    val sightingTrail: List<SightingWithReporter> = emptyList(),
    val isSightingsLoading: Boolean = false,
    val userLocation: Location? = null,
    val alertRadiusKm: Int = 10,
    val showLost: Boolean = true,
    val showFound: Boolean = true,
    val showRadiusCircle: Boolean = true,
    val showSightingTrail: Boolean = false,
    val cameraLat: Double = 0.0,
    val cameraLng: Double = 0.0,
    val cameraZoom: Float = 13f,
    val error: String? = null
)