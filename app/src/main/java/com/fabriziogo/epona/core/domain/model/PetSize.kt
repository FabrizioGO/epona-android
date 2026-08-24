package com.fabriziogo.epona.core.domain.model

enum class PetSize(val value: String, val label: String) {
    SMALL("small", "Small"),
    MEDIUM("medium", "Medium"),
    LARGE("large", "Large");

    companion object {
        fun fromValue(v: String): PetSize =
            entries.firstOrNull { it.value == v } ?: MEDIUM
    }
}