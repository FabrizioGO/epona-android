package com.fabriziogo.epona.feature.profile.myalerts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertStatus
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.AlertStatusBadge
import com.fabriziogo.epona.core.ui.components.AlertTypeBadge
import com.fabriziogo.epona.core.ui.components.EponaCard
import com.fabriziogo.epona.core.ui.components.formatTimeAgo
import com.fabriziogo.epona.core.ui.components.petDisplayName
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun MyAlertCard(
    alertWithDetails: AlertWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alert = alertWithDetails.alert
    val pet = alertWithDetails.pet

    EponaCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
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
                        text = when (pet.species.value) {
                            "dog" -> "🐕"
                            "cat" -> "🐈"
                            "bird" -> "🐦"
                            "rabbit" -> "🐇"
                            else -> "🐾"
                        },
                        style = EponaTypography.headlineSmall
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = petDisplayName(pet),
                        style = EponaTypography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    AlertTypeBadge(type = alert.type)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = alert.lastSeenAddress ?: stringResource(R.string.alert_location_unknown),
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AlertStatusBadge(status = alert.status)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Outlined.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = pluralStringResource(
                            R.plurals.alert_sighting_count,
                            alert.sightingCount,
                            alert.sightingCount
                        ),
                        style = EponaTypography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatTimeAgo(alert.createdAt),
                        style = EponaTypography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

private fun previewAlert(
    type: AlertType = AlertType.LOST,
    status: AlertStatus = AlertStatus.ACTIVE,
    address: String? = "Retiro Park, Madrid",
    sightings: Int = 3,
    pet: Pet = Pet(id = "p1", name = "Luna", species = Species.DOG)
) = AlertWithDetails(
    alert = Alert(
        id = "1",
        type = type,
        status = status,
        lastSeenLocation = Location(40.4, -3.7, address),
        lastSeenAddress = address,
        sightingCount = sightings,
        createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000L
    ),
    pet = pet,
    ownerName = "Fabrizio"
)

@Preview(showBackground = true, name = "Lost / active")
@Composable
private fun MyAlertCardLostPreview() {
    EponaTheme { MyAlertCard(alertWithDetails = previewAlert(), onClick = {}) }
}

@Preview(showBackground = true, name = "Found / resolved")
@Composable
private fun MyAlertCardFoundResolvedPreview() {
    EponaTheme {
        MyAlertCard(
            alertWithDetails = previewAlert(
                type = AlertType.FOUND,
                status = AlertStatus.RESOLVED,
                sightings = 0,
                pet = Pet(id = "p2", name = "Milo", species = Species.CAT)
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Unknown location, long name")
@Composable
private fun MyAlertCardLongTextPreview() {
    EponaTheme {
        MyAlertCard(
            alertWithDetails = previewAlert(
                address = null,
                sightings = 1,
                pet = Pet(id = "p3", name = "Sir Bartholomew Fluffington III", species = Species.OTHER)
            ),
            onClick = {}
        )
    }
}
