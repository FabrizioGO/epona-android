package com.fabriziogo.epona.core.domain.usecase.pet

import com.fabriziogo.epona.core.domain.repository.PetRepository
import javax.inject.Inject

class DeletePetUseCase @Inject constructor(
    private val petRepo: PetRepository
) {
    suspend operator fun invoke(petId: String): Result<Unit> {
        require(petId.isNotBlank()) { "Pet ID cannot be empty" }
        return petRepo.deletePet(petId)
    }
}