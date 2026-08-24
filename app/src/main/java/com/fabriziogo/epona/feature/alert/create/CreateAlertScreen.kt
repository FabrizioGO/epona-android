package com.fabriziogo.epona.feature.alert.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.feature.alert.create.steps.AlertTypeStep
import com.fabriziogo.epona.feature.alert.create.steps.ContactReviewStep
import com.fabriziogo.epona.feature.alert.create.steps.PetSelectionStep
import com.fabriziogo.epona.feature.alert.create.steps.PhotoLocationStep
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.permission.rememberLocationPermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToSuccess: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateAlertViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Asked when the user reaches for their location, not when the wizard opens.
    val locationPermission = rememberLocationPermissionState(
        onGranted = { viewModel.onEvent(CreateAlertEvent.UseCurrentLocation) },
        onDenied = { viewModel.onEvent(CreateAlertEvent.LocationPermissionDenied) }
    )

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                CreateAlertNavEvent.NavigateBack -> onNavigateBack()
                CreateAlertNavEvent.NavigateToAddPet -> onNavigateToAddPet()
                is CreateAlertNavEvent.NavigateToSuccess ->
                    onNavigateToSuccess(event.alertId)
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateAlertEvent.ErrorDismissed)
        }
    }

    Scaffold(
        topBar = {
            EponaTopAppBar(
                title = "Create Alert",
                onCloseClick = { viewModel.onEvent(CreateAlertEvent.CloseClicked) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // M3 progress indicator
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            // Animated step content
            AnimatedContent(
                targetState = state.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn())
                            .togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn())
                            .togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "step_animation",
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) { step ->
                when (step) {
                    1 -> AlertTypeStep(
                        selectedType = state.alertType,
                        onTypeSelected = {
                            viewModel.onEvent(CreateAlertEvent.AlertTypeSelected(it))
                        }
                    )
                    2 -> PetSelectionStep(
                        pets = state.myPets,
                        selectedPet = state.selectedPet,
                        isLoading = state.isLoadingPets,
                        onPetSelected = {
                            viewModel.onEvent(CreateAlertEvent.PetSelected(it))
                        },
                        onAddNewPet = {
                            viewModel.onEvent(CreateAlertEvent.AddNewPetClicked)
                        }
                    )
                    3 -> PhotoLocationStep(
                        photoUris = state.photoUris,
                        location = state.location,
                        address = state.address,
                        description = state.description,
                        isLoadingLocation = state.isLoadingLocation,
                        locationError = state.locationError,
                        onPhotoAdded = {
                            viewModel.onEvent(CreateAlertEvent.PhotoAdded(it))
                        },
                        onPhotoRemoved = {
                            viewModel.onEvent(CreateAlertEvent.PhotoRemoved(it))
                        },
                        onUseCurrentLocation = {
                            if (locationPermission.isGranted) {
                                viewModel.onEvent(CreateAlertEvent.UseCurrentLocation)
                            } else {
                                locationPermission.requestOrOpenAppSettings()
                            }
                        },
                        onLocationPicked = {
                            viewModel.onEvent(CreateAlertEvent.LocationPicked(it))
                        },
                        onDescriptionChanged = {
                            viewModel.onEvent(CreateAlertEvent.DescriptionChanged(it))
                        }
                    )
                    4 -> ContactReviewStep(
                        phone = state.contactPhone,
                        reward = state.reward,
                        alertType = state.alertType,
                        petName = state.selectedPet?.name ?: "",
                        address = state.address,
                        onPhoneChanged = {
                            viewModel.onEvent(CreateAlertEvent.PhoneChanged(it))
                        },
                        onRewardChanged = {
                            viewModel.onEvent(CreateAlertEvent.RewardChanged(it))
                        }
                    )
                }
            }

            // Bottom action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.currentStep > 1) {
                    EponaOutlinedButton(
                        text = "Back",
                        onClick = { viewModel.onEvent(CreateAlertEvent.PreviousStep) }
                    )
                }
                if (state.currentStep == state.totalSteps) {
                    EponaFilledButton(
                        text = "🚨 Publish Alert",
                        onClick = { viewModel.onEvent(CreateAlertEvent.PublishClicked) },
                        loading = state.isSubmitting,
                        enabled = state.canProceed,
                        fullWidth = true,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    EponaFilledButton(
                        text = "Continue",
                        onClick = { viewModel.onEvent(CreateAlertEvent.NextStep) },
                        enabled = state.canProceed,
                        fullWidth = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}