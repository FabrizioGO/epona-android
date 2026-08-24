package com.fabriziogo.epona.feature.map.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MapControls(
    showRadiusCircle: Boolean,
    onRecenterClick: () -> Unit,
    onToggleRadius: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Recenter button
        SmallFloatingActionButton(
            onClick = onRecenterClick,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(
                Icons.Outlined.MyLocation,
                contentDescription = "Recenter map"
            )
        }

        Spacer(Modifier.height(8.dp))

        // Radius toggle button
        SmallFloatingActionButton(
            onClick = onToggleRadius,
            containerColor = if (showRadiusCircle) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            contentColor = if (showRadiusCircle) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ) {
            Icon(
                if (showRadiusCircle) Icons.Outlined.RadioButtonChecked
                else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = "Toggle radius circle"
            )
        }
    }
}