package com.fabriziogo.epona.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.ui.components.AlertTypeBadge
import com.fabriziogo.epona.core.ui.components.RewardBadge
import com.fabriziogo.epona.core.ui.components.petDisplayName
import com.fabriziogo.epona.core.ui.components.emoji
import com.fabriziogo.epona.core.ui.components.formatDistance
import com.fabriziogo.epona.core.ui.extensions.pressScale
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTeal
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.feature.home.previewAlertWithDetails

/**
 * Tall photo card for the home feed grid — pet photo with a gradient overlay carrying the
 * alert type, an optional reward badge, the pet's name, last-seen address, sighting count and
 * distance.
 */
@Composable
fun AlertGridCard(
    alertWithDetails: AlertWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alert = alertWithDetails.alert
    val pet = alertWithDetails.pet

    Box(
        modifier = modifier
            .aspectRatio(0.72f)
            .clip(MaterialTheme.shapes.medium)
            .pressScale(onClick = onClick)
    ) {
        if (pet.photoUrls.isNotEmpty()) {
            AsyncImage(
                model = pet.photoUrls.first(),
                contentDescription = petDisplayName(pet),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (alert.type == AlertType.LOST)
                            EponaColors.LostContainer else EponaColors.FoundContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = pet.species.emoji(), style = EponaTypography.displaySmall)
            }
        }

        // Scrim so the bottom text stays legible over any photo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.75f)
                    )
                )
        )

        AlertTypeBadge(
            type = alert.type,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )

        alert.reward?.let { reward ->
            RewardBadge(
                amount = reward,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = petDisplayName(pet),
                style = EponaTypography.titleMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color.White.copy(alpha = 0.75f)
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    text = alert.lastSeenAddress ?: stringResource(R.string.alert_location_unknown),
                    style = EponaTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.size(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(Modifier.size(2.dp))
                    Text(
                        text = pluralStringResource(
                            R.plurals.alert_sighting_count,
                            alert.sightingCount,
                            alert.sightingCount
                        ),
                        style = EponaTypography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                alertWithDetails.distanceMeters?.let { distance ->
                    Surface(
                        shape = CircleShape,
                        color = EponaTeal
                    ) {
                        Text(
                            text = formatDistance(distance),
                            style = EponaTypography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AlertGridCardPreview() {
    EponaTheme(dynamicColor = false) {
        Box(modifier = Modifier.size(180.dp, 250.dp)) {
            AlertGridCard(
                alertWithDetails = previewAlertWithDetails(),
                onClick = {}
            )
        }
    }
}
