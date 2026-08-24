package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User> {
        require(email.isNotBlank()) { "Email cannot be empty" }
        require(password.length >= 6) { "Password must be at least 6 characters" }
        return authRepo.signInWithEmail(email.trim(), password)
    }
}