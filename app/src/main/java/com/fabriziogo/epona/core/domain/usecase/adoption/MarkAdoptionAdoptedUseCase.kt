package com.fabriziogo.epona.core.domain.usecase.adoption

import com.fabriziogo.epona.core.domain.repository.AdoptionRepository
import javax.inject.Inject

class MarkAdoptionAdoptedUseCase @Inject constructor(
    private val repository: AdoptionRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        require(postId.isNotBlank()) { "Post id is required" }
        return repository.markAdopted(postId)
    }
}
