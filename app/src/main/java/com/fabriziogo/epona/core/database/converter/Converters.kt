package com.fabriziogo.epona.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters for complex types.
 * Uses Kotlinx Serialization for List<String> ↔ JSON.
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
}