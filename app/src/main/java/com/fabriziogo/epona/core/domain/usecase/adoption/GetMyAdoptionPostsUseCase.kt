package com.fabriziogo.epona.core.domain.usecase.adoption

import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.repository.AdoptionRepository
import javax.inject.Inject

class GetMyAdoptionPostsUseCase @Inject constructor(
    private val repository: AdoptionRepository
) {
    suspend operator fun invoke(): Result<List<AdoptionPostWithOwner>> =
        repository.getMyAdoptionPosts()
}
