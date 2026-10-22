package com.fabriziogo.epona.core.domain.usecase.block

import com.fabriziogo.epona.core.domain.model.BlockedUser
import com.fabriziogo.epona.core.domain.repository.BlockRepository
import javax.inject.Inject

class GetBlockedUsersUseCase @Inject constructor(
    private val blockRepository: BlockRepository
) {
    suspend operator fun invoke(): Result<List<BlockedUser>> =
        blockRepository.getBlockedUsers()
}
