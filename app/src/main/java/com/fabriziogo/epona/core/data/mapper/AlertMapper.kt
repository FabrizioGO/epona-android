package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.database.entity.AlertEntity
import com.fabriziogo.epona.core.database.entity.AlertWithPetEntity
import com.fabriziogo.epona.core.database.entity.PetEntity
import com.fabriziogo.epona.core.database.entity.UserEntity
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertStatus
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.FoundCustody
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.network.dto.AlertDetailDto
import com.fabriziogo.epona.core.network.dto.AlertDto
import com.fabriziogo.epona.core.network.dto.AlertInsertParams
import com.fabriziogo.epona.core.network.dto.FoundAlertInsertParams
import com.fabriziogo.epona.core.network.dto.NearbyAlertDto
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species

fun NearbyAlertDto.toDomain(): AlertWithDetails = AlertWithDetails(
    alert = Alert(
        id = alertId,
        type = AlertType.fromValue(type),
        status = AlertStatus.ACTIVE,
        foundCustody = FoundCustody.fromValue(foundCustody),
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
        name = petName ?: "",
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
        foundCustody = FoundCustody.fromValue(foundCustody),
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
        ownerId = petOwnerId ?: "",
        name = petName ?: "",
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
    foundCustody = foundCustody,
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

/**
 * The detail RPC arrives denormalised, but the cache is relational: `alerts` carries
 * foreign keys onto `pets` and `users`, both enforced by Room. These two mappers exist so
 * the parent rows can be written first -- without them the alert insert fails outright.
 */
fun AlertDetailDto.toPetEntity(): PetEntity = PetEntity(
    id = petId,
    ownerId = petOwnerId,
    name = petName,
    species = species,
    breed = breed,
    color = color,
    size = size,
    gender = gender,
    microchipId = microchipId,
    description = petDescription,
    photoUrls = petPhotos
)

/**
 * A placeholder for an owner whose profile we have never fetched: the RPC carries only
 * their name and avatar, so `email` is left blank. Written with `UserDao.insertUserIfAbsent`
 * so it can never overwrite a real profile.
 */
fun AlertDetailDto.toOwnerEntity(): UserEntity = UserEntity(
    id = ownerId,
    displayName = ownerName,
    email = "",
    avatarUrl = ownerAvatar
)

fun AlertWithPetEntity.toDomain(): AlertWithDetails = AlertWithDetails(
    alert = Alert(
        id = alert.id,
        petId = alert.petId,
        userId = alert.userId,
        type = AlertType.fromValue(alert.type),
        status = AlertStatus.fromValue(alert.status),
        foundCustody = FoundCustody.fromValue(alert.foundCustody),
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

/**
 * Params for the create_found_alert RPC, which inserts the ownerless pet and
 * the FOUND alert in one transaction. The pet carries no id, owner or name;
 * the alert carries no pet id yet — the server links them.
 */
fun toFoundAlertInsertParams(
    pet: Pet,
    alert: Alert,
    userId: String
): FoundAlertInsertParams = FoundAlertInsertParams(
    userId = userId,
    species = pet.species.value,
    lat = alert.lastSeenLocation.latitude,
    lng = alert.lastSeenLocation.longitude,
    custody = checkNotNull(alert.foundCustody) {
        "A found alert needs a custody -- CreateFoundAlertUseCase requires one"
    }.value,
    breed = pet.breed,
    color = pet.color,
    size = pet.size.value,
    petDescription = pet.description,
    photoUrls = pet.photoUrls,
    address = alert.lastSeenAddress,
    lastSeenAt = null,  // Server will use NOW()
    alertDescription = alert.description,
    contactPhone = alert.contactPhone
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
    foundCustody = foundCustody,
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