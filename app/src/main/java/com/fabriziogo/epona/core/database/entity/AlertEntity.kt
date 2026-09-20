package com.fabriziogo.epona.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alerts",
    foreignKeys = [
        ForeignKey(
            entity = PetEntity::class,
            parentColumns = ["id"],
            childColumns = ["pet_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["pet_id"]),
        Index(value = ["user_id"]),
        Index(value = ["status"]),
        Index(value = ["last_seen_lat", "last_seen_lng"])
    ]
)
data class AlertEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "pet_id")
    val petId: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    val type: String,       // "lost" | "found"

    val status: String = "active",  // "active" | "resolved" | "expired"

    // "with_finder" | "at_location"; null on lost alerts. Decides whether the
    // detail screen offers a sighting at all — see Alert.acceptsSightings.
    @ColumnInfo(name = "found_custody")
    val foundCustody: String? = null,

    @ColumnInfo(name = "last_seen_lat")
    val lastSeenLat: Double,

    @ColumnInfo(name = "last_seen_lng")
    val lastSeenLng: Double,

    @ColumnInfo(name = "last_seen_address")
    val lastSeenAddress: String? = null,

    @ColumnInfo(name = "last_seen_at")
    val lastSeenAt: Long = System.currentTimeMillis(),

    val description: String? = null,

    val reward: Double? = null,

    @ColumnInfo(name = "contact_phone")
    val contactPhone: String? = null,

    @ColumnInfo(name = "sighting_count")
    val sightingCount: Int = 0,

    @ColumnInfo(name = "resolved_at")
    val resolvedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)