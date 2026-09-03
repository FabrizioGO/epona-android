package com.fabriziogo.epona.feature.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.feature.map.components.AlertMarkerContent
import com.fabriziogo.epona.feature.map.components.AlertPreviewSheet
import com.fabriziogo.epona.feature.map.components.LocationPermissionBanner
import com.fabriziogo.epona.feature.map.components.MapControls
import com.fabriziogo.epona.feature.map.components.MapFilterBar
import com.fabriziogo.epona.feature.map.components.RadiusCircleOverlay
import com.fabriziogo.epona.feature.map.components.SightingTrailOverlay
import com.fabriziogo.epona.core.ui.components.EponaFAB
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.permission.RequestLocationPermissionOnEntry
import com.fabriziogo.epona.core.ui.permission.rememberLocationPermissionState
import com.fabriziogo.epona.core.ui.theme.StatusBarIcons
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreateAlert: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is MapNavEvent.NavigateToDetail -> onNavigateToDetail(event.alertId)
                MapNavEvent.NavigateToCreateAlert -> onNavigateToCreateAlert()
            }
        }
    }

    // Opening the Map tab is itself the ask: the system dialog comes up here, and a
    // grant re-runs the nearby query so the camera lands on the user.
    val locationPermission = rememberLocationPermissionState(
        onGranted = { viewModel.onEvent(MapEvent.LocationPermissionGranted) }
    )
    RequestLocationPermissionOnEntry(locationPermission)

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Google Maps renders light tiles in either theme, so the glyphs over it stay dark
    // even when enableEdgeToEdge would have picked white ones for the dark theme.
    StatusBarIcons(darkIcons = true)

    if (state.isLoading) {
        LoadingIndicator()
        return
    }

    // Created after loading finishes, so the map opens on the resolved location
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

    // Animate camera when recenter is triggered
    LaunchedEffect(isMapLoaded, state.cameraLat, state.cameraLng, state.cameraZoom) {
        if (!isMapLoaded || state.cameraLat == 0.0) return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.fromLatLngZoom(
                    LatLng(state.cameraLat, state.cameraLng),
                    state.cameraZoom
                )
            ),
            durationMs = 600
        )
    }

    // Animate to selected alert
    LaunchedEffect(isMapLoaded, state.selectedAlert) {
        if (!isMapLoaded) return@LaunchedEffect
        state.selectedAlert?.let { alert ->
            val loc = alert.alert.lastSeenLocation
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(loc.latitude, loc.longitude),
                    15f
                ),
                durationMs = 500
            )
        }
    }

    val sheetState = rememberStandardBottomSheetState(
        initialValue = if (state.selectedAlert != null) SheetValue.Expanded
        else SheetValue.Hidden,
        skipHiddenState = false
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    // Show/hide bottom sheet based on selection
    LaunchedEffect(state.selectedAlert) {
        if (state.selectedAlert != null) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.hide()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            state.selectedAlert?.let { alert ->
                AlertPreviewSheet(
                    alertWithDetails = alert,
                    sightings = state.sightingTrail,
                    isSightingsLoading = state.isSightingsLoading,
                    onViewDetailClick = {
                        viewModel.onEvent(MapEvent.ViewDetailClicked)
                    },
                    onDismiss = {
                        viewModel.onEvent(MapEvent.SheetDismissed)
                    }
                )
            }
        },
        sheetPeekHeight = if (state.selectedAlert != null) 280.dp else 0.dp,
        modifier = modifier
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Google Map
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
                onMapClick = { viewModel.onEvent(MapEvent.SheetDismissed) },
                onMapLoaded = { isMapLoaded = true },
                // The map itself is full-bleed; this keeps the SDK's own compass and
                // attribution, plus the camera target, clear of the status bar.
                contentPadding = PaddingValues(top = statusBarHeight)
            ) {
                // Radius circle overlay
                if (state.showRadiusCircle) {
                    state.userLocation?.let { loc ->
                        RadiusCircleOverlay(
                            center = LatLng(loc.latitude, loc.longitude),
                            radiusMeters = (state.alertRadiusKm * 1000).toDouble()
                        )
                    }
                }

                // Sighting trail overlay
                if (state.showSightingTrail && state.sightingTrail.isNotEmpty()) {
                    SightingTrailOverlay(sightings = state.sightingTrail)
                }

                // Alert markers
                state.filteredAlerts.forEach { alertWithDetails ->
                    val loc = alertWithDetails.alert.lastSeenLocation
                    MarkerComposable(
                        keys = arrayOf(alertWithDetails.alert.id),
                        state = MarkerState(LatLng(loc.latitude, loc.longitude)),
                        onClick = {
                            viewModel.onEvent(MapEvent.MarkerClicked(alertWithDetails.alert.id))
                            true
                        }
                    ) {
                        AlertMarkerContent(
                            alertWithDetails = alertWithDetails,
                            isSelected = state.selectedAlert?.alert?.id == alertWithDetails.alert.id
                        )
                    }
                }
            }

            // Location prompt — the map still works without it, so this only nudges
            if (!locationPermission.isGranted) {
                LocationPermissionBanner(
                    isPermanentlyDenied = locationPermission.isPermanentlyDenied,
                    onEnableClick = { locationPermission.requestOrOpenAppSettings() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 72.dp, start = 16.dp, end = 16.dp)
                )
            }

            // Filter bar at top
            MapFilterBar(
                showLost = state.showLost,
                showFound = state.showFound,
                onFilterToggled = { viewModel.onEvent(MapEvent.FilterToggled(it)) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp, start = 16.dp, end = 72.dp)
            )

            // Map controls (recenter, radius toggle)
            MapControls(
                showRadiusCircle = state.showRadiusCircle,
                onRecenterClick = { viewModel.onEvent(MapEvent.RecenterClicked) },
                onToggleRadius = { viewModel.onEvent(MapEvent.ToggleRadiusCircle) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
            )

            // FAB for creating alert
            EponaFAB(
                onClick = { viewModel.onEvent(MapEvent.CreateAlertClicked) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, end = 16.dp)
            )
        }
    }
}
