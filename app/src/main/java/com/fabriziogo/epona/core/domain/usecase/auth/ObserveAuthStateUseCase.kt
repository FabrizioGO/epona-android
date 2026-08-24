package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveAuthStateUseCase @Inject constructor(
    private val authRepo: AuthRepository
) {
    operator fun invoke(): Flow<Boolean> =
        authRepo.observeAuthState()
}