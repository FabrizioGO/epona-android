package com.fabriziogo.epona.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.auth.GetCurrentUserUseCase
import com.fabriziogo.epona.core.domain.usecase.auth.SignOutUseCase
import com.fabriziogo.epona.core.domain.usecase.user.GetUserStatsUseCase
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
class ProfileViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getUserStats: GetUserStatsUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val _navEvents = Channel<ProfileNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init { loadProfile() }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            ProfileEvent.MyPetsClicked -> emitNav(ProfileNavEvent.NavigateToMyPets)
            ProfileEvent.MyAlertsClicked -> emitNav(ProfileNavEvent.NavigateToMyAlerts)
            ProfileEvent.SettingsClicked -> emitNav(ProfileNavEvent.NavigateToSettings)
            ProfileEvent.HelpClicked -> emitNav(ProfileNavEvent.NavigateToHelp)
            ProfileEvent.SignOutClicked -> _state.update { it.copy(showSignOutDialog = true) }
            ProfileEvent.SignOutConfirmed -> performSignOut()
            ProfileEvent.SignOutDismissed -> _state.update { it.copy(showSignOutDialog = false) }
            ProfileEvent.Refresh -> loadProfile()
            ProfileEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getCurrentUser()
                .onSuccess { user ->
                    _state.update { it.copy(user = user, isLoading = false) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.message)
                    }
                }

            getUserStats()
                .onSuccess { stats ->
                    _state.update { it.copy(stats = stats) }
                }
        }
    }

    private fun performSignOut() {
        viewModelScope.launch {
            _state.update { it.copy(isSigningOut = true, showSignOutDialog = false) }
            signOut()
                .onSuccess {
                    _state.update { it.copy(isSigningOut = false) }
                    _navEvents.send(ProfileNavEvent.NavigateToAuth)
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isSigningOut = false,
                            error = err.message ?: "Failed to sign out"
                        )
                    }
                }
        }
    }

    private fun emitNav(event: ProfileNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }
}
