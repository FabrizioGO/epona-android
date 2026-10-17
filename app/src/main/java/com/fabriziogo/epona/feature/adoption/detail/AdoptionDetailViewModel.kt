package com.fabriziogo.epona.feature.adoption.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.usecase.adoption.DeleteAdoptionPostUseCase
import com.fabriziogo.epona.core.domain.usecase.adoption.GetAdoptionPostDetailUseCase
import com.fabriziogo.epona.core.domain.usecase.adoption.MarkAdoptionAdoptedUseCase
import com.fabriziogo.epona.feature.adoption.navigation.ADOPTION_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdoptionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAdoptionPostDetail: GetAdoptionPostDetailUseCase,
    private val markAdopted: MarkAdoptionAdoptedUseCase,
    private val deleteAdoptionPost: DeleteAdoptionPostUseCase,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle[ADOPTION_ID_ARG])

    private val _state = MutableStateFlow(AdoptionDetailUiState())
    val state: StateFlow<AdoptionDetailUiState> = _state.asStateFlow()

    private val _navEvents = Channel<AdoptionDetailNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init { load() }

    fun onEvent(event: AdoptionDetailEvent) {
        when (event) {
            AdoptionDetailEvent.BackClicked -> emitNav(AdoptionDetailNavEvent.NavigateBack)
            AdoptionDetailEvent.ShareClicked -> share()
            AdoptionDetailEvent.CallClicked -> call()
            AdoptionDetailEvent.MarkAdoptedClicked -> _state.update { it.copy(showMarkAdoptedDialog = true) }
            AdoptionDetailEvent.MarkAdoptedConfirmed -> markPostAdopted()
            AdoptionDetailEvent.MarkAdoptedDismissed -> _state.update { it.copy(showMarkAdoptedDialog = false) }
            AdoptionDetailEvent.DeleteClicked -> _state.update { it.copy(showDeleteDialog = true) }
            AdoptionDetailEvent.DeleteConfirmed -> deletePost()
            AdoptionDetailEvent.DeleteDismissed -> _state.update { it.copy(showDeleteDialog = false) }
            AdoptionDetailEvent.RetryLoad -> load()
            AdoptionDetailEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { getAdoptionPostDetail(postId) }
                .getOrElse { Result.failure(it) }
                .onSuccess { detail ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            detail = detail,
                            isOwner = detail.post.userId == authRepo.currentUserId
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.message ?: "Failed to load post") }
                }
        }
    }

    private fun markPostAdopted() {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, showMarkAdoptedDialog = false) }
            markAdopted(postId)
                .onSuccess {
                    _state.update { it.copy(isUpdating = false) }
                    load()
                }
                .onFailure { err ->
                    _state.update { it.copy(isUpdating = false, error = err.message ?: "Failed to update post") }
                }
        }
    }

    private fun deletePost() {
        viewModelScope.launch {
            _state.update { it.copy(isUpdating = true, showDeleteDialog = false) }
            deleteAdoptionPost(postId)
                .onSuccess { emitNav(AdoptionDetailNavEvent.NavigateBack) }
                .onFailure { err ->
                    _state.update { it.copy(isUpdating = false, error = err.message ?: "Failed to delete post") }
                }
        }
    }

    private fun share() {
        val post = _state.value.detail?.post ?: return
        emitNav(AdoptionDetailNavEvent.Share(post, "https://epona.app/adoption/${post.id}"))
    }

    private fun call() {
        val phone = _state.value.detail?.post?.contactPhone
        if (!phone.isNullOrBlank()) {
            emitNav(AdoptionDetailNavEvent.DialPhone(phone))
        } else {
            _state.update { it.copy(error = "No contact number available") }
        }
    }

    private fun emitNav(event: AdoptionDetailNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }
}
