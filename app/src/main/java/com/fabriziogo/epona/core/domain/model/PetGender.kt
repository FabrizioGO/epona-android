package com.fabriziogo.epona.core.domain.model

import com.fabriziogo.epona.R

enum class PetGender(val value: String, val label: Int) {
    MALE("male", R.string.gender_male),
    FEMALE("female", R.string.gender_female),
    UNKNOWN("unknown", R.string.gender_unknown);

    companion object {
        fun fromValue(v: String): PetGender =
            entries.firstOrNull { it.value == v } ?: UNKNOWN
    }
}