package com.fabriziogo.epona.feature.auth.login

import androidx.annotation.StringRes
import com.fabriziogo.epona.core.ui.text.UiText

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val error: UiText? = null
)