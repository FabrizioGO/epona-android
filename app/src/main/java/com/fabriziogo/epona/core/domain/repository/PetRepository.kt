package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {

    fun observeMyPets(): Flow<List<Pet>>

    suspend fun getPet(petId: String): Result<Pet>

    suspend fun registerPet(pet: Pet): Result<Pet>

    suspend fun updatePet(pet: Pet): Result<Pet>

    suspend fun deletePet(petId: String): Result<Unit>

    suspend fun uploadPetPhoto(
        petId: String,
        imageBytes: ByteArray,
        fileName: String
    ): Result<String>  // Returns photo URL
}