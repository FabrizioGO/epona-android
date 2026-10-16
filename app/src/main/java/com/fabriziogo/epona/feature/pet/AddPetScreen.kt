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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
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

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(AddPetEvent.ErrorDismissed)
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

    // Pinned bar: picks up its tonal 'scrolled' color as the content scrolls under it.

    val topBarScroll = TopAppBarDefaults.pinnedScrollBehavior()


    Scaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            EponaTopAppBar(
                scrollBehavior = topBarScroll,
                title = stringResource(R.string.create_add_pet),
                onCloseClick = requestExit,
                // Full-screen form pattern: Close on the left, the one primary action on the right.
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(AddPetEvent.SavePet) },
                        enabled = state.isFormValid && !state.isLoading
                    ) {
                        Text(stringResource(R.string.pet_save))
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

        AddPetScreenContent(
            state = state,
            paddingValues = paddingValues,
            onEvent = { viewModel.onEvent(it) },
            onAddPhotoClick = photoPicker::open
        )
    }
}

@Composable
fun AddPetScreenContent(
    state: AddPetUiState,
    paddingValues: PaddingValues,
    onEvent: (AddPetEvent) -> Unit,
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
        paddingValues = paddingValues
    )
}
@EponaPreviews
@Composable
fun AddPetScreenPreview() {
    AddPetScreenContent(
        state = AddPetUiState(),
        paddingValues = PaddingValues(),
        onEvent = {}
    )
}
