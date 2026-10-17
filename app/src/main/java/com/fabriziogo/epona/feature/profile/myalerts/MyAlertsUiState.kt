package com.fabriziogo.epona.feature.profile.myalerts

import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.model.AlertWithDetails

data class MyAlertsUiState(
    val isLoading: Boolean = true,
    val alerts: List<AlertWithDetails> = emptyList(),
    val adoptionPosts: List<AdoptionPostWithOwner> = emptyList(),
    val error: String? = null
)
