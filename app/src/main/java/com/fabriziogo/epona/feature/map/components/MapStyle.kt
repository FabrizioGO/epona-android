package com.fabriziogo.epona.feature.map.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import com.fabriziogo.epona.R
import com.google.android.gms.maps.model.MapStyleOptions

/**
 * Dark base-map style when the app theme is dark, so the map does not glare against dark
 * surfaces. Follows the theme's surface rather than the system setting, so an app-level
 * theme override stays consistent. Returns null (the default map) in light theme.
 */
@Composable
fun rememberMapStyle(): MapStyleOptions? {
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return remember(isDark) {
        if (isDark) MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) else null
    }
}
