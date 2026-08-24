package com.fabriziogo.epona.core.domain.usecase.pet

import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.repository.PetRepository
import javax.inject.Inject

class UpdatePetUseCase @Inject constructor(
    private val petRepo: PetRepository
) {
    suspend operator fun invoke(pet: Pet): Result<Pet> {
        require(pet.id.isNotBlank()) { "Pet ID required for update" }
        require(pet.name.isNotBlank()) { "Pet name cannot be empty" }
        return petRepo.updatePet(pet)
    }
}