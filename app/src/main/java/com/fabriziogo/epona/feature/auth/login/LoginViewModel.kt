package com.fabriziogo.epona.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.auth.SignInUseCase
import com.fabriziogo.epona.core.domain.repository.AuthRepository
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
class LoginViewModel @Inject constructor(
    private val signIn: SignInUseCase,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _navEvents = Channel<LoginNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, emailError = null) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, passwordError = null) }
            }
            is LoginEvent.SignInClicked -> signInWithEmail()
            is LoginEvent.GoogleSignInResult -> signInWithGoogle(event.idToken)
            is LoginEvent.GoogleSignInFailed -> {
                _state.update { it.copy(isGoogleLoading = false, error = "Google sign-in failed") }
            }
            is LoginEvent.NavigateToRegister -> {
                viewModelScope.launch { _navEvents.send(LoginNavEvent.NavigateToRegister) }
            }
            is LoginEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun signInWithEmail() {
        val s = _state.value
        val emailErr = validateEmail(s.email)
        val passErr = validatePassword(s.password)

        if (emailErr != null || passErr != null) {
            _state.update { it.copy(emailError = emailErr, passwordError = passErr) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            signIn(s.email, s.password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _navEvents.send(LoginNavEvent.NavigateToHome)
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = mapAuthError(err)
                        )
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
                    _navEvents.send(LoginNavEvent.NavigateToHome)
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isGoogleLoading = false,
                            error = mapAuthError(err)
                        )
                    }
                }
        }
    }
}

// Shared validation & error mapping
internal fun validateEmail(email: String): String? = when {
    email.isBlank() -> "Email is required"
    !email.contains("@") || !email.contains(".") -> "Enter a valid email"
    else -> null
}

internal fun validatePassword(password: String): String? = when {
    password.isBlank() -> "Password is required"
    password.length < 6 -> "Must be at least 6 characters"
    else -> null
}

internal fun validateDisplayName(name: String): String? = when {
    name.isBlank() -> "Name is required"
    name.length < 2 -> "Must be at least 2 characters"
    else -> null
}

internal fun mapAuthError(err: Throwable): String = when {
    err.message?.contains("invalid_credentials", true) == true ->
        "Incorrect email or password"
    err.message?.contains("email_not_confirmed", true) == true ->
        "Please verify your email first"
    err.message?.contains("user_already_exists", true) == true ->
        "An account with this email already exists"
    err.message?.contains("weak_password", true) == true ->
        "Password is too weak"
    err.message?.contains("network", true) == true ->
        "No internet connection. Please try again."
    else -> err.message ?: "Something went wrong. Please try again."
}