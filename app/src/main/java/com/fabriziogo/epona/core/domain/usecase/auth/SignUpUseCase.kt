package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.model.LegalTerms
import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String,
        acceptedTerms: Boolean
    ): Result<SignUpResult> {
        val validationError = when {
            email.isBlank() -> "Email cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            displayName.isBlank() -> "Display name cannot be empty"
            !acceptedTerms -> "You must agree to the Terms of Service and Privacy Policy"
            else -> null
        }
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }
        return authRepo.signUpWithEmail(
            email.trim(),
            password,
            displayName.trim(),
            acceptedTermsVersion = LegalTerms.VERSION
        )
    }
}
