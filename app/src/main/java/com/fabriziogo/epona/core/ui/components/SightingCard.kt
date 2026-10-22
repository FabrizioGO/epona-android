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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.R
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.domain.model.SightingWithReporter

@Composable
fun SightingCard(
    sighting: SightingWithReporter,
    modifier: Modifier = Modifier,
    onBlockClick: (() -> Unit)? = null
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
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatTimeAgo(sighting.sighting.spottedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (onBlockClick != null) {
                        var menuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    Icons.Outlined.MoreVert,
                                    contentDescription = stringResource(R.string.action_more_options)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.block_user_action)) },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Block, contentDescription = null)
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onBlockClick()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            sighting.sighting.note?.let { note ->
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
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
                            onClickLabel = stringResource(R.string.sighting_open_in_maps)
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
                        style = MaterialTheme.typography.labelMedium,
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
