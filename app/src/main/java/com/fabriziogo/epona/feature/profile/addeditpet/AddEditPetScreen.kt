package com.fabriziogo.epona.feature.profile.addeditpet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaFilterChip
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.feature.profile.addeditpet.components.PetPhotoPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPetScreen(
    onNavigateBack: () -> Unit,
    onNavigateBackWithSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddEditPetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                AddEditPetNavEvent.NavigateBack -> onNavigateBack()
                AddEditPetNavEvent.NavigateBackWithSuccess -> onNavigateBackWithSuccess()
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(AddEditPetEvent.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            EponaTopAppBar(
                title = if (state.isEditMode) "Edit Pet" else "Add Pet",
                onBackClick = { viewModel.onEvent(AddEditPetEvent.BackClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            PetPhotoPicker(
                photoUrl = state.photoUrl,
                onAddPhoto = { viewModel.onEvent(AddEditPetEvent.PhotoAdded(it)) },
                onRemovePhoto = { viewModel.onEvent(AddEditPetEvent.PhotoRemoved) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))

            EponaTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(AddEditPetEvent.NameChanged(it)) },
                label = "Pet name",
                errorText = state.nameError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Species",
                style = EponaTypography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Species.entries.forEach { species ->
                    EponaFilterChip(
                        label = species.label,
                        selected = state.species == species,
                        onClick = { viewModel.onEvent(AddEditPetEvent.SpeciesSelected(species)) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EponaTextField(
                    value = state.breed,
                    onValueChange = { viewModel.onEvent(AddEditPetEvent.BreedChanged(it)) },
                    label = "Breed (optional)",
                    modifier = Modifier.weight(1f)
                )
                EponaTextField(
                    value = state.color,
                    onValueChange = { viewModel.onEvent(AddEditPetEvent.ColorChanged(it)) },
                    label = "Color (optional)",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Size",
                style = EponaTypography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PetSize.entries.forEach { size ->
                    EponaFilterChip(
                        label = size.label,
                        selected = state.size == size,
                        onClick = { viewModel.onEvent(AddEditPetEvent.SizeSelected(size)) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Gender",
                style = EponaTypography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PetGender.entries.forEach { gender ->
                    EponaFilterChip(
                        label = gender.label,
                        selected = state.gender == gender,
                        onClick = { viewModel.onEvent(AddEditPetEvent.GenderSelected(gender)) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EponaTextField(
                    value = state.ageYears,
                    onValueChange = { viewModel.onEvent(AddEditPetEvent.AgeChanged(it)) },
                    label = "Age (years)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                EponaTextField(
                    value = state.microchipId,
                    onValueChange = { viewModel.onEvent(AddEditPetEvent.MicrochipChanged(it)) },
                    label = "Microchip ID (optional)",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            EponaTextField(
                value = state.description,
                onValueChange = { viewModel.onEvent(AddEditPetEvent.DescriptionChanged(it)) },
                label = "Description (optional)",
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))

            EponaFilledButton(
                text = if (state.isEditMode) "Save Changes" else "Add Pet",
                onClick = { viewModel.onEvent(AddEditPetEvent.SaveClicked) },
                loading = state.isSaving,
                fullWidth = true
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
