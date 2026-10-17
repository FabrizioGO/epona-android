package com.fabriziogo.epona.feature.auth.register

import com.fabriziogo.epona.core.ui.text.UiText

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstNameError: Int? = null,
    val lastNameError: Int? = null,
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val confirmPasswordError: Int? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val acceptedTerms: Boolean = false,
    val error: UiText? = null,
    /**
     * Set when the account was created but Supabase requires the emailed
     * confirmation link before a session exists. Holds the address it was sent to.
     */
    val pendingConfirmationEmail: String? = null
)