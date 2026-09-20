package com.fabriziogo.epona.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["owner_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["owner_id"])
    ]
)
data class PetEntity(
    @PrimaryKey
    val id: String,

    // Nullable since the found-pet flow caches ownerless strays. SQLite does not
    // enforce a foreign key whose child value is NULL, so the FK above stays.
    // PetDao.observeMyPets/getMyPets use WHERE owner_id = :ownerId, which never
    // matches NULL — ownerless pets stay out of My Pets by construction.
    @ColumnInfo(name = "owner_id")
    val ownerId: String?,

    @ColumnInfo(name = "name")
    val name: String?,

    val species: String = "dog",

    val breed: String? = null,

    val color: String? = null,

    val size: String? = "medium",

    @ColumnInfo(name = "age_years")
    val ageYears: Int? = null,

    val gender: String? = "unknown",

    @ColumnInfo(name = "microchip_id")
    val microchipId: String? = null,

    val description: String? = null,

    @ColumnInfo(name = "photo_urls")
    val photoUrls: List<String> = emptyList(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)