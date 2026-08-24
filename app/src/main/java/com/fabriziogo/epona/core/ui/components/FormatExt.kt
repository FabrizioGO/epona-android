package com.fabriziogo.epona.core.ui.components

import java.util.concurrent.TimeUnit

/**
 * Formats a distance in meters to a human-readable string.
 */
fun formatDistance(meters: Double): String = when {
    meters < 1000 -> "${meters.toInt()} m"
    else -> String.format("%.1f km", meters / 1000.0)
}

/**
 * Formats a timestamp (epoch millis) to relative "time ago" string.
 */
fun formatTimeAgo(epochMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochMillis

    val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hrs = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        mins < 1 -> "just now"
        mins < 60 -> "${mins}m ago"
        hrs < 24 -> "${hrs}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        else -> "${days / 30}mo ago"
    }
}