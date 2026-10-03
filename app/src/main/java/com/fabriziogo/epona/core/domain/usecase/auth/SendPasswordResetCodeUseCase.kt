package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.repository.AuthRepository
import javax.inject.Inject

class SendPasswordResetCodeUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    suspend operator fun invoke(email: String): Result<Unit> {
        require(email.isNotBlank()) { "Email cannot be empty" }
        return authRepo.sendPasswordResetCode(email.trim())
    }
}
