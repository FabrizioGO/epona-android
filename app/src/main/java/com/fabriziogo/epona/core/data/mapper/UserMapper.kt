package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.database.entity.UserEntity
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.network.dto.UserDto

fun UserDto.toDomain(): User = User(
    id = id,
    displayName = displayName,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    alertRadiusKm = alertRadiusKm,
    location = null,  // Location not returned in standard DTO
    createdAt = createdAt?.toEpochMillis() ?: 0L,
    updatedAt = updatedAt?.toEpochMillis() ?: 0L
)

fun UserDto.toEntity(): UserEntity = UserEntity(
    id = id,
    displayName = displayName,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    alertRadiusKm = alertRadiusKm,
    fcmToken = fcmToken,
    createdAt = createdAt?.toEpochMillis() ?: System.currentTimeMillis(),
    updatedAt = updatedAt?.toEpochMillis() ?: System.currentTimeMillis()
)

fun UserEntity.toDomain(): User = User(
    id = id,
    displayName = displayName,
    email = email,
    phone = phone,
    avatarUrl = avatarUrl,
    location = if (latitude != null && longitude != null)
        Location(latitude, longitude) else null,
    alertRadiusKm = alertRadiusKm,
    createdAt = createdAt,
    updatedAt = updatedAt
)