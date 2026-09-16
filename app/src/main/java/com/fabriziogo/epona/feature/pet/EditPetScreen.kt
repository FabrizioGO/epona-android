package com.fabriziogo.epona.feature.pet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
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

    Scaffold(
        modifier = modifier,
        topBar = {
            EponaTopAppBar(
                title = stringResource(R.string.pet_edit_title),
                onCloseClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        EditPetScreenContent(
            state = state,
            paddingValues = paddingValues,
            onEvent = { viewModel.onEvent(it) },
            onCancel = onNavigateBack,
            onAddPhotoClick = photoPicker::open
        )
    }
}

@Composable
fun EditPetScreenContent(
    state: EditPetUiState,
    paddingValues: PaddingValues,
    onEvent: (EditPetEvent) -> Unit,
    onCancel: () -> Unit,
    onAddPhotoClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ---- Basic Info Section ----
            Text(
                text = stringResource(R.string.pet_section_info),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Pet name (required)
            EponaTextField(
                value = state.petName,
                onValueChange = { onEvent(EditPetEvent.NameChanged(it)) },
                label = stringResource(R.string.pet_name_label),
                placeholder = stringResource(R.string.pet_name_placeholder),
                errorText = state.nameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Species selection
            Text(
                text = stringResource(R.string.pet_species_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChips(
                options = Species.entries,
                selected = state.selectedSpecies,
                label = { stringResource(it.label) },
                onSelected = { onEvent(EditPetEvent.SpeciesChanged(it)) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Breed
            EponaTextField(
                value = state.breed,
                onValueChange = { onEvent(EditPetEvent.BreedChanged(it)) },
                label = stringResource(R.string.pet_breed_label),
                placeholder = stringResource(R.string.pet_breed_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Color
            EponaTextField(
                value = state.color,
                onValueChange = { onEvent(EditPetEvent.ColorChanged(it)) },
                label = stringResource(R.string.pet_color_label),
                placeholder = stringResource(R.string.pet_color_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Physical Traits Section ----
            Text(
                text = stringResource(R.string.pet_section_physical_traits),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Size selection
            Text(
                text = stringResource(R.string.pet_size_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChips(
                options = PetSize.entries,
                selected = state.selectedSize,
                label = { stringResource(it.label) },
                onSelected = { onEvent(EditPetEvent.SizeChanged(it)) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Age
            EponaTextField(
                value = state.ageYears,
                onValueChange = { onEvent(EditPetEvent.AgeChanged(it)) },
                label = stringResource(R.string.pet_age_label),
                placeholder = stringResource(R.string.pet_age_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Gender selection
            Text(
                text = stringResource(R.string.pet_gender_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChips(
                options = PetGender.entries,
                selected = state.selectedGender,
                label = { stringResource(it.label) },
                onSelected = { onEvent(EditPetEvent.GenderChanged(it)) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Additional Info Section ----
            Text(
                text = stringResource(R.string.pet_section_additional_info),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Microchip ID
            EponaTextField(
                value = state.microchipId,
                onValueChange = { onEvent(EditPetEvent.MicrochipIdChanged(it)) },
                label = stringResource(R.string.pet_microchip_label),
                placeholder = stringResource(R.string.pet_optional_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Description
            EponaTextField(
                value = state.description,
                onValueChange = { onEvent(EditPetEvent.DescriptionChanged(it)) },
                label = stringResource(R.string.create_description),
                placeholder = stringResource(R.string.pet_description_placeholder),
                singleLine = false,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Photos Section ----
            PhotoPickerSection(
                photos = state.photos,
                onAddClick = onAddPhotoClick,
                onRemove = { onEvent(EditPetEvent.PhotoRemoved(it)) },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // ---- Action Buttons ----
            EponaFilledButton(
                text = stringResource(R.string.pet_update),
                onClick = { onEvent(EditPetEvent.SavePet) },
                enabled = state.isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            EponaOutlinedButton(
                text = stringResource(R.string.action_cancel),
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditPetScreenPreview() {
    EditPetScreenContent(
        state = EditPetUiState(),
        paddingValues = PaddingValues(),
        onEvent = {},
        onCancel = {}
    )
}
