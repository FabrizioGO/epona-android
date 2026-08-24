package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.database.entity.AlertEntity
import com.fabriziogo.epona.core.database.entity.AlertWithPetEntity
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertStatus
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.network.dto.AlertDetailDto
import com.fabriziogo.epona.core.network.dto.AlertDto
import com.fabriziogo.epona.core.network.dto.AlertInsertParams
import com.fabriziogo.epona.core.network.dto.NearbyAlertDto
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species

fun NearbyAlertDto.toDomain(): AlertWithDetails = AlertWithDetails(
    alert = Alert(
        id = alertId,
        type = AlertType.fromValue(type),
        status = AlertStatus.ACTIVE,
        lastSeenLocation = Location(
            latitude = lastSeenLat ?: 0.0,
            longitude = lastSeenLng ?: 0.0,
            address = lastSeenAddress
        ),
        lastSeenAddress = lastSeenAddress,
        lastSeenAt = lastSeenAt?.toEpochMillis() ?: 0L,
        reward = reward,
        sightingCount = sightingCount,
        createdAt = createdAt?.toEpochMillis() ?: 0L
    ),
    pet = Pet(
        name = petName,
        species = Species.fromValue(species),
        breed = breed,
        color = color,
        photoUrls = petPhotos
    ),
    ownerName = ownerName,
    distanceMeters = distanceMeters
)

fun AlertDetailDto.toDomain(): AlertWithDetails = AlertWithDetails(
    alert = Alert(
        id = alertId,
        petId = petId,
        userId = ownerId,
        type = AlertType.fromValue(type),
        status = AlertStatus.fromValue(status),
        lastSeenLocation = Location(lastSeenLat, lastSeenLng, lastSeenAddress),
        lastSeenAddress = lastSeenAddress,
        lastSeenAt = lastSeenAt?.toEpochMillis() ?: 0L,
        description = alertDescription,
        reward = reward,
        contactPhone = contactPhone,
        sightingCount = sightingCount,
        resolvedAt = resolvedAt?.toEpochMillis(),
        createdAt = createdAt?.toEpochMillis() ?: 0L
    ),
    pet = Pet(
        id = petId,
        ownerId = ownerId,
        name = petName,
        species = Species.fromValue(species),
        breed = breed,
        color = color,
        size = PetSize.fromValue(size ?: "medium"),
        gender = PetGender.fromValue(gender ?: "unknown"),
        microchipId = microchipId,
        description = petDescription,
        photoUrls = petPhotos
    ),
    ownerName = ownerName,
    ownerAvatar = ownerAvatar
)

fun AlertDetailDto.toAlertEntity(): AlertEntity = AlertEntity(
    id = alertId,
    petId = petId,
    userId = ownerId,
    type = type,
    status = status,
    lastSeenLat = lastSeenLat,
    lastSeenLng = lastSeenLng,
    lastSeenAddress = lastSeenAddress,
    lastSeenAt = lastSeenAt?.toEpochMillis() ?: System.currentTimeMillis(),
    description = alertDescription,
    reward = reward,
    contactPhone = contactPhone,
    sightingCount = sightingCount,
    resolvedAt = resolvedAt?.toEpochMillis(),
    createdAt = createdAt?.toEpochMillis() ?: System.currentTimeMillis()
)

fun AlertWithPetEntity.toDomain(): AlertWithDetails = AlertWithDetails(
    alert = Alert(
        id = alert.id,
        petId = alert.petId,
        userId = alert.userId,
        type = AlertType.fromValue(alert.type),
        status = AlertStatus.fromValue(alert.status),
        lastSeenLocation = Location(alert.lastSeenLat, alert.lastSeenLng, alert.lastSeenAddress),
        lastSeenAddress = alert.lastSeenAddress,
        lastSeenAt = alert.lastSeenAt,
        description = alert.description,
        reward = alert.reward,
        contactPhone = alert.contactPhone,
        sightingCount = alert.sightingCount,
        resolvedAt = alert.resolvedAt,
        createdAt = alert.createdAt
    ),
    pet = pet.toDomain(),
    ownerName = ""  // Not available from local join, filled by UI if needed
)

fun Alert.toInsertParams(userId: String): AlertInsertParams = AlertInsertParams(
    petId = petId,
    userId = userId,
    type = type.value,
    lat = lastSeenLocation.latitude,
    lng = lastSeenLocation.longitude,
    address = lastSeenAddress,
    lastSeenAt = null,  // Server will use NOW()
    description = description,
    reward = reward,
    contactPhone = contactPhone
)

fun AlertDto.toEntity(
    lat: Double = lastSeenLat ?: 0.0,
    lng: Double = lastSeenLng ?: 0.0
): AlertEntity = AlertEntity(
    id = id ?: "",
    petId = petId,
    userId = userId ?: "",
    type = type,
    status = status,
    lastSeenLat = lat,
    lastSeenLng = lng,
    lastSeenAddress = lastSeenAddress,
    lastSeenAt = lastSeenAt?.toEpochMillis() ?: System.currentTimeMillis(),
    description = description,
    reward = reward,
    contactPhone = contactPhone,
    sightingCount = sightingCount,
    resolvedAt = resolvedAt?.toEpochMillis(),
    createdAt = createdAt?.toEpochMillis() ?: System.currentTimeMillis()
)