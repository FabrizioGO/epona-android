package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.formatTimeAgo
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun DetailInfoCards(
    lastSeenAddress: String,
    lastSeenAt: Long,
    sightingCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        InfoCard(
            icon = Icons.Outlined.LocationOn,
            label = stringResource(R.string.detail_last_seen),
            value = lastSeenAddress,
            modifier = Modifier.weight(1f)
        )
        InfoCard(
            icon = Icons.Outlined.Schedule,
            label = stringResource(R.string.detail_time),
            value = formatTimeAgo(lastSeenAt),
            modifier = Modifier.weight(1f)
        )
    }

    if (sightingCount > 0) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            InfoCard(
                icon = Icons.Outlined.Visibility,
                label = stringResource(R.string.detail_sightings_label),
                value = stringResource(R.string.detail_sightings_reported, sightingCount),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = label,
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = value,
                    style = EponaTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
        }
    }
}
