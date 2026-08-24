package com.fabriziogo.epona.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "display_name")
    val displayName: String,

    val email: String,

    val phone: String? = null,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    @ColumnInfo(name = "alert_radius_km")
    val alertRadiusKm: Int = 10,

    @ColumnInfo(name = "fcm_token")
    val fcmToken: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)