@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.fabriziogo.epona.feature.alert.create

import com.fabriziogo.epona.core.ui.text.ErrorSnackbarEffect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.TopAppBarDefaults
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import com.fabriziogo.epona.core.ui.form.FormBottomBar
import com.fabriziogo.epona.core.ui.theme.FormSpacing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.feature.alert.create.steps.AlertTypeStep
import com.fabriziogo.epona.feature.alert.create.steps.ContactReviewStep
import com.fabriziogo.epona.feature.alert.create.steps.FoundPetStep
import com.fabriziogo.epona.feature.alert.create.steps.LocationStep
import com.fabriziogo.epona.feature.alert.create.steps.MatchesStep
import com.fabriziogo.epona.feature.alert.create.steps.PetSelectionStep
import com.fabriziogo.epona.core.ui.layout.constrainedWidth
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.core.ui.permission.rememberLocationPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToCreateAdoption: () -> Unit,
    onNavigateToSuccess: (String) -> Unit,
    onNavigateToMatch: (String) -> Unit,
    onNavigateToPickLocation: (Location?) -> Unit,
    pickedLocation: Location?,
    onPickedLocationConsumed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateAlertViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cameraDeniedMessage = stringResource(R.string.photo_camera_denied)

    // Asked when the user reaches for their location, not when the wizard opens.
    // Note: keep this as plain remember (not rememberSaveable). The composition is
    // disposed while the map picker is pushed, so on return it is reconstructed
    // already-granted and does not re-fire UseCurrentLocation over the user's pin.
    val locationPermission = rememberLocationPermissionState(
        onGranted = { viewModel.onEvent(CreateAlertEvent.UseCurrentLocation) },
        onDenied = { viewModel.onEvent(CreateAlertEvent.LocationPermissionDenied) }
    )

    LaunchedEffect(pickedLocation) {
        pickedLocation?.let {
            viewModel.onEvent(CreateAlertEvent.LocationPicked(it))
            onPickedLocationConsumed()
        }
    }

    // Owned by the screen rather than by FoundPetStep, so previews of the step
    // do not need an ActivityResultRegistry to render.
    val photoPicker = rememberMediaPickerState(
        remainingSlots = MAX_PHOTOS_PER_ENTITY - state.foundPet.photos.size,
        onUrisPicked = { uris -> viewModel.onEvent(CreateAlertEvent.FoundPhotosPicked(uris)) },
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
                CreateAlertNavEvent.NavigateBack -> onNavigateBack()
                CreateAlertNavEvent.NavigateToAddPet -> onNavigateToAddPet()
                CreateAlertNavEvent.NavigateToCreateAdoption -> onNavigateToCreateAdoption()
                is CreateAlertNavEvent.NavigateToSuccess ->
                    onNavigateToSuccess(event.alertId)
                is CreateAlertNavEvent.NavigateToMatch ->
                    onNavigateToMatch(event.alertId)
            }
        }
    }

    ErrorSnackbarEffect(state.error, snackbarHostState) { viewModel.onEvent(CreateAlertEvent.ErrorDismissed) }

    // Past the first step the user has made choices; leaving should not silently drop them.
    var showDiscardDialog by remember { mutableStateOf(false) }
    val hasProgress = state.currentStep > 1

    // Back steps through the wizard instead of popping the whole flow.
    BackHandler(enabled = hasProgress) {
        viewModel.onEvent(CreateAlertEvent.PreviousStep)
    }

    if (showDiscardDialog) {
        EponaConfirmDialog(
            title = stringResource(R.string.discard_title),
            message = stringResource(R.string.discard_message),
            confirmText = stringResource(R.string.discard_confirm),
            dismissText = stringResource(R.string.discard_keep_editing),
            onConfirm = {
                showDiscardDialog = false
                viewModel.onEvent(CreateAlertEvent.CloseClicked)
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    // Pinned bar: picks up its tonal 'scrolled' color as the content scrolls under it.
    val topBarScroll = TopAppBarDefaults.pinnedScrollBehavior()

    val isFinalStep = state.currentStep == state.totalSteps

    Scaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            EponaTopAppBar(
                scrollBehavior = topBarScroll,
                title = stringResource(R.string.create_alert_title),
                onCloseClick = {
                    if (hasProgress) showDiscardDialog = true
                    else viewModel.onEvent(CreateAlertEvent.CloseClicked)
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            FormBottomBar(
                primaryText = if (isFinalStep) {
                    stringResource(
                        if (state.alertType == AlertType.FOUND) {
                            R.string.create_found_publish
                        } else {
                            R.string.create_publish
                        }
                    )
                } else {
                    stringResource(R.string.create_continue)
                },
                onPrimary = {
                    viewModel.onEvent(
                        if (isFinalStep) CreateAlertEvent.PublishClicked else CreateAlertEvent.NextStep
                    )
                },
                loading = state.isSubmitting,
                secondaryText = if (state.currentStep > 1) stringResource(R.string.create_back) else null,
                onSecondary = { viewModel.onEvent(CreateAlertEvent.PreviousStep) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            val stepDescription = stringResource(
                R.string.create_step_progress, state.currentStep, state.totalSteps
            )
            Text(
                text = stepDescription,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .constrainedWidth()
                    .padding(horizontal = FormSpacing.Gutter)
                    .padding(top = 8.dp, bottom = 8.dp)
            )
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .constrainedWidth()
                    .padding(horizontal = FormSpacing.Gutter)
                    .semantics { stateDescription = stepDescription },
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
                modifier = Modifier.weight(1f)
            ) { step ->
                // One scroll state per step, so each starts at the top.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .constrainedWidth()
                        .padding(horizontal = FormSpacing.Gutter)
                ) {
                    when (step) {
                        1 -> AlertTypeStep(
                            selectedType = state.alertType,
                            onTypeSelected = {
                                viewModel.onEvent(CreateAlertEvent.AlertTypeSelected(it))
                            },
                            onAdoptionClick = {
                                viewModel.onEvent(CreateAlertEvent.AdoptionSelected)
                            }
                        )
                        2 -> if (state.alertType == AlertType.LOST) {
                            PetSelectionStep(
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
                        } else {
                            FoundPetStep(
                                form = state.foundPet,
                                onSpeciesChanged = {
                                    viewModel.onEvent(CreateAlertEvent.FoundPetSpeciesChanged(it))
                                },
                                onBreedChanged = {
                                    viewModel.onEvent(CreateAlertEvent.FoundPetBreedChanged(it))
                                },
                                onColorChanged = {
                                    viewModel.onEvent(CreateAlertEvent.FoundPetColorChanged(it))
                                },
                                onSizeChanged = {
                                    viewModel.onEvent(CreateAlertEvent.FoundPetSizeChanged(it))
                                },
                                onDescriptionChanged = {
                                    viewModel.onEvent(CreateAlertEvent.FoundPetDescriptionChanged(it))
                                },
                                onAddPhotosClick = { photoPicker.open() },
                                onPhotoRemove = {
                                    viewModel.onEvent(CreateAlertEvent.FoundPhotoRemoved(it))
                                }
                            )
                        }
                        3 -> LocationStep(
                            location = state.location,
                            address = state.address,
                            description = state.description,
                            isLoadingLocation = state.isLoadingLocation,
                            locationError = state.locationError?.let { stringResource(it) },
                            onUseCurrentLocation = {
                                if (locationPermission.isGranted) {
                                    viewModel.onEvent(CreateAlertEvent.UseCurrentLocation)
                                } else {
                                    locationPermission.requestOrOpenAppSettings()
                                }
                            },
                            onOpenMapPicker = { onNavigateToPickLocation(state.location) },
                            onDescriptionChanged = {
                                viewModel.onEvent(CreateAlertEvent.DescriptionChanged(it))
                            },
                            showCustody = state.alertType == AlertType.FOUND,
                            custody = state.custody,
                            onCustodySelected = {
                                viewModel.onEvent(CreateAlertEvent.CustodySelected(it))
                            }
                        )
                        4 -> MatchesStep(
                            matches = state.matches,
                            isLoading = state.isLoadingMatches,
                            error = state.matchesError?.asString(),
                            lookingFor = if (state.alertType == AlertType.LOST) {
                                AlertType.FOUND
                            } else {
                                AlertType.LOST
                            },
                            onMatchClick = {
                                viewModel.onEvent(CreateAlertEvent.MatchSelected(it))
                            }
                        )
                        5 -> ContactReviewStep(
                            phone = state.contactPhone,
                            reward = state.reward,
                            alertType = state.alertType,
                            petName = if (state.alertType == AlertType.LOST) {
                                state.selectedPet?.name ?: ""
                            } else {
                                stringResource(state.foundPet.species.foundLabel)
                            },
                            address = state.address,
                            custody = state.custody,
                            onPhoneChanged = {
                                viewModel.onEvent(CreateAlertEvent.PhoneChanged(it))
                            },
                            onRewardChanged = {
                                viewModel.onEvent(CreateAlertEvent.RewardChanged(it))
                            }
                        )
                    }

                    // Why Continue did nothing; live so a screen reader announces it.
                    val blocker = state.stepBlocker
                    if (state.showStepError && blocker != null && step == state.currentStep) {
                        Text(
                            text = stringResource(blocker),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(top = FormSpacing.FieldGap)
                                .semantics { liveRegion = LiveRegionMode.Polite }
                        )
                    }
                    Spacer(Modifier.height(FormSpacing.SectionGap))
                }
            }
        }
    }
}
