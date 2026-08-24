package com.fabriziogo.epona.feature.alert.resolve

data class ResolveAlertUiState(
    val isLoading: Boolean = true,
    val petName: String = "",
    val error: String? = null
)