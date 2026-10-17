package com.fabriziogo.epona.core.domain.usecase.adoption

import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.repository.AdoptionRepository
import javax.inject.Inject

class GetAdoptionPostDetailUseCase @Inject constructor(
    private val repository: AdoptionRepository
) {
    suspend operator fun invoke(postId: String): Result<AdoptionPostWithOwner> {
        require(postId.isNotBlank()) { "Post id is required" }
        return repository.getAdoptionPost(postId)
    }
}
