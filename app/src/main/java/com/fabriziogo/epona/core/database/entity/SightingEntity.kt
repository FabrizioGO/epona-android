package com.fabriziogo.epona.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sightings",
    foreignKeys = [
        ForeignKey(
            entity = AlertEntity::class,
            parentColumns = ["id"],
            childColumns = ["alert_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["alert_id"]),
        Index(value = ["reporter_id"])
    ]
)
data class SightingEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "alert_id")
    val alertId: String,

    @ColumnInfo(name = "reporter_id")
    val reporterId: String,

    val latitude: Double,

    val longitude: Double,

    val address: String? = null,

    @ColumnInfo(name = "photo_urls")
    val photoUrls: List<String> = emptyList(),

    val note: String? = null,

    @ColumnInfo(name = "reporter_name")
    val reporterName: String? = null,

    @ColumnInfo(name = "reporter_avatar")
    val reporterAvatar: String? = null,

    @ColumnInfo(name = "spotted_at")
    val spottedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)