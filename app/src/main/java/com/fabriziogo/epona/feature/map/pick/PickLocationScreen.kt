package com.fabriziogo.epona.feature.map.pick

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.permission.rememberLocationPermissionState
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickLocationScreen(
    onLocationConfirmed: (Location) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PickLocationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is PickLocationNavEvent.Confirmed -> onLocationConfirmed(event.location)
                PickLocationNavEvent.Back -> onNavigateBack()
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(PickLocationEvent.ErrorDismissed)
        }
    }

    // Manual pinning is the point of this screen, so only ask on the FAB tap —
    // never on entry.
    val locationPermission = rememberLocationPermissionState(
        onGranted = { viewModel.onEvent(PickLocationEvent.UseCurrentLocation) }
    )

    // No StatusBarIcons call here: unlike MapScreen the map is not full-bleed under
    // the status bar — the app bar owns that inset in colorScheme.surface, so forcing
    // dark glyphs would break dark theme.

    if (state.isResolvingCamera) {
        LoadingIndicator()
        return
    }

    // Created after the target is known, so the map opens on the resolved location
    // instead of starting at (0, 0) and swinging across the globe.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(state.cameraLat, state.cameraLng),
            state.cameraZoom
        )
    }

    // CameraUpdateFactory is initialized by the Maps SDK when the map comes up,
    // so no camera update may be built before onMapLoaded fires.
    var isMapLoaded by remember { mutableStateOf(false) }

    // Crosshair loop: every settle (finger or programmatic recenter) reports the
    // camera target as the pin position through the one geocode path.
    LaunchedEffect(isMapLoaded, cameraPositionState.isMoving) {
        if (!isMapLoaded || cameraPositionState.isMoving) return@LaunchedEffect
        val t = cameraPositionState.position.target
        viewModel.onEvent(PickLocationEvent.CameraSettled(t.latitude, t.longitude))
    }

    // Animate camera when recenter is triggered
    LaunchedEffect(isMapLoaded, state.cameraLat, state.cameraLng) {
        if (!isMapLoaded) return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(state.cameraLat, state.cameraLng),
                state.cameraZoom
            ),
            durationMs = 600
        )
    }

    Scaffold(
        topBar = {
            EponaTopAppBar(
                title = stringResource(R.string.pick_location_title),
                onBackClick = { viewModel.onEvent(PickLocationEvent.BackClicked) }
            )
        },
        bottomBar = {
            PickLocationBottomBar(
                address = state.address,
                isGeocoding = state.isGeocoding,
                target = state.target,
                canConfirm = state.canConfirm,
                onConfirm = { viewModel.onEvent(PickLocationEvent.ConfirmClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermission.isGranted
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                ),
                onMapLoaded = { isMapLoaded = true }
            )

            // Crosshair overlay, not a Marker: the pin stays fixed while the map
            // pans underneath it. Offset so the pin tip sits on the camera target.
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-20).dp)
                    .size(40.dp)
                    .alpha(if (cameraPositionState.isMoving) 0.6f else 1f)
            )

            // Hint until the first settle reports a target.
            if (state.target == null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = stringResource(R.string.pick_location_hint),
                        style = EponaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }

            SmallFloatingActionButton(
                onClick = {
                    if (state.isLocating) return@SmallFloatingActionButton
                    if (locationPermission.isGranted) {
                        viewModel.onEvent(PickLocationEvent.UseCurrentLocation)
                    } else {
                        locationPermission.requestOrOpenAppSettings()
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                if (state.isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Outlined.MyLocation,
                        contentDescription = stringResource(R.string.map_my_location)
                    )
                }
            }
        }
    }
}

@Composable
private fun PickLocationBottomBar(
    address: String,
    isGeocoding: Boolean,
    target: Location?,
    canConfirm: Boolean,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isGeocoding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.pick_location_resolving),
                        style = EponaTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (address.isNotBlank()) {
                    Text(
                        text = address,
                        style = EponaTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.pick_location_no_address),
                        style = EponaTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            target?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${String.format("%.4f", it.latitude)}, " +
                            String.format("%.4f", it.longitude),
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.height(12.dp))
            EponaFilledButton(
                text = stringResource(R.string.pick_location_confirm),
                onClick = onConfirm,
                enabled = canConfirm,
                fullWidth = true
            )
        }
    }
}

@Preview(showBackground = true, name = "BottomBar resolved")
@Composable
private fun PickLocationBottomBarPreview() {
    EponaTheme {
        PickLocationBottomBar(
            address = "Retiro Park, Madrid",
            isGeocoding = false,
            target = Location(40.4154, -3.6844),
            canConfirm = true,
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true, name = "BottomBar resolving")
@Composable
private fun PickLocationBottomBarResolvingPreview() {
    EponaTheme {
        PickLocationBottomBar(
            address = "",
            isGeocoding = true,
            target = null,
            canConfirm = false,
            onConfirm = {}
        )
    }
}
