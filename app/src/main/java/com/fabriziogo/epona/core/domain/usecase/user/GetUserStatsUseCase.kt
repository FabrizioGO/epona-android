package com.fabriziogo.epona.core.domain.usecase.user

import com.fabriziogo.epona.core.domain.model.UserStats
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.repository.UserRepository
import javax.inject.Inject

class GetUserStatsUseCase @Inject constructor(
    private val authRepo: AuthRepository,
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(): Result<UserStats> {
        val userId = authRepo.awaitUserId()
            ?: return Result.failure(IllegalStateException("Not authenticated"))
        return userRepo.getUserStats(userId)
    }
}