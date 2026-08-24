package com.fabriziogo.epona.feature.map.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * Shown over the map while location is not granted. The map still works from the
 * fallback location, so this stays out of the way instead of blocking the screen.
 */
@Composable
fun LocationPermissionBanner(
    isPermanentlyDenied: Boolean,
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.LocationOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )

            Spacer(Modifier.width(10.dp))

            Text(
                text = "Turn on location to see pets near you",
                style = EponaTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            TextButton(onClick = onEnableClick) {
                Text(
                    text = if (isPermanentlyDenied) "Settings" else "Enable",
                    style = EponaTypography.labelLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationPermissionBannerPreview() {
    EponaTheme {
        LocationPermissionBanner(
            isPermanentlyDenied = false,
            onEnableClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LocationPermissionBannerPermanentlyDeniedPreview() {
    EponaTheme {
        LocationPermissionBanner(
            isPermanentlyDenied = true,
            onEnableClick = {}
        )
    }
}
