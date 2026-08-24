package com.fabriziogo.epona.core.domain.model

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val location: Location? = null,
    val alertRadiusKm: Int = 10,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)