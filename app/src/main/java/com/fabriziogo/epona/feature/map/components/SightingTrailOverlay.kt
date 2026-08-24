package com.fabriziogo.epona.feature.map.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline

@Composable
fun SightingTrailOverlay(
    sightings: List<SightingWithReporter>,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val points = sightings.map { s ->
        LatLng(s.sighting.location.latitude, s.sighting.location.longitude)
    }

    // Connecting polyline
    if (points.size >= 2) {
        Polyline(
            points = points,
            color = Color(0xFF6650A4).copy(alpha = 0.7f),
            width = 6f
        )
    }

    // Dot for each sighting point
    points.forEachIndexed { index, latLng ->
        Circle(
            center = latLng,
            radius = 12.0,
            fillColor = if (index == 0) Color(0xFF6650A4) else Color(0xFF6650A4).copy(alpha = 0.5f),
            strokeColor = Color.White,
            strokeWidth = 2f
        )
    }
}