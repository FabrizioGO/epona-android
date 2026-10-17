package com.fabriziogo.epona.feature.profile.settings

import com.fabriziogo.epona.core.ui.text.UiText

data class SettingsUiState(
    val alertRadiusKm: Int = 10,
    val isSaving: Boolean = false,
    val error: UiText? = null
)
