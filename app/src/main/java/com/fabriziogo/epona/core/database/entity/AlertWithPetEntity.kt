package com.fabriziogo.epona.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * One-to-one relationship: Alert with its Pet.
 * Used for list display and detail screens.
 */
data class AlertWithPetEntity(
    @Embedded
    val alert: AlertEntity,

    @Relation(
        parentColumn = "pet_id",
        entityColumn = "id"
    )
    val pet: PetEntity
)