package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.repository.AuthRepository
import javax.inject.Inject

class UpdatePasswordUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    suspend operator fun invoke(newPassword: String): Result<Unit> {
        require(newPassword.length >= 6) { "Password must be at least 6 characters" }
        return authRepo.updatePassword(newPassword)
    }
}
