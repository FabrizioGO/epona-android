package com.fabriziogo.epona.feature.auth.forgotpassword

sealed interface ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent
    data class CodeChanged(val code: String) : ForgotPasswordEvent
    data class NewPasswordChanged(val password: String) : ForgotPasswordEvent
    data class ConfirmPasswordChanged(val password: String) : ForgotPasswordEvent
    data object SendCodeClicked : ForgotPasswordEvent
    data object ResendCodeClicked : ForgotPasswordEvent
    data object VerifyCodeClicked : ForgotPasswordEvent
    data object SavePasswordClicked : ForgotPasswordEvent
    data object BackClicked : ForgotPasswordEvent
    data object ErrorDismissed : ForgotPasswordEvent
}

sealed interface ForgotPasswordNavEvent {
    data object NavigateBack : ForgotPasswordNavEvent
    data object NavigateToHome : ForgotPasswordNavEvent
}
