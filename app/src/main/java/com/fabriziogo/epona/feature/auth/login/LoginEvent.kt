package com.fabriziogo.epona.feature.auth.login

sealed interface LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent
    data class PasswordChanged(val password: String) : LoginEvent
    data object SignInClicked : LoginEvent
    data class GoogleSignInResult(val idToken: String) : LoginEvent
    data object GoogleSignInFailed : LoginEvent
    data object NavigateToRegister : LoginEvent
    data object ErrorDismissed : LoginEvent
}

sealed interface LoginNavEvent {
    data object NavigateToHome : LoginNavEvent
    data object NavigateToRegister : LoginNavEvent
    data object LaunchGoogleSignIn : LoginNavEvent
}