package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PetDto(
    val id: String? = null,
    @SerialName("owner_id")
    val ownerId: String? = null,
    val name: String,
    val species: String = "dog",
    val breed: String? = null,
    val color: String? = null,
    val size: String? = "medium",
    @SerialName("age_years")
    val ageYears: Int? = null,
    val gender: String? = "unknown",
    @SerialName("microchip_id")
    val microchipId: String? = null,
    val description: String? = null,
    @SerialName("photo_urls")
    val photoUrls: List<String> = emptyList(),
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
data class PetInsertDto(
    @SerialName("owner_id")
    val ownerId: String,
    val name: String,
    val species: String = "dog",
    val breed: String? = null,
    val color: String? = null,
    val size: String? = "medium",
    @SerialName("age_years")
    val ageYears: Int? = null,
    val gender: String? = "unknown",
    @SerialName("microchip_id")
    val microchipId: String? = null,
    val description: String? = null,
    @SerialName("photo_urls")
    val photoUrls: List<String> = emptyList()
)