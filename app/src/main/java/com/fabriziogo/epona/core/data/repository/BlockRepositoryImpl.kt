package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.database.dao.AdoptionPostDao
import com.fabriziogo.epona.core.database.dao.AlertDao
import com.fabriziogo.epona.core.database.dao.SightingDao
import com.fabriziogo.epona.core.domain.model.BlockedUser
import com.fabriziogo.epona.core.domain.repository.BlockRepository
import com.fabriziogo.epona.core.network.dto.BlockUserInsertParams
import com.fabriziogo.epona.core.network.service.BlockService
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockRepositoryImpl @Inject constructor(
    private val blockService: BlockService,
    private val alertDao: AlertDao,
    private val sightingDao: SightingDao,
    private val adoptionPostDao: AdoptionPostDao
) : BlockRepository {

    override suspend fun blockUser(userId: String): Result<Unit> = runCatching {
        blockService.block(BlockUserInsertParams(blockedId = userId))
    }.recoverCatching { err ->
        // 23505 = unique_violation on the (blocker_id, blocked_id) primary key: this
        // user is already blocked, which is the outcome the caller asked for.
        if (err is PostgrestRestException && err.code == "23505") Unit else throw err
    }.onSuccess {
        // The server now filters them out, but the offline fallback reads Room.
        runCatching {
            alertDao.deleteByUser(userId)
            sightingDao.deleteByReporter(userId)
            adoptionPostDao.deleteByUser(userId)
        }
    }

    override suspend fun unblockUser(userId: String): Result<Unit> = runCatching {
        blockService.unblock(userId)
    }

    override suspend fun getBlockedUsers(): Result<List<BlockedUser>> = runCatching {
        blockService.getBlockedUsers().map { it.toDomain() }
    }
}
