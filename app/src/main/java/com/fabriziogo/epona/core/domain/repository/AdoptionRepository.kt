package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner

interface AdoptionRepository {
    suspend fun getAdoptionPosts(limit: Int = 50, offset: Int = 0): Result<List<AdoptionPostWithOwner>>
    suspend fun getAdoptionPost(postId: String): Result<AdoptionPostWithOwner>
    suspend fun getMyAdoptionPosts(): Result<List<AdoptionPostWithOwner>>
    suspend fun createAdoptionPost(post: AdoptionPost): Result<AdoptionPost>
    suspend fun markAdopted(postId: String): Result<Unit>
    suspend fun deleteAdoptionPost(postId: String): Result<Unit>
}
