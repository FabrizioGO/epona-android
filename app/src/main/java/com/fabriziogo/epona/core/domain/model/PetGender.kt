package com.fabriziogo.epona.core.domain.model

enum class PetGender(val value: String, val label: String) {
    MALE("male", "Male"),
    FEMALE("female", "Female"),
    UNKNOWN("unknown", "Unknown");

    companion object {
        fun fromValue(v: String): PetGender =
            entries.firstOrNull { it.value == v } ?: UNKNOWN
    }
}