package com.fabriziogo.epona.core.domain.usecase.pet

import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.repository.PetRepository
import javax.inject.Inject

class GetPetUseCase @Inject constructor(
    private val petRepo: PetRepository
) {
    suspend operator fun invoke(petId: String): Result<Pet> {
        require(petId.isNotBlank()) { "Pet ID cannot be empty" }
        return petRepo.getPet(petId)
    }
}
