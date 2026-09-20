package com.fabriziogo.epona.core.domain.model

import com.fabriziogo.epona.R

enum class Species(val value: String, val label: Int, val foundLabel: Int) {
    DOG("dog", R.string.species_dog, R.string.species_dog_found),
    CAT("cat", R.string.species_cat, R.string.species_cat_found),
    OTHER("other", R.string.species_other, R.string.species_other_found);

    companion object {
        fun fromValue(v: String): Species =
            entries.firstOrNull { it.value == v } ?: OTHER
    }
}