package com.fabriziogo.epona.feature.detail

import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.SightingWithReporter

data class AlertDetailUiState(
    val isLoading: Boolean = true,
    val alertDetail: AlertWithDetails? = null,
    val sightings: List<SightingWithReporter> = emptyList(),
    val isSightingsLoading: Boolean = false,
    val isCurrentUserOwner: Boolean = false,
    val showResolveDialog: Boolean = false,
    val isResolving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)