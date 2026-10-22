package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.BlockedUser

interface BlockRepository {

    /**
     * Blocks [userId]. Blocking someone already blocked succeeds -- see the
     * implementation. On success their content is also dropped from the local cache.
     */
    suspend fun blockUser(userId: String): Result<Unit>

    suspend fun unblockUser(userId: String): Result<Unit>

    suspend fun getBlockedUsers(): Result<List<BlockedUser>>
}
