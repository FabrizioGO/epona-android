package com.fabriziogo.epona.core.domain.usecase.block

import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.repository.BlockRepository
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val blockRepository: BlockRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        if (userId.isBlank()) {
            return Result.failure(IllegalArgumentException("User id must not be blank"))
        }
        if (userId == authRepository.currentUserId) {
            return Result.failure(IllegalArgumentException("You cannot block yourself"))
        }
        return blockRepository.blockUser(userId)
    }
}
