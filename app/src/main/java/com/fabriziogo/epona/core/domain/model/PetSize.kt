package com.fabriziogo.epona.core.domain.model

import com.fabriziogo.epona.R

enum class PetSize(val value: String, val label: Int) {
    SMALL("small", R.string.size_small),
    MEDIUM("medium", R.string.size_medium),
    LARGE("large", R.string.size_large);

    companion object {
        fun fromValue(v: String): PetSize =
            entries.firstOrNull { it.value == v } ?: MEDIUM
    }
}