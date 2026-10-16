package com.fabriziogo.epona.feature.map.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R

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
                contentDescription = stringResource(R.string.map_my_location)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Radius toggle: a toggle button exposes its on/off state to TalkBack, which a FAB
        // that only swaps colors does not.
        FilledIconToggleButton(
            checked = showRadiusCircle,
            onCheckedChange = { onToggleRadius() },
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.filledIconToggleButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Icon(
                if (showRadiusCircle) Icons.Outlined.RadioButtonChecked
                else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = stringResource(R.string.map_toggle_radius)
            )
        }
    }
}