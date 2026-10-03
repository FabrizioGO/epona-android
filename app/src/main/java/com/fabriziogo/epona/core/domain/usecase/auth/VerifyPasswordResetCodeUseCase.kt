package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.repository.AuthRepository
import javax.inject.Inject

class VerifyPasswordResetCodeUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    suspend operator fun invoke(email: String, code: String): Result<Unit> {
        require(email.isNotBlank()) { "Email cannot be empty" }
        require(code.isNotBlank()) { "Code cannot be empty" }
        return authRepo.verifyPasswordResetCode(email.trim(), code.trim())
    }
}
