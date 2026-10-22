package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.domain.model.BlockedUser
import com.fabriziogo.epona.core.network.dto.BlockedUserDto

fun BlockedUserDto.toDomain(): BlockedUser = BlockedUser(
    id = blockedId,
    displayName = displayName.orEmpty(),
    avatarUrl = avatarUrl
)
