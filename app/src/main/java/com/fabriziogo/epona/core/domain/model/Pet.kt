package com.fabriziogo.epona.core.domain.model

data class Pet(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val species: Species = Species.DOG,
    val breed: String? = null,
    val color: String? = null,
    val size: PetSize = PetSize.MEDIUM,
    val ageYears: Int? = null,
    val gender: PetGender = PetGender.UNKNOWN,
    val microchipId: String? = null,
    val description: String? = null,
    val photoUrls: List<String> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)