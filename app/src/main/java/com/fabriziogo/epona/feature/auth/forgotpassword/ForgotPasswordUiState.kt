package com.fabriziogo.epona.feature.auth.forgotpassword

enum class ForgotPasswordStep { EnterEmail, EnterCode, NewPassword }

data class ForgotPasswordUiState(
    val step: ForgotPasswordStep = ForgotPasswordStep.EnterEmail,
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val emailError: String? = null,
    val codeError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val resendCooldownSeconds: Int = 0,
    val error: String? = null
)
