package com.fabriziogo.epona.feature.auth.login

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.usecase.auth.SignInUseCase
import com.fabriziogo.epona.core.ui.text.UiText
import com.fabriziogo.epona.core.ui.text.uiTextOf
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
                _state.update { it.copy(isGoogleLoading = false, error = uiTextOf(R.string.error_google_sign_in)) }
            }
            is LoginEvent.NavigateToRegister -> {
                viewModelScope.launch { _navEvents.send(LoginNavEvent.NavigateToRegister) }
            }
            is LoginEvent.ForgotPasswordClicked -> {
                val email = _state.value.email.trim()
                viewModelScope.launch {
                    _navEvents.send(LoginNavEvent.NavigateToForgotPassword(email))
                }
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
@StringRes
internal fun validateEmail(email: String): Int? = when {
    email.isBlank() -> R.string.error_email_required
    !email.contains("@") || !email.contains(".") -> R.string.error_email_invalid
    else -> null
}

@StringRes
internal fun validatePassword(password: String): Int? = when {
    password.isBlank() -> R.string.error_password_required
    password.length < 6 -> R.string.error_password_short
    else -> null
}

// For passwords being set (sign-up, reset). Matches the project's server-side
// minimum_password_length; sign-in keeps the old 6 so existing accounts still work.
@StringRes
internal fun validateNewPassword(password: String): Int? = when {
    password.isBlank() -> R.string.error_password_required
    password.length < 8 -> R.string.error_password_short_new
    else -> null
}

@StringRes
internal fun validateDisplayName(name: String): Int? = when {
    name.isBlank() -> R.string.error_name_required
    name.length < 2 -> R.string.error_name_short
    else -> null
}

/**
 * Known backend codes become localized text; anything else keeps the server's own message,
 * with a generic localized fallback when there is none.
 */
internal fun mapAuthError(err: Throwable): UiText {
    val message = err.message
    fun has(code: String) = message?.contains(code, true) == true
    return when {
        has("invalid_credentials") -> uiTextOf(R.string.error_invalid_credentials)
        has("email_not_confirmed") -> uiTextOf(R.string.error_email_not_confirmed)
        has("user_already_exists") -> uiTextOf(R.string.error_user_exists)
        has("weak_password") -> uiTextOf(R.string.error_weak_password)
        has("otp_expired") || has("token has expired or is invalid") ->
            uiTextOf(R.string.error_code_invalid)
        has("same_password") -> uiTextOf(R.string.error_password_same_as_old)
        has("rate_limit") -> uiTextOf(R.string.error_rate_limited)
        has("network") -> uiTextOf(R.string.error_network)
        else -> uiTextOf(message, R.string.error_generic)
    }
}
