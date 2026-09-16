package com.fabriziogo.epona.core.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun SightingCard(
    sighting: SightingWithReporter,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    EponaCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sighting.reporterName,
                    style = EponaTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = formatTimeAgo(sighting.sighting.spottedAt),
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            sighting.sighting.note?.let { note ->
                Text(
                    text = note,
                    style = EponaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            sighting.sighting.address?.let { addr ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Open in Google Maps"
                        ) {
                            openInGoogleMaps(
                                context = context,
                                latitude = sighting.sighting.location.latitude,
                                longitude = sighting.sighting.location.longitude,
                                label = addr
                            )
                        }
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = addr,
                        style = EponaTypography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun openInGoogleMaps(
    context: Context,
    latitude: Double,
    longitude: Double,
    label: String?
) {
    val mapsIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label ?: "Sighting")})")
    ).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (mapsIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapsIntent)
    } else {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            )
        )
    }
}
