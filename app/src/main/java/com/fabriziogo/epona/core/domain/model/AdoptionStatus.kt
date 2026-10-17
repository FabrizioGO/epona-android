package com.fabriziogo.epona.core.domain.model

enum class AdoptionStatus(val value: String) {
    ACTIVE("active"),
    ADOPTED("adopted");

    companion object {
        fun fromValue(v: String?): AdoptionStatus =
            entries.firstOrNull { it.value == v } ?: ACTIVE
    }
}
