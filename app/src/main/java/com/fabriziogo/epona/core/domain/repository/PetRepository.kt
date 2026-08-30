package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.Pet
import kotlinx.coroutines.flow.Flow

interface PetRepository {

    fun observeMyPets(): Flow<List<Pet>>

    suspend fun getPet(petId: String): Result<Pet>

    suspend fun registerPet(pet: Pet): Result<Pet>

    suspend fun updatePet(pet: Pet): Result<Pet>

    suspend fun deletePet(petId: String): Result<Unit>

    /**
     * Uploads locally picked images and returns their public URLs, in the order
     * they were given. Writes nothing to the pets table — the caller decides which
     * row the URLs end up on, which is what lets this run before the pet exists.
     */
    suspend fun uploadPetPhotos(localUris: List<String>): Result<List<String>>
}
