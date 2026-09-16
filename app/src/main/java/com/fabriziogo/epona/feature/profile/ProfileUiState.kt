package com.fabriziogo.epona.feature.profile

import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.model.UserStats

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val stats: UserStats = UserStats(),
    val showSignOutDialog: Boolean = false,
    val isSigningOut: Boolean = false,
    /**
     * Local cache URI of the avatar being uploaded. Shown optimistically while
     * the upload + profile write are in flight; cleared once they settle.
     */
    val avatarPreviewUri: String? = null,
    val isUploadingAvatar: Boolean = false,
    val error: String? = null
)
