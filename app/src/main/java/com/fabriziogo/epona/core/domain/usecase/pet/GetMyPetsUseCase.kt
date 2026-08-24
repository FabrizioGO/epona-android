package com.fabriziogo.epona.core.domain.usecase.pet

import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyPetsUseCase @Inject constructor(
    private val petRepo: PetRepository
) {
    operator fun invoke(): Flow<List<Pet>> =
        petRepo.observeMyPets()
}