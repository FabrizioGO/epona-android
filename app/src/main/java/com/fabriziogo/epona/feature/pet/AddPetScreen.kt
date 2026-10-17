package com.fabriziogo.epona.feature.pet

import com.fabriziogo.epona.core.ui.text.ErrorSnackbarEffect
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
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
fun AddPetScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddPetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cameraDeniedMessage = stringResource(R.string.photo_camera_denied)

    // Owned by the screen rather than by the stateless content below, so previews of
    // the form do not need an ActivityResultRegistry to render.
    val photoPicker = rememberMediaPickerState(
        remainingSlots = MAX_PHOTOS_PER_ENTITY - state.photos.size,
        onUrisPicked = { uris -> viewModel.onEvent(AddPetEvent.PhotosPicked(uris)) },
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
                AddPetNavEvent.NavigateBack -> onNavigateBack()
                AddPetNavEvent.NavigateToSuccess -> onNavigateToSuccess()
            }
        }
    }

    ErrorSnackbarEffect(state.error, snackbarHostState) { viewModel.onEvent(AddPetEvent.ErrorDismissed) }

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

    val isDirty = state.petName.isNotBlank() || state.breed.isNotBlank() ||
        state.color.isNotBlank() || state.ageYears.isNotBlank() ||
        state.microchipId.isNotBlank() || state.description.isNotBlank() ||
        state.photos.isNotEmpty()
    var showDiscardDialog by remember { mutableStateOf(false) }
    val requestExit = { if (isDirty) showDiscardDialog = true else onNavigateBack() }

    BackHandler(enabled = isDirty) { showDiscardDialog = true }

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
        title = stringResource(R.string.create_add_pet),
        onClose = requestExit,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            FormBottomBar(
                primaryText = stringResource(R.string.pet_save),
                onPrimary = { viewModel.onEvent(AddPetEvent.SavePet) },
                loading = state.isLoading
            )
        }
    ) {
        AddPetScreenContent(
            state = state,
            onEvent = { viewModel.onEvent(it) },
            onAddPhotoClick = photoPicker::open,
            nameFocus = nameFocus,
            ageFocus = ageFocus
        )
    }
}

@Composable
fun ColumnScope.AddPetScreenContent(
    state: AddPetUiState,
    onEvent: (AddPetEvent) -> Unit,
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
            enabled = !state.isLoading
        ),
        actions = PetFormActions(
            onNameChanged = { onEvent(AddPetEvent.NameChanged(it)) },
            onSpeciesChanged = { onEvent(AddPetEvent.SpeciesChanged(it)) },
            onBreedChanged = { onEvent(AddPetEvent.BreedChanged(it)) },
            onColorChanged = { onEvent(AddPetEvent.ColorChanged(it)) },
            onSizeChanged = { onEvent(AddPetEvent.SizeChanged(it)) },
            onAgeChanged = { onEvent(AddPetEvent.AgeChanged(it)) },
            onGenderChanged = { onEvent(AddPetEvent.GenderChanged(it)) },
            onMicrochipChanged = { onEvent(AddPetEvent.MicrochipIdChanged(it)) },
            onDescriptionChanged = { onEvent(AddPetEvent.DescriptionChanged(it)) },
            onPhotoRemoved = { onEvent(AddPetEvent.PhotoRemoved(it)) },
            onAddPhotoClick = onAddPhotoClick
        ),
        nameFocus = nameFocus,
        ageFocus = ageFocus
    )
}

@EponaPreviews
@Composable
fun AddPetScreenPreview() {
    EponaTheme {
        Column { AddPetScreenContent(state = AddPetUiState(), onEvent = {}) }
    }
}
