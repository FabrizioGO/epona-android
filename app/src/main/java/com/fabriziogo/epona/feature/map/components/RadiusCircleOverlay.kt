package com.fabriziogo.epona.feature.map.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle

@Composable
fun RadiusCircleOverlay(
    center: LatLng,
    radiusMeters: Double,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    Circle(
        center = center,
        radius = radiusMeters,
        fillColor = Color(0xFF6650A4).copy(alpha = 0.08f),
        strokeColor = Color(0xFF6650A4).copy(alpha = 0.4f),
        strokeWidth = 2f
    )
}