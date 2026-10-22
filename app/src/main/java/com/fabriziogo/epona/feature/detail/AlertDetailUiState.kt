package com.fabriziogo.epona.feature.detail

import com.fabriziogo.epona.core.ui.text.UiText

import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.SightingWithReporter

data class AlertDetailUiState(
    val isLoading: Boolean = true,
    val alertDetail: AlertWithDetails? = null,
    val sightings: List<SightingWithReporter> = emptyList(),
    val isSightingsLoading: Boolean = false,
    val isCurrentUserOwner: Boolean = false,
    val currentUserId: String? = null,
    val showResolveDialog: Boolean = false,
    val isResolving: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    /** Whether FlagContentDialog is showing, for this alert. */
    val showFlagDialog: Boolean = false,
    val isSubmittingFlag: Boolean = false,
    val flagSuccessMessage: UiText? = null,
    /** The user the pending BlockUserDialog is about; null when it is not showing. */
    val blockTarget: BlockTarget? = null,
    val isBlocking: Boolean = false,
    val error: UiText? = null
)

data class BlockTarget(val userId: String, val userName: String)
