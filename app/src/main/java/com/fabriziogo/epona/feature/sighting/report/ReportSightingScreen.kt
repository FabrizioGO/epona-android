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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.ui.components.LocationPickerCard
import com.fabriziogo.epona.feature.sighting.report.components.SightingNoteField
import com.fabriziogo.epona.feature.sighting.report.components.SubmitSection
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.PhotoThumbnailRow
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.core.ui.permission.RequestLocationPermissionOnEntry
import com.fabriziogo.epona.core.ui.permission.rememberLocationPermissionState
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSightingScreen(
    onNavigateBack: () -> Unit,
    onNavigateBackWithSuccess: () -> Unit,
    onNavigateToPickLocation: (Location?) -> Unit,
    pickedLocation: Location?,
    onPickedLocationConsumed: () -> Unit,
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

    val scope = rememberCoroutineScope()
    val cameraDeniedMessage = stringResource(R.string.photo_camera_denied)
    val photoPicker = rememberMediaPickerState(
        remainingSlots = MAX_PHOTOS_PER_ENTITY - state.photos.size,
        onUrisPicked = { uris -> viewModel.onEvent(ReportSightingEvent.PhotosPicked(uris)) },
        onCameraDenied = {
            scope.launch { snackbarHostState.showSnackbar(cameraDeniedMessage) }
        }
    )

    if (photoPicker.isSheetVisible) {
        PhotoSourceSheet(
            onDismiss = photoPicker::dismiss,
            onGalleryClick = photoPicker::pickFromGallery,
            onCameraClick = photoPicker::takePhoto,
            isCameraAvailable = photoPicker.isCameraAvailable
        )
    }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                SightingNavEvent.NavigateBack -> onNavigateBack()
                SightingNavEvent.NavigateBackWithSuccess -> onNavigateBackWithSuccess()
            }
        }
    }

    LaunchedEffect(pickedLocation) {
        pickedLocation?.let {
            viewModel.onEvent(ReportSightingEvent.LocationPicked(it))
            onPickedLocationConsumed()
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
                title = stringResource(R.string.sighting_report_title),
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
                    text = stringResource(R.string.sighting_spotted, state.petName),
                    style = EponaTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.sighting_add_photo),
                    style = EponaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))

            // Photo capture
            Text(
                text = stringResource(R.string.sighting_photos_title),
                style = EponaTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.sighting_photos_desc),
                style = EponaTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            PhotoThumbnailRow(
                photos = state.photos,
                onAddClick = photoPicker::open,
                onRemove = { viewModel.onEvent(ReportSightingEvent.PhotoRemoved(it)) },
                thumbnailSize = 90.dp
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
                onOpenMapPicker = { onNavigateToPickLocation(state.location) }
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
