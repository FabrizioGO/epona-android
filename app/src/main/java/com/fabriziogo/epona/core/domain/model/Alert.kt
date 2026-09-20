package com.fabriziogo.epona.core.domain.model

data class Alert(
    val id: String = "",
    val petId: String = "",
    val userId: String = "",
    val type: AlertType,
    val status: AlertStatus = AlertStatus.ACTIVE,
    /** Where a found pet is now. Null for a lost alert, which has no custody. */
    val foundCustody: FoundCustody? = null,
    val lastSeenLocation: Location,
    val lastSeenAddress: String? = null,
    val lastSeenAt: Long = 0L,
    val description: String? = null,
    val reward: Double? = null,
    val contactPhone: String? = null,
    val sightingCount: Int = 0,
    val resolvedAt: Long? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

/**
 * Whether anyone can still spot this pet and report it.
 *
 * False only for a found pet the finder took with them: it is in one safe place,
 * so a sighting would be meaningless and the post asks readers to contact the
 * finder instead. Every UI that offers a sighting reads this rather than
 * re-testing the enum, so the rule cannot drift — and create_sighting enforces
 * the same thing server-side, which is what actually makes it true.
 */
val Alert.acceptsSightings: Boolean
    get() = foundCustody != FoundCustody.WITH_FINDER

/**
 * Alert with joined pet and owner data.
 * Used for list/detail display without extra queries.
 */
data class AlertWithDetails(
    val alert: Alert,
    val pet: Pet,
    val ownerName: String,
    val ownerAvatar: String? = null,
    val distanceMeters: Double? = null
)
