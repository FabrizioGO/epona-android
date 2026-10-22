package com.fabriziogo.epona.core.domain.model

data class BlockedUser(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null
)
