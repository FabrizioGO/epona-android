package com.fabriziogo.epona.core.domain.usecase.user

import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.model.isLocalPhotoUri
import com.fabriziogo.epona.core.domain.repository.UserRepository
import javax.inject.Inject

class UpdateAvatarUseCase @Inject constructor(
    private val userRepo: UserRepository
) {
    /**
     * [avatarUri] may be a local device URI (picked this session, already processed
     * into the app cache by ImageProcessor) or an already-remote URL. Local URIs
     * are uploaded to the avatars bucket first — the bucket path names the owner,
     * so no profile row needs to exist yet — and only the public URL is written.
     * A failure before the profile write leaves no half-updated user behind.
     */
    suspend operator fun invoke(avatarUri: String): Result<User> {
        require(avatarUri.isNotBlank()) { "Avatar image is required" }

        val remoteUrl = if (avatarUri.isLocalPhotoUri()) {
            userRepo.uploadAvatar(avatarUri)
                .getOrElse { return Result.failure(it) }
        } else {
            avatarUri
        }

        return userRepo.updateProfile(avatarUrl = remoteUrl)
    }
}
