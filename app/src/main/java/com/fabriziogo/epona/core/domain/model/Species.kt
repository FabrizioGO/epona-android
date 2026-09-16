package com.fabriziogo.epona.core.domain.model

import com.fabriziogo.epona.R

enum class Species(val value: String, val label: Int) {
    DOG("dog", R.string.species_dog),
    CAT("cat", R.string.species_cat),
    BIRD("bird", R.string.species_bird),
    RABBIT("rabbit", R.string.species_rabbit),
    OTHER("other", R.string.species_other);

    companion object {
        fun fromValue(v: String): Species =
            entries.firstOrNull { it.value == v } ?: OTHER
    }
}