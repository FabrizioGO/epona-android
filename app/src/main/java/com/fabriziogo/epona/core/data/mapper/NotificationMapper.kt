package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.database.entity.NotificationEntity
import com.fabriziogo.epona.core.domain.model.Notification
import com.fabriziogo.epona.core.domain.model.NotificationType
import com.fabriziogo.epona.core.network.dto.NotificationDto

fun NotificationDto.toDomain(): Notification = Notification(
    id = id ?: "",
    userId = userId ?: "",
    alertId = alertId,
    type = NotificationType.fromValue(type),
    title = title,
    body = body,
    isRead = isRead,
    createdAt = createdAt?.toEpochMillis() ?: 0L
)

fun NotificationDto.toEntity(): NotificationEntity = NotificationEntity(
    id = id ?: "",
    userId = userId ?: "",
    alertId = alertId,
    type = type,
    title = title,
    body = body,
    isRead = isRead,
    createdAt = createdAt?.toEpochMillis() ?: System.currentTimeMillis()
)

fun NotificationEntity.toDomain(): Notification = Notification(
    id = id,
    userId = userId,
    alertId = alertId,
    type = NotificationType.fromValue(type),
    title = title,
    body = body,
    isRead = isRead,
    createdAt = createdAt
)