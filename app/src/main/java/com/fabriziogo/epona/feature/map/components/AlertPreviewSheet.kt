package com.fabriziogo.epona.feature.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.fabriziogo.epona.core.ui.components.AlertTypeBadge
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.SightingCard
import com.fabriziogo.epona.core.ui.components.petDisplayName
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun AlertPreviewSheet(
    alertWithDetails: AlertWithDetails,
    sightings: List<SightingWithReporter>,
    isSightingsLoading: Boolean,
    onViewDetailClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alert = alertWithDetails.alert
    val pet = alertWithDetails.pet

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Header row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = petDisplayName(pet),
                        style = EponaTypography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = listOfNotNull(pet.breed, pet.color).joinToString(" · "),
                        style = EponaTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AlertTypeBadge(type = alert.type)
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.cd_dismiss),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Last seen location
        alert.lastSeenAddress?.let { address ->
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = address,
                        style = EponaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }

        // Description
        alert.description?.let { desc ->
            item {
                Text(
                    text = desc,
                    style = EponaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                    maxLines = 3
                )
            }
        }

        // View detail button
        item {
            Spacer(Modifier.height(12.dp))
            EponaFilledButton(
                text = stringResource(R.string.map_view_details),
                onClick = onViewDetailClick,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
        }

        // Sightings section
        if (isSightingsLoading) {
            item {
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
        } else if (sightings.isNotEmpty()) {
            item {
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.map_sighting_trail_count, sightings.size),
                    style = EponaTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
            }
            items(sightings) { sighting ->
                SightingCard(
                    sighting = sighting,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
