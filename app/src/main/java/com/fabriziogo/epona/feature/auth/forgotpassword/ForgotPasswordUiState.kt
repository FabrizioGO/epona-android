package com.fabriziogo.epona.feature.auth.forgotpassword

import com.fabriziogo.epona.core.ui.text.UiText

enum class ForgotPasswordStep { EnterEmail, EnterCode, NewPassword }

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.EnterEmail,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val emailError: Int? = null,
    val codeError: Int? = null,
    val newPasswordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val isLoading: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val error: UiText? = null
)
