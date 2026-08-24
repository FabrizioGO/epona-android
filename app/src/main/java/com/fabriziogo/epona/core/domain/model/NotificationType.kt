package com.fabriziogo.epona.core.domain.model

enum class NotificationType(val value: String) {
    NEW_ALERT("new_alert"),
    SIGHTING("sighting"),
    RESOLVED("resolved"),
    MESSAGE("message");

    companion object {
        fun fromValue(v: String): NotificationType =
            entries.firstOrNull { it.value == v } ?: MESSAGE
    }
}