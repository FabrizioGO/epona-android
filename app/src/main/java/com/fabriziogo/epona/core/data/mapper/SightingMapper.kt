package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.database.entity.SightingEntity
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Sighting
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.fabriziogo.epona.core.network.dto.SightingInsertParams
import com.fabriziogo.epona.core.network.dto.SightingTrailDto

fun SightingTrailDto.toDomain(): SightingWithReporter = SightingWithReporter(
    sighting = Sighting(
        id = sightingId,
        alertId = alertId,
        reporterId = reporterId,
        location = Location(
            latitude = lat ?: 0.0,
            longitude = lng ?: 0.0,
            address = address
        ),
        address = address,
        photoUrls = sightingPhotos,
        note = note,
        spottedAt = spottedAt?.toEpochMillis() ?: 0L,
        createdAt = createdAt?.toEpochMillis() ?: 0L
    ),
    reporterName = reporterName,
    reporterAvatar = reporterAvatar
)

fun SightingTrailDto.toEntity(): SightingEntity = SightingEntity(
    id = sightingId,
    alertId = alertId,
    reporterId = reporterId,
    latitude = lat ?: 0.0,
    longitude = lng ?: 0.0,
    address = address,
    photoUrls = sightingPhotos,
    note = note,
    reporterName = reporterName,
    reporterAvatar = reporterAvatar,
    spottedAt = spottedAt?.toEpochMillis() ?: System.currentTimeMillis(),
    createdAt = createdAt?.toEpochMillis() ?: System.currentTimeMillis()
)

fun SightingEntity.toDomain(): SightingWithReporter = SightingWithReporter(
    sighting = Sighting(
        id = id,
        alertId = alertId,
        reporterId = reporterId,
        location = Location(latitude, longitude, address),
        address = address,
        photoUrls = photoUrls,
        note = note,
        spottedAt = spottedAt,
        createdAt = createdAt
    ),
    reporterName = reporterName ?: "Anonymous",
    reporterAvatar = reporterAvatar
)

fun Sighting.toInsertParams(reporterId: String): SightingInsertParams =
    SightingInsertParams(
        alertId = alertId,
        reporterId = reporterId,
        lat = location.latitude,
        lng = location.longitude,
        address = address,
        photoUrls = photoUrls,
        note = note,
        spottedAt = null  // Server will use NOW()
    )