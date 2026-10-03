package com.fabriziogo.epona.feature.auth.forgotpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.auth.SendPasswordResetCodeUseCase
import com.fabriziogo.epona.core.domain.usecase.auth.SignOutUseCase
import com.fabriziogo.epona.core.domain.usecase.auth.UpdatePasswordUseCase
import com.fabriziogo.epona.core.domain.usecase.auth.VerifyPasswordResetCodeUseCase
import com.fabriziogo.epona.feature.auth.login.mapAuthError
import com.fabriziogo.epona.feature.auth.login.validateEmail
import com.fabriziogo.epona.feature.auth.login.validatePassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val FORGOT_PASSWORD_EMAIL_ARG = "email"

// Supabase's OTP length is a project setting (6-10); the hosted project sends 8 digits
// while local config.toml says 6, so accept the range instead of hardcoding one value.
const val RESET_CODE_MIN_LENGTH = 6
const val RESET_CODE_MAX_LENGTH = 8
private const val RESEND_COOLDOWN_SECONDS = 60

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sendCode: SendPasswordResetCodeUseCase,
    private val verifyCode: VerifyPasswordResetCodeUseCase,
    private val updatePassword: UpdatePasswordUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        ForgotPasswordUiState(email = savedStateHandle[FORGOT_PASSWORD_EMAIL_ARG] ?: "")
    )
    val state: StateFlow<ForgotPasswordUiState> = _state.asStateFlow()

    private val _navEvents = Channel<ForgotPasswordNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    private var cooldownJob: Job? = null

    fun onEvent(event: ForgotPasswordEvent) {
        when (event) {
            is ForgotPasswordEvent.EmailChanged ->
                _state.update { it.copy(email = event.email, emailError = null) }
            is ForgotPasswordEvent.CodeChanged ->
                _state.update {
                    it.copy(
                        code = event.code.filter(Char::isDigit).take(RESET_CODE_MAX_LENGTH),
                        codeError = null
                    )
                }
            is ForgotPasswordEvent.NewPasswordChanged ->
                _state.update { it.copy(newPassword = event.password, newPasswordError = null) }
            is ForgotPasswordEvent.ConfirmPasswordChanged ->
                _state.update {
                    it.copy(confirmPassword = event.password, confirmPasswordError = null)
                }
            is ForgotPasswordEvent.SendCodeClicked -> sendResetCode(advanceToCodeStep = true)
            is ForgotPasswordEvent.ResendCodeClicked -> {
                if (_state.value.resendCooldownSeconds == 0) {
                    sendResetCode(advanceToCodeStep = false)
                }
            }
            is ForgotPasswordEvent.VerifyCodeClicked -> verifyResetCode()
            is ForgotPasswordEvent.SavePasswordClicked -> savePassword()
            is ForgotPasswordEvent.BackClicked -> back()
            is ForgotPasswordEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun sendResetCode(advanceToCodeStep: Boolean) {
        val s = _state.value
        val emailErr = validateEmail(s.email)
        if (emailErr != null) {
            _state.update { it.copy(emailError = emailErr) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            sendCode(s.email)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            step = if (advanceToCodeStep) ForgotPasswordStep.EnterCode else it.step,
                            code = ""
                        )
                    }
                    startCooldown()
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = mapAuthError(err)) }
                }
        }
    }

    private fun verifyResetCode() {
        val s = _state.value
        if (s.code.length !in RESET_CODE_MIN_LENGTH..RESET_CODE_MAX_LENGTH) {
            _state.update { it.copy(codeError = "Enter the code from the email") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            verifyCode(s.email, s.code)
                .onSuccess {
                    _state.update {
                        it.copy(isLoading = false, step = ForgotPasswordStep.NewPassword)
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = mapAuthError(err)) }
                }
        }
    }

    private fun savePassword() {
        val s = _state.value
        val passErr = validatePassword(s.newPassword)
        val confirmErr = when {
            s.confirmPassword.isBlank() -> "Please confirm your password"
            s.confirmPassword != s.newPassword -> "Passwords don't match"
            else -> null
        }
        if (passErr != null || confirmErr != null) {
            _state.update {
                it.copy(newPasswordError = passErr, confirmPasswordError = confirmErr)
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            updatePassword(s.newPassword)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    _navEvents.send(ForgotPasswordNavEvent.NavigateToHome)
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = mapAuthError(err)) }
                }
        }
    }

    private fun back() {
        when (_state.value.step) {
            ForgotPasswordStep.EnterEmail ->
                viewModelScope.launch { _navEvents.send(ForgotPasswordNavEvent.NavigateBack) }
            ForgotPasswordStep.EnterCode ->
                _state.update {
                    it.copy(step = ForgotPasswordStep.EnterEmail, code = "", codeError = null)
                }
            // Verifying the code already signed the user in; leaving here without a new
            // password must not leave that recovery session behind.
            ForgotPasswordStep.NewPassword ->
                viewModelScope.launch {
                    signOut()
                    _navEvents.send(ForgotPasswordNavEvent.NavigateBack)
                }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (remaining in RESEND_COOLDOWN_SECONDS downTo 1) {
                _state.update { it.copy(resendCooldownSeconds = remaining) }
                delay(1_000)
            }
            _state.update { it.copy(resendCooldownSeconds = 0) }
        }
    }
}
