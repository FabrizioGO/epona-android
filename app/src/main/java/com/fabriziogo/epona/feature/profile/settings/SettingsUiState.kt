package com.fabriziogo.epona.feature.profile.settings

data class SettingsUiState(
    val alertRadiusKm: Int = 10,
    val notificationsEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)
