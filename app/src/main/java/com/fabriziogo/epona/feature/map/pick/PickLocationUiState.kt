package com.fabriziogo.epona.feature.map.pick

import com.fabriziogo.epona.core.domain.model.Location

const val DEFAULT_LAT = 40.7128
const val DEFAULT_LNG = -74.0060

data class PickLocationUiState(
    val isResolvingCamera: Boolean = true,
    val cameraLat: Double = DEFAULT_LAT,
    val cameraLng: Double = DEFAULT_LNG,
    val cameraZoom: Float = 16f,
    val target: Location? = null,
    val address: String = "",
    val isGeocoding: Boolean = false,
    val isLocating: Boolean = false,
    val error: String? = null
) {
    val canConfirm: Boolean get() = target != null
}
