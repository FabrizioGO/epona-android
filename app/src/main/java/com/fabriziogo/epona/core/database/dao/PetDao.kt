package com.fabriziogo.epona.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.fabriziogo.epona.core.database.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    @Query("SELECT * FROM pets WHERE owner_id = :ownerId ORDER BY created_at DESC")
    fun observeMyPets(ownerId: String): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE owner_id = :ownerId ORDER BY created_at DESC")
    suspend fun getMyPets(ownerId: String): List<PetEntity>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPet(petId: String): PetEntity?

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    fun observePet(petId: String): Flow<PetEntity?>

    // Upsert rather than INSERT OR REPLACE: `pets` is the parent of `alerts` with
    // ON DELETE CASCADE, and REPLACE deletes the conflicting row before re-inserting it,
    // which silently dropped every cached alert for the pet being refreshed.
    @Upsert
    suspend fun insertPet(pet: PetEntity)

    @Upsert
    suspend fun insertPets(pets: List<PetEntity>)

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Query("""
        UPDATE pets
        SET photo_urls = :photoUrls, updated_at = :updatedAt
        WHERE id = :petId
    """)
    suspend fun updatePhotoUrls(
        petId: String,
        photoUrls: List<String>,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM pets WHERE id = :petId")
    suspend fun deletePet(petId: String)

    @Query("DELETE FROM pets WHERE owner_id = :ownerId")
    suspend fun deleteAllForOwner(ownerId: String)
}