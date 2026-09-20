package com.fabriziogo.epona.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.acceptsSightings
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun PetCard(
    alertWithDetails: AlertWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alert = alertWithDetails.alert
    val pet = alertWithDetails.pet

    EponaCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pet photo or placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (alert.type == AlertType.LOST)
                            EponaColors.LostContainer
                        else EponaColors.FoundContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (pet.photoUrls.isNotEmpty()) {
                    AsyncImage(
                        model = pet.photoUrls.first(),
                        contentDescription = petDisplayName(pet),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    Text(
                        text = pet.species.emoji(),
                        style = EponaTypography.headlineSmall
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = petDisplayName(pet),
                        style = EponaTypography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AlertTypeBadge(type = alert.type)
                }

                Text(
                    text = listOfNotNull(pet.breed, pet.color)
                        .joinToString(" · "),
                    style = EponaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // What tapping this row leads to differs: an animal still out
                // there takes a sighting, one already in someone's care takes a
                // phone call. Saying so here keeps the promise honest.
                if (!alert.acceptsSightings) {
                    Text(
                        text = stringResource(R.string.create_matches_hint_custody),
                        style = EponaTypography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    alertWithDetails.distanceMeters?.let { dist ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = formatDistance(dist),
                                style = EponaTypography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = formatTimeAgo(alert.createdAt),
                            style = EponaTypography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
