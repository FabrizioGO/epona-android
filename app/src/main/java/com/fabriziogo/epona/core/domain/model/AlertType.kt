package com.fabriziogo.epona.core.domain.model

enum class AlertType(val value: String) {
    LOST("lost"),
    FOUND("found");

    companion object {
        fun fromValue(v: String): AlertType =
            entries.firstOrNull { it.value == v } ?: LOST
    }
}