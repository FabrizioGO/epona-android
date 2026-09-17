package com.fabriziogo.epona.core.domain.usecase.user

import com.fabriziogo.epona.core.domain.repository.UserRepository
import javax.inject.Inject

class SyncUserLocaleUseCase @Inject constructor(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> = userRepo.syncLocale()
}
