package com.fabriziogo.epona.core.domain.model

/**
 * A post offering one or more pets for adoption. Unlike an [Alert] it has no
 * location and no pet row: [title] describes the animal(s) ("Golden Retriever puppies
 * in adoption") and the pet fields below are all optional hints.
 *
 * [price] is the adoption fee; null means free.
 */
data class AdoptionPost(
    val id: String = "",
    val userId: String = "",
    val title: String,
    val species: Species,
    val breed: String? = null,
    val petName: String? = null,
    val ageStage: AgeStage? = null,
    val size: PetSize? = null,
    val gender: PetGender? = null,
    val description: String? = null,
    val price: Double? = null,
    val photoUrls: List<String> = emptyList(),
    val contactPhone: String,
    val status: AdoptionStatus = AdoptionStatus.ACTIVE,
    val adoptedAt: Long? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class AdoptionPostWithOwner(
    val post: AdoptionPost,
    val ownerName: String,
    val ownerAvatar: String? = null
)
