@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.fabriziogo.epona.feature.pet

import com.fabriziogo.epona.feature.pet.components.PetFormState
import com.fabriziogo.epona.feature.pet.components.PetFormContent
import com.fabriziogo.epona.feature.pet.components.PetFormActions
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.layout.constrainedWidth
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.FullScreenProgress
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.feature.pet.components.PhotoPickerSection
import com.fabriziogo.epona.feature.pet.components.SelectionChips
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    // Pinned bar: picks up its tonal 'scrolled' color as the content scrolls under it.

    val topBarScroll = TopAppBarDefaults.pinnedScrollBehavior()


    Scaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            EponaTopAppBar(
                scrollBehavior = topBarScroll,
                title = stringResource(R.string.pet_edit_title),
                onCloseClick = requestExit,
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(EditPetEvent.SavePet) },
                        enabled = state.isFormValid && !state.isLoading
                    ) {
                        Text(stringResource(R.string.pet_update))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (state.isLoading) {
            FullScreenProgress(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        EditPetScreenContent(
            state = state,
            paddingValues = paddingValues,
            onEvent = onEvent,
            onAddPhotoClick = photoPicker::open
        )
    }
}

@Composable
fun EditPetScreenContent(
    state: EditPetUiState,
    paddingValues: PaddingValues,
    onEvent: (EditPetEvent) -> Unit,
    onAddPhotoClick: () -> Unit = {}
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
            nameError = state.nameError
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
        paddingValues = paddingValues
    )
}
@EponaPreviews
@Composable
fun EditPetScreenPreview() {
    EditPetScreenContent(
        state = EditPetUiState(),
        paddingValues = PaddingValues(),
        onEvent = {}
    )
}
