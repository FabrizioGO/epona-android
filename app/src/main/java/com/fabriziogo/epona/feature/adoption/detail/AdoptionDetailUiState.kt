package com.fabriziogo.epona.feature.adoption.detail

import com.fabriziogo.epona.core.ui.text.UiText

import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.model.AdoptionStatus

data class AdoptionDetailUiState(
    val isLoading: Boolean = true,
    val detail: AdoptionPostWithOwner? = null,
    val isOwner: Boolean = false,
    val isUpdating: Boolean = false,
    val showMarkAdoptedDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: UiText? = null
) {
    val isAdopted: Boolean get() = detail?.post?.status == AdoptionStatus.ADOPTED
}

sealed interface AdoptionDetailEvent {
    data object BackClicked : AdoptionDetailEvent
    data object ShareClicked : AdoptionDetailEvent
    data object CallClicked : AdoptionDetailEvent
    data object MarkAdoptedClicked : AdoptionDetailEvent
    data object MarkAdoptedConfirmed : AdoptionDetailEvent
    data object MarkAdoptedDismissed : AdoptionDetailEvent
    data object DeleteClicked : AdoptionDetailEvent
    data object DeleteConfirmed : AdoptionDetailEvent
    data object DeleteDismissed : AdoptionDetailEvent
    data object RetryLoad : AdoptionDetailEvent
    data object ErrorDismissed : AdoptionDetailEvent
}

sealed interface AdoptionDetailNavEvent {
    data object NavigateBack : AdoptionDetailNavEvent
    data class Share(val post: AdoptionPost, val url: String) : AdoptionDetailNavEvent
    data class DialPhone(val phone: String) : AdoptionDetailNavEvent
}
