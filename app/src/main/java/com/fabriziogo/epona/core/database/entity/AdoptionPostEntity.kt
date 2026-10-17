package com.fabriziogo.epona.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Offline copy of an adoption post, owner name/avatar included so it is self-contained.
 * Deliberately has no foreign key to `users`: the owner of a post is usually someone
 * else, and their row is never cached.
 */
@Entity(
    tableName = "adoption_posts",
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["status", "created_at"])
    ]
)
data class AdoptionPostEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    val title: String,
    val species: String,
    val breed: String? = null,

    @ColumnInfo(name = "pet_name")
    val petName: String? = null,

    @ColumnInfo(name = "age_stage")
    val ageStage: String? = null,

    val size: String? = null,
    val gender: String? = null,
    val description: String? = null,
    val price: Double? = null,

    @ColumnInfo(name = "photo_urls")
    val photoUrls: List<String> = emptyList(),

    @ColumnInfo(name = "contact_phone")
    val contactPhone: String,

    val status: String = "active",

    @ColumnInfo(name = "adopted_at")
    val adoptedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = 0L,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = 0L,

    @ColumnInfo(name = "owner_name")
    val ownerName: String = "",

    @ColumnInfo(name = "owner_avatar")
    val ownerAvatar: String? = null
)
