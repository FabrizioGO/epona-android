package com.fabriziogo.epona.feature.sighting.report

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.feature.sighting.report.components.LocationPickerCard
import com.fabriziogo.epona.feature.sighting.report.components.PhotoCaptureCard
import com.fabriziogo.epona.feature.sighting.report.components.SightingNoteField
import com.fabriziogo.epona.feature.sighting.report.components.SubmitSection
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.permission.RequestLocationPermissionOnEntry
import com.fabriziogo.epona.core.ui.permission.rememberLocationPermissionState
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSightingScreen(
    onNavigateBack: () -> Unit,
    onNavigateBackWithSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportSightingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // The screen auto-detects the sighting location as soon as it opens, so ask here.
    val locationPermission = rememberLocationPermissionState(
        onGranted = { viewModel.onEvent(ReportSightingEvent.UseCurrentLocation) },
        onDenied = { viewModel.onEvent(ReportSightingEvent.LocationPermissionDenied) }
    )
    RequestLocationPermissionOnEntry(locationPermission)

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                SightingNavEvent.NavigateBack -> onNavigateBack()
                SightingNavEvent.NavigateBackWithSuccess -> onNavigateBackWithSuccess()
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReportSightingEvent.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            EponaTopAppBar(
                title = "Report Sighting",
                onCloseClick = { viewModel.onEvent(ReportSightingEvent.BackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Context banner
            if (state.petName.isNotBlank()) {
                Text(
                    text = "You spotted ${state.petName}? Help bring them home!",
                    style = EponaTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Add a photo and pin the location where you saw them.",
                    style = EponaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))

            // Photo capture
            PhotoCaptureCard(
                photoUris = state.photoUris,
                onAddPhoto = { uri ->
                    viewModel.onEvent(ReportSightingEvent.PhotoAdded(uri))
                },
                onRemovePhoto = { index ->
                    viewModel.onEvent(ReportSightingEvent.PhotoRemoved(index))
                }
            )

            Spacer(Modifier.height(20.dp))

            // Location picker
            LocationPickerCard(
                location = state.location,
                address = state.address,
                isLoading = state.isLoadingLocation,
                errorText = state.locationError,
                onUseCurrentLocation = {
                    if (locationPermission.isGranted) {
                        viewModel.onEvent(ReportSightingEvent.UseCurrentLocation)
                    } else {
                        locationPermission.requestOrOpenAppSettings()
                    }
                },
                onLocationPicked = { loc ->
                    viewModel.onEvent(ReportSightingEvent.LocationPicked(loc))
                }
            )

            Spacer(Modifier.height(20.dp))

            // Note
            SightingNoteField(
                note = state.note,
                onNoteChanged = { viewModel.onEvent(ReportSightingEvent.NoteChanged(it)) }
            )

            Spacer(Modifier.height(24.dp))

            // Submit
            SubmitSection(
                canSubmit = state.canSubmit,
                isSubmitting = state.isSubmitting,
                onSubmit = { viewModel.onEvent(ReportSightingEvent.SubmitClicked) }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
