package com.fabriziogo.epona.feature.profile

import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.model.UserStats

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val stats: UserStats = UserStats(),
    val showSignOutDialog: Boolean = false,
    val isSigningOut: Boolean = false,
    val error: String? = null
)
