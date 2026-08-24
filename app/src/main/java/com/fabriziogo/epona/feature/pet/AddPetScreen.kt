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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.feature.pet.components.PhotoPickerSection
import com.fabriziogo.epona.feature.pet.components.SelectionChips

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

    Scaffold(
        modifier = modifier,
        topBar = {
            EponaTopAppBar(
                title = "Add Pet",
                onCloseClick = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        AddPetScreenContent(
            state = state,
            paddingValues = paddingValues,
            onEvent = { viewModel.onEvent(it) },
            onCancel = onNavigateBack
        )
    }
}

@Composable
fun AddPetScreenContent(
    state: AddPetUiState,
    paddingValues: PaddingValues,
    onEvent: (AddPetEvent) -> Unit,
    onCancel: () -> Unit
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
                text = "Pet Information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Pet name (required)
            EponaTextField(
                value = state.petName,
                onValueChange = { onEvent(AddPetEvent.NameChanged(it)) },
                label = "Pet Name",
                placeholder = "e.g., Buddy, Luna",
                errorText = state.nameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Species selection
            Text(
                text = "Species",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChips(
                options = Species.entries,
                selected = state.selectedSpecies,
                label = { it.label },
                onSelected = { onEvent(AddPetEvent.SpeciesChanged(it)) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Breed
            EponaTextField(
                value = state.breed,
                onValueChange = { onEvent(AddPetEvent.BreedChanged(it)) },
                label = "Breed",
                placeholder = "e.g., Golden Retriever",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Color
            EponaTextField(
                value = state.color,
                onValueChange = { onEvent(AddPetEvent.ColorChanged(it)) },
                label = "Color",
                placeholder = "e.g., Brown, White with spots",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Physical Traits Section ----
            Text(
                text = "Physical Traits",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Size selection
            Text(
                text = "Size",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChips(
                options = PetSize.entries,
                selected = state.selectedSize,
                label = { it.label },
                onSelected = { onEvent(AddPetEvent.SizeChanged(it)) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Age
            EponaTextField(
                value = state.ageYears,
                onValueChange = { onEvent(AddPetEvent.AgeChanged(it)) },
                label = "Age (years)",
                placeholder = "e.g., 3",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Gender selection
            Text(
                text = "Gender",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SelectionChips(
                options = PetGender.entries,
                selected = state.selectedGender,
                label = { it.label },
                onSelected = { onEvent(AddPetEvent.GenderChanged(it)) },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Additional Info Section ----
            Text(
                text = "Additional Information",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Microchip ID
            EponaTextField(
                value = state.microchipId,
                onValueChange = { onEvent(AddPetEvent.MicrochipIdChanged(it)) },
                label = "Microchip ID",
                placeholder = "Optional",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Description
            EponaTextField(
                value = state.description,
                onValueChange = { onEvent(AddPetEvent.DescriptionChanged(it)) },
                label = "Description",
                placeholder = "Add any distinctive features or markings",
                singleLine = false,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Photos Section ----
            PhotoPickerSection(
                photoUrls = state.photoUrls,
                onPhotosSelected = { urls ->
                    onEvent(AddPetEvent.PhotosSelected(urls))
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // ---- Action Buttons ----
            EponaFilledButton(
                text = "Save Pet",
                onClick = { onEvent(AddPetEvent.SavePet) },
                enabled = state.isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            EponaOutlinedButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPetScreenPreview() {
    AddPetScreenContent(
        state = AddPetUiState(),
        paddingValues = PaddingValues(),
        onEvent = {},
        onCancel = {}
    )
}
