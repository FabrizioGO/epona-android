package com.fabriziogo.epona.feature.map.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle

@Composable
fun RadiusCircleOverlay(
    center: LatLng,
    radiusMeters: Double,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val tint = MaterialTheme.colorScheme.primary
    Circle(
        center = center,
        radius = radiusMeters,
        fillColor = tint.copy(alpha = 0.08f),
        strokeColor = tint.copy(alpha = 0.4f),
        strokeWidth = 2f
    )
}