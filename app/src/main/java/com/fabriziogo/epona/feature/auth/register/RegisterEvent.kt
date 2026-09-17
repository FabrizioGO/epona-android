package com.fabriziogo.epona.feature.auth.register

sealed interface RegisterEvent {
    data class FirstNameChanged(val name: String) : RegisterEvent
    data class LastNameChanged(val name: String) : RegisterEvent
    data class EmailChanged(val email: String) : RegisterEvent
    data class PasswordChanged(val password: String) : RegisterEvent
    data class ConfirmPasswordChanged(val password: String) : RegisterEvent
    data object SignUpClicked : RegisterEvent
    data class GoogleSignInResult(val idToken: String) : RegisterEvent
    data object GoogleSignInFailed : RegisterEvent
    data object NavigateToLogin : RegisterEvent
    data object ErrorDismissed : RegisterEvent
    data object ConfirmationAcknowledged : RegisterEvent
}

sealed interface RegisterNavEvent {
    data object NavigateToHome : RegisterNavEvent
    data object NavigateToLogin : RegisterNavEvent
}