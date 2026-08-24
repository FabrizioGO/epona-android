package com.fabriziogo.epona.core.data.mapper

import com.fabriziogo.epona.core.database.entity.PetEntity
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.network.dto.PetDto
import com.fabriziogo.epona.core.network.dto.PetInsertDto

fun PetDto.toDomain(): Pet = Pet(
    id = id ?: "",
    ownerId = ownerId ?: "",
    name = name,
    species = Species.fromValue(species),
    breed = breed,
    color = color,
    size = PetSize.fromValue(size ?: "medium"),
    ageYears = ageYears,
    gender = PetGender.fromValue(gender ?: "unknown"),
    microchipId = microchipId,
    description = description,
    photoUrls = photoUrls,
    createdAt = createdAt?.toEpochMillis() ?: 0L,
    updatedAt = updatedAt?.toEpochMillis() ?: 0L
)

fun PetDto.toEntity(): PetEntity = PetEntity(
    id = id ?: "",
    ownerId = ownerId ?: "",
    name = name,
    species = species,
    breed = breed,
    color = color,
    size = size,
    ageYears = ageYears,
    gender = gender,
    microchipId = microchipId,
    description = description,
    photoUrls = photoUrls,
    createdAt = createdAt?.toEpochMillis() ?: System.currentTimeMillis(),
    updatedAt = updatedAt?.toEpochMillis() ?: System.currentTimeMillis()
)

fun PetEntity.toDomain(): Pet = Pet(
    id = id,
    ownerId = ownerId,
    name = name,
    species = Species.fromValue(species),
    breed = breed,
    color = color,
    size = PetSize.fromValue(size ?: "medium"),
    ageYears = ageYears,
    gender = PetGender.fromValue(gender ?: "unknown"),
    microchipId = microchipId,
    description = description,
    photoUrls = photoUrls,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Pet.toInsertDto(ownerId: String): PetInsertDto = PetInsertDto(
    ownerId = ownerId,
    name = name,
    species = species.value,
    breed = breed,
    color = color,
    size = size.value,
    ageYears = ageYears,
    gender = gender.value,
    microchipId = microchipId,
    description = description,
    photoUrls = photoUrls
)

fun Pet.toDto(): PetDto = PetDto(
    id = id,
    ownerId = ownerId,
    name = name,
    species = species.value,
    breed = breed,
    color = color,
    size = size.value,
    ageYears = ageYears,
    gender = gender.value,
    microchipId = microchipId,
    description = description,
    photoUrls = photoUrls
)