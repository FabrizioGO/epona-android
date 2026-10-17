package com.fabriziogo.epona.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fabriziogo.epona.core.database.entity.AdoptionPostEntity

@Dao
interface AdoptionPostDao {

    @Query("SELECT * FROM adoption_posts WHERE status = 'active' ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    suspend fun getActive(limit: Int, offset: Int): List<AdoptionPostEntity>

    @Query("SELECT * FROM adoption_posts WHERE id = :postId LIMIT 1")
    suspend fun getById(postId: String): AdoptionPostEntity?

    @Query("SELECT * FROM adoption_posts WHERE user_id = :userId ORDER BY created_at DESC")
    suspend fun getMine(userId: String): List<AdoptionPostEntity>

    @Upsert
    suspend fun upsert(post: AdoptionPostEntity)

    @Upsert
    suspend fun upsertAll(posts: List<AdoptionPostEntity>)

    @Query("UPDATE adoption_posts SET status = 'adopted', adopted_at = :adoptedAt, updated_at = :adoptedAt WHERE id = :postId")
    suspend fun markAdopted(postId: String, adoptedAt: Long)

    @Query("DELETE FROM adoption_posts WHERE id = :postId")
    suspend fun delete(postId: String)
}
