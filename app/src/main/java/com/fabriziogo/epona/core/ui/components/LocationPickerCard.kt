package com.fabriziogo.epona.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun LocationPickerCard(
    location: Location?,
    address: String,
    isLoading: Boolean,
    errorText: String?,
    onUseCurrentLocation: () -> Unit,
    onOpenMapPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.sighting_location_title),
            style = EponaTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.sighting_location_desc),
            style = EponaTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Map preview / location card. The Surface itself is not clickable: the
        // filled state embeds a MapView, which consumes touches before they reach
        // a parent clickable — so each branch handles its own tap instead.
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.sighting_location_loading),
                            style = EponaTypography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (location != null) {
                Column {
                    LocationMapPreview(
                        location = location,
                        onOpenMapPicker = onOpenMapPicker
                    )
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = address.ifBlank { stringResource(R.string.sighting_location_set) },
                                style = EponaTypography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2
                            )
                            Text(
                                text = "${String.format("%.4f", location.latitude)}, " +
                                        String.format("%.4f", location.longitude),
                                style = EponaTypography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // The whole card is tappable; this labels the filled state as such
                        // since the empty state says "tap to set" but the filled one did not.
                        Text(
                            text = stringResource(R.string.action_edit),
                            style = EponaTypography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                // Empty state — tap to set
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .clickable(onClick = onOpenMapPicker),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.sighting_location_tap),
                            style = EponaTypography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Error
        errorText?.let {
            Text(
                text = it,
                style = EponaTypography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }

        // Use current location button
        TextButton(
            onClick = onUseCurrentLocation,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Icon(
                Icons.Outlined.MyLocation,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(R.string.sighting_use_current),
                style = EponaTypography.labelLarge
            )
        }
    }
}

/**
 * Static map snapshot of the chosen location. Lite mode renders a bitmap instead
 * of a live map — cheap enough for a card, and gesture-free so there is no fight
 * with the surrounding vertical scroll. A transparent overlay intercepts taps
 * because the MapView consumes touches before a parent clickable ever sees them.
 */
@Composable
private fun LocationMapPreview(
    location: Location,
    onOpenMapPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latLng = LatLng(location.latitude, location.longitude)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        contentAlignment = Alignment.Center
    ) {
        if (LocalInspectionMode.current) {
            // GoogleMap cannot render in the preview pane (same reason MapScreen
            // has no @Preview), so LocationStep previews get a stand-in.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val cameraPositionState = rememberCameraPositionState(
                key = "${location.latitude},${location.longitude}"
            ) {
                position = CameraPosition.fromLatLngZoom(latLng, 15f)
            }
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                googleMapOptionsFactory = { GoogleMapOptions().liteMode(true) },
                properties = MapProperties(),
                uiSettings = MapUiSettings(
                    compassEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false,
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomControlsEnabled = false,
                    zoomGesturesEnabled = false
                )
            ) {
                Marker(state = MarkerState(position = latLng))
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onOpenMapPicker)
        )
    }
}
