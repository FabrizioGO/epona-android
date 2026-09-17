package com.fabriziogo.epona.feature.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.usecase.auth.SignUpUseCase
import com.fabriziogo.epona.feature.auth.login.mapAuthError
import com.fabriziogo.epona.feature.auth.login.validateDisplayName
import com.fabriziogo.epona.feature.auth.login.validateEmail
import com.fabriziogo.epona.feature.auth.login.validatePassword
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
class RegisterViewModel @Inject constructor(
    private val signUp: SignUpUseCase,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    private val _navEvents = Channel<RegisterNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.FirstNameChanged -> {
                _state.update { it.copy(firstName = event.name, firstNameError = null) }
            }
            is RegisterEvent.LastNameChanged -> {
                _state.update { it.copy(lastName = event.name, lastNameError = null) }
            }
            is RegisterEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            is RegisterEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null) }
            }
            is RegisterEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.password, confirmPasswordError = null) }
            }
            is RegisterEvent.SignUpClicked -> signUpWithEmail()
            is RegisterEvent.GoogleSignInResult -> signInWithGoogle(event.idToken)
            is RegisterEvent.GoogleSignInFailed -> {
                _state.update { it.copy(isGoogleLoading = false, error = "Google sign-in failed") }
            }
            is RegisterEvent.NavigateToLogin -> {
                viewModelScope.launch { _navEvents.send(RegisterNavEvent.NavigateToLogin) }
            }
            is RegisterEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
            is RegisterEvent.ConfirmationAcknowledged -> {
                _state.update { it.copy(pendingConfirmationEmail = null) }
                viewModelScope.launch { _navEvents.send(RegisterNavEvent.NavigateToLogin) }
            }
        }
    }

    private fun signUpWithEmail() {
        val s = _state.value
        val firstNameErr = validateDisplayName(s.firstName)
        val lastNameErr = validateDisplayName(s.lastName)
        val emailErr = validateEmail(s.email)
        val passErr = validatePassword(s.password)
        val confirmErr = when {
            s.confirmPassword.isBlank() -> "Please confirm your password"
            s.confirmPassword != s.password -> "Passwords don't match"
            else -> null
        }

        if (firstNameErr != null || lastNameErr != null || emailErr != null ||
            passErr != null || confirmErr != null
        ) {
            _state.update {
                it.copy(
                    firstNameError = firstNameErr,
                    lastNameError = lastNameErr,
                    emailError = emailErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmErr
                )
            }
            return
        }

        val displayName = "${s.firstName.trim()} ${s.lastName.trim()}".trim()

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            signUp(s.email, s.password, displayName)
                .onSuccess { result ->
                    _state.update { it.copy(isLoading = false) }
                    when (result) {
                        is SignUpResult.SignedIn ->
                            _navEvents.send(RegisterNavEvent.NavigateToHome)
                        is SignUpResult.ConfirmationRequired ->
                            _state.update {
                                it.copy(pendingConfirmationEmail = result.email)
                            }
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = mapAuthError(err))
                    }
                }
        }
    }

    private fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isGoogleLoading = true, error = null) }
            authRepo.signInWithGoogle(idToken)
                .onSuccess {
                    _state.update { it.copy(isGoogleLoading = false) }
                    _navEvents.send(RegisterNavEvent.NavigateToHome)
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isGoogleLoading = false, error = mapAuthError(err))
                    }
                }
        }
    }
}