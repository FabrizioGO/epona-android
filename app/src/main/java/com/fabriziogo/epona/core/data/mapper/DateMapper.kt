package com.fabriziogo.epona.core.data.mapper

import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Converts ISO-8601 / Supabase timestamp strings to epoch millis.
 * Handles: "2024-09-15T10:30:00+00:00", "2024-09-15T10:30:00Z",
 *          "2024-09-15T10:30:00.123456+00"
 */
fun String.toEpochMillis(): Long = try {
    ZonedDateTime.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        .toInstant()
        .toEpochMilli()
} catch (e: DateTimeParseException) {
    try {
        Instant.parse(this).toEpochMilli()
    } catch (e2: DateTimeParseException) {
        System.currentTimeMillis()
    }
}

fun Long.toIsoString(): String =
    Instant.ofEpochMilli(this)
        .toString()