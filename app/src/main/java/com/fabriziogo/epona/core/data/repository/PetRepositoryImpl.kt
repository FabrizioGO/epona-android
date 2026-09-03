package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.data.mapper.toDto
import com.fabriziogo.epona.core.data.mapper.toEntity
import com.fabriziogo.epona.core.data.mapper.toInsertDto
import com.fabriziogo.epona.core.database.dao.PetDao
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.repository.PetRepository
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.PetService
import com.fabriziogo.epona.core.data.media.PhotoUploader
import com.fabriziogo.epona.core.network.service.StorageService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val petService: PetService,
    private val photoUploader: PhotoUploader,
    private val petDao: PetDao
) : PetRepository {

    private suspend fun currentUserId(): String = authService.requireUserId()

    /**
     * Cached pets for the signed-in owner, kept fresh from the server.
     *
     * The Flow itself reads Room only, and [registerPet] is the sole other writer, so
     * without a refresh here a fresh install, a new device, or a destructive migration
     * shows an empty list while the pets sit on the server.
     */
    override fun observeMyPets(): Flow<List<Pet>> = channelFlow {
        val ownerId = currentUserId()

        val refresh = launch { refreshMyPets(ownerId) }

        // With rows already cached, show them now and let the refresh update them —
        // offline that means the list appears instead of waiting out a network timeout.
        // With nothing cached there is nothing worth showing, so wait for the fetch
        // rather than flashing the empty state.
        if (petDao.getMyPets(ownerId).isEmpty()) refresh.join()

        petDao.observeMyPets(ownerId)
            .map { entities -> entities.map { it.toDomain() } }
            .collect { pets -> send(pets) }
    }

    private suspend fun refreshMyPets(ownerId: String) {
        try {
            petDao.insertPets(petService.getMyPets(ownerId).map { it.toEntity() })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.w(e, "Pets refresh failed; falling back to the cached list")
        }
    }

    override suspend fun getPet(petId: String): Result<Pet> = runCatching {
        authService.awaitReady()
        val dto = petService.getPet(petId)
        petDao.insertPet(dto.toEntity())
        dto.toDomain()
    }.recoverCatching {
        petDao.getPet(petId)?.toDomain()
            ?: throw IllegalStateException("Pet not found")
    }

    override suspend fun registerPet(pet: Pet): Result<Pet> = runCatching {
        val ownerId = currentUserId()
        val dto = petService.insertPet(pet.toInsertDto(ownerId))
        petDao.insertPet(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun updatePet(pet: Pet): Result<Pet> = runCatching {
        val dto = petService.updatePet(pet.id, pet.toDto())
        petDao.insertPet(dto.toEntity())
        dto.toDomain()
    }

    override suspend fun deletePet(petId: String): Result<Unit> = runCatching {
        petService.deletePet(petId)
        petDao.deletePet(petId)
    }

    override suspend fun uploadPetPhotos(
        localUris: List<String>
    ): Result<List<String>> = runCatching {
        photoUploader.uploadAll(StorageService.BUCKET_PETS, localUris)
    }
}