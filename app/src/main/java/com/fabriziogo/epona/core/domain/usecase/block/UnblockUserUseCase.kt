package com.fabriziogo.epona.core.domain.usecase.block

import com.fabriziogo.epona.core.domain.repository.BlockRepository
import javax.inject.Inject

class UnblockUserUseCase @Inject constructor(
    private val blockRepository: BlockRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> =
        blockRepository.unblockUser(userId)
}
