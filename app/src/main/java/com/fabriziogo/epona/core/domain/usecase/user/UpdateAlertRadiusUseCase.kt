package com.fabriziogo.epona.core.domain.usecase.user

import com.fabriziogo.epona.core.domain.repository.UserRepository
import javax.inject.Inject

class UpdateAlertRadiusUseCase @Inject constructor(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(radiusKm: Int): Result<Unit> {
        require(radiusKm in 1..100) {
            "Alert radius must be between 1 and 100 km"
        }
        return userRepo.updateAlertRadius(radiusKm)
    }
}