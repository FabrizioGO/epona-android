package com.fabriziogo.epona.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.R
import java.util.concurrent.TimeUnit

/**
 * Formats a distance in meters to a human-readable string.
 */
@Composable
fun formatDistance(meters: Double): String = when {
    meters < 1000 -> stringResource(R.string.format_distance_meters, meters.toInt())
    else -> stringResource(R.string.format_distance_km, meters / 1000.0)
}

/**
 * Formats a timestamp (epoch millis) to relative "time ago" string.
 */
@Composable
fun formatTimeAgo(epochMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochMillis

    val mins = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hrs = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        mins < 1 -> stringResource(R.string.time_just_now)
        mins < 60 -> stringResource(R.string.time_minutes_ago, mins)
        hrs < 24 -> stringResource(R.string.time_hours_ago, hrs)
        days < 7 -> stringResource(R.string.time_days_ago, days)
        days < 30 -> stringResource(R.string.time_weeks_ago, days / 7)
        else -> stringResource(R.string.time_months_ago, days / 30)
    }
}
