package com.fabriziogo.epona.core.domain.model

enum class Species(val value: String, val label: String) {
    DOG("dog", "Dog"),
    CAT("cat", "Cat"),
    BIRD("bird", "Bird"),
    RABBIT("rabbit", "Rabbit"),
    OTHER("other", "Other");

    companion object {
        fun fromValue(v: String): Species =
            entries.firstOrNull { it.value == v } ?: OTHER
    }
}