package com.fabriziogo.epona.feature.profile.blockedusers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.BlockedUser
import com.fabriziogo.epona.core.domain.usecase.block.GetBlockedUsersUseCase
import com.fabriziogo.epona.core.domain.usecase.block.UnblockUserUseCase
import com.fabriziogo.epona.core.ui.text.UiText
import com.fabriziogo.epona.core.ui.text.uiTextOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BlockedUsersUiState(
    val isLoading: Boolean = true,
    val users: List<BlockedUser> = emptyList(),
    /** Ids with an unblock request in flight, so their button can be disabled. */
    val unblocking: Set<String> = emptySet(),
    val error: UiText? = null
)

sealed interface BlockedUsersEvent {
    data class UnblockClicked(val userId: String) : BlockedUsersEvent
    data object Retry : BlockedUsersEvent
    data object ErrorDismissed : BlockedUsersEvent
}

@HiltViewModel
class BlockedUsersViewModel @Inject constructor(
    private val getBlockedUsers: GetBlockedUsersUseCase,
    private val unblockUser: UnblockUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BlockedUsersUiState())
    val state: StateFlow<BlockedUsersUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun onEvent(event: BlockedUsersEvent) {
        when (event) {
            is BlockedUsersEvent.UnblockClicked -> unblock(event.userId)
            BlockedUsersEvent.Retry -> load()
            BlockedUsersEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getBlockedUsers()
                .onSuccess { users ->
                    _state.update { it.copy(isLoading = false, users = users) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = uiTextOf(err.message, R.string.error_load_blocked_users)
                        )
                    }
                }
        }
    }

    private fun unblock(userId: String) {
        if (userId in _state.value.unblocking) return
        viewModelScope.launch {
            _state.update { it.copy(unblocking = it.unblocking + userId) }
            unblockUser(userId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            users = it.users.filterNot { u -> u.id == userId },
                            unblocking = it.unblocking - userId
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            unblocking = it.unblocking - userId,
                            error = uiTextOf(err.message, R.string.error_unblock_user)
                        )
                    }
                }
        }
    }
}
