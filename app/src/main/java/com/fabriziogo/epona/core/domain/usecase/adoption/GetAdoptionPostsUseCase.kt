package com.fabriziogo.epona.core.domain.usecase.adoption

import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.repository.AdoptionRepository
import javax.inject.Inject

class GetAdoptionPostsUseCase @Inject constructor(
    private val repository: AdoptionRepository
) {
    suspend operator fun invoke(limit: Int = 50, offset: Int = 0): Result<List<AdoptionPostWithOwner>> =
        repository.getAdoptionPosts(limit, offset)
}
