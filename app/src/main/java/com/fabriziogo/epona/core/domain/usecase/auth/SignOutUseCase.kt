package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        authRepo.signOut()
}