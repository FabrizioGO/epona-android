package com.fabriziogo.epona.feature.auth.register

data class RegisterUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val error: String? = null,
    /**
     * Set when the account was created but Supabase requires the emailed
     * confirmation link before a session exists. Holds the address it was sent to.
     */
    val pendingConfirmationEmail: String? = null
)