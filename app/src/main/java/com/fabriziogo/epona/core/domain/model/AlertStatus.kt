package com.fabriziogo.epona.core.domain.model

enum class AlertStatus(val value: String) {
    ACTIVE("active"),
    RESOLVED("resolved"),
    EXPIRED("expired");

    companion object {
        fun fromValue(v: String): AlertStatus =
            entries.firstOrNull { it.value == v } ?: ACTIVE
    }
}