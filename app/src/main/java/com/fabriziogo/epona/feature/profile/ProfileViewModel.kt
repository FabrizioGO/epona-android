package com.fabriziogo.epona.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.auth.GetCurrentUserUseCase
import com.fabriziogo.epona.core.domain.usecase.auth.SignOutUseCase
import com.fabriziogo.epona.core.domain.usecase.user.GetUserStatsUseCase
import com.fabriziogo.epona.core.domain.usecase.user.UpdateAvatarUseCase
import com.fabriziogo.epona.core.media.ImageProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getUserStats: GetUserStatsUseCase,
    private val updateAvatar: UpdateAvatarUseCase,
    private val imageProcessor: ImageProcessor,
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
            is ProfileEvent.AvatarPicked -> updateAvatarPhoto(event.uri)
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

    /**
     * Same photo pipeline as the pet form: decode on this scope (disk work that
     * must survive rotation), show the cache copy optimistically, then upload to
     * the avatars bucket and persist the public URL on the profile.
     */
    private fun updateAvatarPhoto(source: Uri) {
        if (_state.value.isUploadingAvatar) {
            Timber.d("Profile: avatar upload ignored, one is already in flight")
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isUploadingAvatar = true, error = null) }

            val local = imageProcessor.process(source).getOrElse { err ->
                Timber.e(err, "Profile: avatar processing failed")
                _state.update {
                    it.copy(isUploadingAvatar = false, error = AVATAR_PROCESS_ERROR)
                }
                return@launch
            }
            _state.update { it.copy(avatarPreviewUri = local.uri) }

            updateAvatar(local.uri)
                .onSuccess { user ->
                    Timber.d("Profile: avatar updated")
                    // Uploaded and persisted, so the cache copy is dead weight.
                    imageProcessor.delete(listOf(local.uri))
                    _state.update {
                        it.copy(user = user, avatarPreviewUri = null, isUploadingAvatar = false)
                    }
                }
                .onFailure { err ->
                    Timber.e(err, "Profile: avatar upload failed")
                    imageProcessor.delete(listOf(local.uri))
                    _state.update {
                        it.copy(
                            avatarPreviewUri = null,
                            isUploadingAvatar = false,
                            error = err.message ?: AVATAR_UPLOAD_ERROR
                        )
                    }
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

    private companion object {
        const val AVATAR_PROCESS_ERROR = "That photo could not be opened. Try another one."
        const val AVATAR_UPLOAD_ERROR = "Could not update your photo. Please try again."
    }
}
