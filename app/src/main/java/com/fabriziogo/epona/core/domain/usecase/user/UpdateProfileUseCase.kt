package com.fabriziogo.epona.core.domain.usecase.user

import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.repository.UserRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(
        displayName: String? = null,
        phone: String? = null,
        avatarUrl: String? = null
    ): Result<User> {
        displayName?.let {
            require(it.isNotBlank()) { "Display name cannot be empty" }
        }
        return userRepo.updateProfile(
            displayName = displayName?.trim(),
            phone = phone?.trim(),
            avatarUrl = avatarUrl
        )
    }
}