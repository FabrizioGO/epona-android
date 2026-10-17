package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Row of the `adoption_posts_with_owner` view: the post plus its owner's name and avatar. */
@Serializable
data class AdoptionPostDto(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val species: String,
    val breed: String? = null,
    @SerialName("pet_name")
    val petName: String? = null,
    @SerialName("age_stage")
    val ageStage: String? = null,
    val size: String? = null,
    val gender: String? = null,
    val description: String? = null,
    val price: Double? = null,
    @SerialName("photo_urls")
    val photoUrls: List<String> = emptyList(),
    @SerialName("contact_phone")
    val contactPhone: String,
    val status: String = "active",
    @SerialName("adopted_at")
    val adoptedAt: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("owner_name")
    val ownerName: String? = null,
    @SerialName("owner_avatar")
    val ownerAvatar: String? = null
)

/** Insert payload for the `adoption_posts` table. Server defaults fill id, status and timestamps. */
@Serializable
data class AdoptionPostInsertDto(
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val species: String,
    val breed: String? = null,
    @SerialName("pet_name")
    val petName: String? = null,
    @SerialName("age_stage")
    val ageStage: String? = null,
    val size: String? = null,
    val gender: String? = null,
    val description: String? = null,
    val price: Double? = null,
    @SerialName("photo_urls")
    val photoUrls: List<String> = emptyList(),
    @SerialName("contact_phone")
    val contactPhone: String
)
