package com.fabriziogo.epona.feature.sighting.report

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.components.LocationPickerCard
import com.fabriziogo.epona.feature.sighting.report.components.SightingNoteField
import com.fabriziogo.epona.feature.sighting.report.components.SightingInfoBanner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.fabriziogo.epona.core.ui.form.FormBottomBar
import com.fabriziogo.epona.core.ui.form.FormScaffold
import com.fabriziogo.epona.core.ui.form.FormSection
import com.fabriziogo.epona.core.ui.theme.FormSpacing
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.PhotoThumbnailRow
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.core.ui.permission.RequestLocationPermissionOnEntry
import com.fabriziogo.epona.core.ui.permission.rememberLocationPermissionState
import kotlinx.coroutines.launch

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

    val isDirty = state.note.isNotBlank() || state.photos.isNotEmpty()
    var showDiscardDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isDirty && !state.isSubmitting) { showDiscardDialog = true }

    if (showDiscardDialog) {
        EponaConfirmDialog(
            title = stringResource(R.string.discard_title),
            message = stringResource(R.string.discard_message),
            confirmText = stringResource(R.string.discard_confirm),
            dismissText = stringResource(R.string.discard_keep_editing),
            onConfirm = {
                showDiscardDialog = false
                viewModel.onEvent(ReportSightingEvent.BackClicked)
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    // A failed submit (no location) leaves the error under the card, which is the first field.
    val scrollState = rememberScrollState()
    LaunchedEffect(state.locationError) {
        if (state.locationError != null) scrollState.animateScrollTo(0)
    }

    FormScaffold(
        title = stringResource(R.string.sighting_report_title),
        onClose = {
            if (isDirty && !state.isSubmitting) showDiscardDialog = true
            else viewModel.onEvent(ReportSightingEvent.BackClicked)
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        scrollState = scrollState,
        bottomBar = {
            FormBottomBar(
                primaryText = stringResource(R.string.sighting_submit),
                onPrimary = { viewModel.onEvent(ReportSightingEvent.SubmitClicked) },
                primaryIcon = Icons.Outlined.Visibility,
                loading = state.isSubmitting
            )
        }
    ) {
        Spacer(Modifier.height(FormSpacing.FieldGap))

        if (state.petName.isNotBlank()) {
            Text(
                text = stringResource(R.string.sighting_spotted, state.petName),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(4.dp))
        }
        SightingInfoBanner()
        Spacer(Modifier.height(FormSpacing.SectionGap))

        // The one required field comes first.
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
        Spacer(Modifier.height(FormSpacing.SectionGap))

        FormSection(
            title = stringResource(R.string.sighting_photos_title),
            supporting = stringResource(R.string.sighting_photos_desc)
        ) {
            PhotoThumbnailRow(
                photos = state.photos,
                onAddClick = photoPicker::open,
                onRemove = { viewModel.onEvent(ReportSightingEvent.PhotoRemoved(it)) }
            )
        }

        SightingNoteField(
            note = state.note,
            onNoteChanged = { viewModel.onEvent(ReportSightingEvent.NoteChanged(it)) },
            enabled = !state.isSubmitting
        )
    }
}
