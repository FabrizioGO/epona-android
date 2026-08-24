package com.fabriziogo.epona.core.domain.usecase.pet

import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.repository.PetRepository
import javax.inject.Inject

class CreatePetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(pet: Pet): Result<Pet> {
        require(pet.name.isNotBlank()) { "Pet name is required" }
        return petRepository.registerPet(pet)
    }
}
