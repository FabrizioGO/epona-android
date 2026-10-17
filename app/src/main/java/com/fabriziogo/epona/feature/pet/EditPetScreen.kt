package com.fabriziogo.epona.feature.pet

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.form.FormBottomBar
import com.fabriziogo.epona.core.ui.form.FormScaffold
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.feature.pet.components.PetFormActions
import com.fabriziogo.epona.feature.pet.components.PetFormContent
import com.fabriziogo.epona.feature.pet.components.PetFormState
import kotlinx.coroutines.launch

@Composable
fun EditPetScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cameraDeniedMessage = stringResource(R.string.photo_camera_denied)

    // Owned by the screen rather than by the stateless content below, so previews of
    // the form do not need an ActivityResultRegistry to render.
    val photoPicker = rememberMediaPickerState(
        remainingSlots = MAX_PHOTOS_PER_ENTITY - state.photos.size,
        onUrisPicked = { uris -> viewModel.onEvent(EditPetEvent.PhotosPicked(uris)) },
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
                EditPetNavEvent.NavigateBack -> onNavigateBack()
                EditPetNavEvent.NavigateToSuccess -> onNavigateToSuccess()
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(EditPetEvent.ErrorDismissed)
        }
    }

    // A failed Save puts focus on the first invalid field; focusing scrolls it into view.
    val nameFocus = remember { FocusRequester() }
    val ageFocus = remember { FocusRequester() }
    LaunchedEffect(state.submitAttempts) {
        if (state.submitAttempts == 0) return@LaunchedEffect
        when {
            state.nameError != null -> nameFocus.requestFocus()
            state.ageError != null -> ageFocus.requestFocus()
        }
    }

    // The screen holds no copy of the loaded pet to diff against, so "has edits" means the
    // user has changed something since opening it. Save and error events are not edits.
    var hasEdits by rememberSaveable { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val onEvent: (EditPetEvent) -> Unit = { event ->
        if (event !is EditPetEvent.ErrorDismissed && event !is EditPetEvent.SavePet) {
            hasEdits = true
        }
        viewModel.onEvent(event)
    }
    val requestExit = { if (hasEdits) showDiscardDialog = true else onNavigateBack() }

    BackHandler(enabled = hasEdits) { showDiscardDialog = true }

    if (showDiscardDialog) {
        EponaConfirmDialog(
            title = stringResource(R.string.discard_title),
            message = stringResource(R.string.discard_message),
            confirmText = stringResource(R.string.discard_confirm),
            dismissText = stringResource(R.string.discard_keep_editing),
            onConfirm = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    FormScaffold(
        title = stringResource(R.string.pet_edit_title),
        onClose = requestExit,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            // Nothing to save until the pet has loaded.
            if (!state.isLoading) {
                FormBottomBar(
                    primaryText = stringResource(R.string.pet_update),
                    onPrimary = { viewModel.onEvent(EditPetEvent.SavePet) },
                    loading = state.isSaving
                )
            }
        }
    ) {
        if (state.isLoading) {
            // Only the initial load replaces the form; saving keeps it on screen.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            EditPetScreenContent(
                state = state,
                onEvent = onEvent,
                onAddPhotoClick = photoPicker::open,
                nameFocus = nameFocus,
                ageFocus = ageFocus
            )
        }
    }
}

@Composable
fun ColumnScope.EditPetScreenContent(
    state: EditPetUiState,
    onEvent: (EditPetEvent) -> Unit,
    onAddPhotoClick: () -> Unit = {},
    nameFocus: FocusRequester? = null,
    ageFocus: FocusRequester? = null
) {
    PetFormContent(
        state = PetFormState(
            petName = state.petName,
            selectedSpecies = state.selectedSpecies,
            breed = state.breed,
            color = state.color,
            selectedSize = state.selectedSize,
            ageYears = state.ageYears,
            selectedGender = state.selectedGender,
            microchipId = state.microchipId,
            description = state.description,
            photos = state.photos,
            nameError = state.nameError,
            ageError = state.ageError,
            enabled = !state.isSaving
        ),
        actions = PetFormActions(
            onNameChanged = { onEvent(EditPetEvent.NameChanged(it)) },
            onSpeciesChanged = { onEvent(EditPetEvent.SpeciesChanged(it)) },
            onBreedChanged = { onEvent(EditPetEvent.BreedChanged(it)) },
            onColorChanged = { onEvent(EditPetEvent.ColorChanged(it)) },
            onSizeChanged = { onEvent(EditPetEvent.SizeChanged(it)) },
            onAgeChanged = { onEvent(EditPetEvent.AgeChanged(it)) },
            onGenderChanged = { onEvent(EditPetEvent.GenderChanged(it)) },
            onMicrochipChanged = { onEvent(EditPetEvent.MicrochipIdChanged(it)) },
            onDescriptionChanged = { onEvent(EditPetEvent.DescriptionChanged(it)) },
            onPhotoRemoved = { onEvent(EditPetEvent.PhotoRemoved(it)) },
            onAddPhotoClick = onAddPhotoClick
        ),
        nameFocus = nameFocus,
        ageFocus = ageFocus
    )
}

@EponaPreviews
@Composable
fun EditPetScreenPreview() {
    EponaTheme {
        Column { EditPetScreenContent(state = EditPetUiState(), onEvent = {}) }
    }
}
