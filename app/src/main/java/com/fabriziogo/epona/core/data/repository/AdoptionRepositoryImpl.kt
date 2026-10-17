package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.data.mapper.toEntity
import com.fabriziogo.epona.core.data.mapper.toInsertDto
import com.fabriziogo.epona.core.database.dao.AdoptionPostDao
import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.repository.AdoptionRepository
import com.fabriziogo.epona.core.network.service.AdoptionService
import com.fabriziogo.epona.core.network.service.AuthService
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** Network first, Room as the offline fallback — same shape as AlertRepositoryImpl. */
@Singleton
class AdoptionRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val adoptionService: AdoptionService,
    private val adoptionDao: AdoptionPostDao
) : AdoptionRepository {

    override suspend fun getAdoptionPosts(limit: Int, offset: Int): Result<List<AdoptionPostWithOwner>> =
        runCatching {
            authService.awaitReady()
            val dtos = adoptionService.getActivePosts(limit, offset)
            cache { adoptionDao.upsertAll(dtos.map { it.toEntity() }) }
            dtos.map { it.toDomain() }
        }.recoverCatching { networkError ->
            val cached = adoptionDao.getActive(limit, offset)
            if (cached.isEmpty()) throw networkError
            cached.map { it.toDomain() }
        }

    override suspend fun getAdoptionPost(postId: String): Result<AdoptionPostWithOwner> =
        runCatching {
            authService.awaitReady()
            val dto = adoptionService.getPost(postId)
            cache { adoptionDao.upsert(dto.toEntity()) }
            dto.toDomain()
        }.recoverCatching { networkError ->
            adoptionDao.getById(postId)?.toDomain() ?: throw networkError
        }

    override suspend fun getMyAdoptionPosts(): Result<List<AdoptionPostWithOwner>> =
        runCatching {
            val userId = authService.requireUserId()
            val dtos = adoptionService.getMyPosts(userId)
            cache { adoptionDao.upsertAll(dtos.map { it.toEntity() }) }
            dtos.map { it.toDomain() }
        }.recoverCatching { networkError ->
            val cached = adoptionDao.getMine(authService.requireUserId())
            if (cached.isEmpty()) throw networkError
            cached.map { it.toDomain() }
        }

    override suspend fun createAdoptionPost(post: AdoptionPost): Result<AdoptionPost> =
        runCatching {
            val userId = authService.requireUserId()
            val dto = adoptionService.createPost(post.toInsertDto(userId))
            cache { adoptionDao.upsert(dto.toEntity()) }
            dto.toDomain().post
        }

    override suspend fun markAdopted(postId: String): Result<Unit> =
        runCatching {
            adoptionService.markAdopted(postId)
            cache { adoptionDao.markAdopted(postId, System.currentTimeMillis()) }
        }

    override suspend fun deleteAdoptionPost(postId: String): Result<Unit> =
        runCatching {
            adoptionService.deletePost(postId)
            cache { adoptionDao.delete(postId) }
        }

    /** The cache is an optimisation: a failed write must not turn a server success into an error. */
    private suspend inline fun cache(block: () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Failed to cache adoption posts locally")
        }
    }
}
