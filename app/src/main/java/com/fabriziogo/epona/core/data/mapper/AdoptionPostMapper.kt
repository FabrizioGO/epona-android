package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.database.entity.AdoptionPostEntity
import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.model.AdoptionStatus
import com.fabriziogo.epona.core.domain.model.AgeStage
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.network.dto.AdoptionPostDto
import com.fabriziogo.epona.core.network.dto.AdoptionPostInsertDto

private fun String?.nullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

// Size and gender are optional here, unlike on Pet: an unrecognised or missing value
// means "not given", not MEDIUM / UNKNOWN.
private fun String?.toPetSizeOrNull(): PetSize? = PetSize.entries.firstOrNull { it.value == this }
private fun String?.toPetGenderOrNull(): PetGender? = PetGender.entries.firstOrNull { it.value == this }

fun AdoptionPostDto.toDomain(): AdoptionPostWithOwner = AdoptionPostWithOwner(
    post = AdoptionPost(
        id = id,
        userId = userId,
        title = title,
        species = Species.fromValue(species),
        breed = breed.nullIfBlank(),
        petName = petName.nullIfBlank(),
        ageStage = AgeStage.fromValue(ageStage),
        size = size.toPetSizeOrNull(),
        gender = gender.toPetGenderOrNull(),
        description = description.nullIfBlank(),
        price = price,
        photoUrls = photoUrls,
        contactPhone = contactPhone,
        status = AdoptionStatus.fromValue(status),
        adoptedAt = adoptedAt?.toEpochMillis(),
        createdAt = createdAt?.toEpochMillis() ?: 0L,
        updatedAt = updatedAt?.toEpochMillis() ?: 0L
    ),
    ownerName = ownerName.orEmpty(),
    ownerAvatar = ownerAvatar
)

fun AdoptionPost.toInsertDto(userId: String): AdoptionPostInsertDto = AdoptionPostInsertDto(
    userId = userId,
    title = title.trim(),
    species = species.value,
    breed = breed.nullIfBlank(),
    petName = petName.nullIfBlank(),
    ageStage = ageStage?.value,
    size = size?.value,
    gender = gender?.value,
    description = description.nullIfBlank(),
    price = price,
    photoUrls = photoUrls,
    contactPhone = contactPhone.trim()
)

fun AdoptionPostDto.toEntity(): AdoptionPostEntity = AdoptionPostEntity(
    id = id,
    userId = userId,
    title = title,
    species = species,
    breed = breed,
    petName = petName,
    ageStage = ageStage,
    size = size,
    gender = gender,
    description = description,
    price = price,
    photoUrls = photoUrls,
    contactPhone = contactPhone,
    status = status,
    adoptedAt = adoptedAt?.toEpochMillis(),
    createdAt = createdAt?.toEpochMillis() ?: 0L,
    updatedAt = updatedAt?.toEpochMillis() ?: 0L,
    ownerName = ownerName.orEmpty(),
    ownerAvatar = ownerAvatar
)

fun AdoptionPostEntity.toDomain(): AdoptionPostWithOwner = AdoptionPostWithOwner(
    post = AdoptionPost(
        id = id,
        userId = userId,
        title = title,
        species = Species.fromValue(species),
        breed = breed,
        petName = petName,
        ageStage = AgeStage.fromValue(ageStage),
        size = size.toPetSizeOrNull(),
        gender = gender.toPetGenderOrNull(),
        description = description,
        price = price,
        photoUrls = photoUrls,
        contactPhone = contactPhone,
        status = AdoptionStatus.fromValue(status),
        adoptedAt = adoptedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    ),
    ownerName = ownerName,
    ownerAvatar = ownerAvatar
)
