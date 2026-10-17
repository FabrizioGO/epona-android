@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.fabriziogo.epona.feature.adoption.create

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AgeStage
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.domain.usecase.adoption.ADOPTION_TITLE_MAX_LENGTH
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.ui.components.EponaConfirmDialog
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaFilterChip
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.components.EponaTopAppBar
import com.fabriziogo.epona.core.ui.layout.constrainedWidth
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.feature.pet.components.PhotoPickerSection
import com.fabriziogo.epona.feature.pet.components.SelectionChips
import kotlinx.coroutines.launch

@Composable
fun CreateAdoptionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateAdoptionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val cameraDeniedMessage = stringResource(R.string.photo_camera_denied)

    // Owned by the screen so previews of the form need no ActivityResultRegistry.
    val photoPicker = rememberMediaPickerState(
        remainingSlots = MAX_PHOTOS_PER_ENTITY - state.photos.size,
        onUrisPicked = { uris -> viewModel.onEvent(CreateAdoptionEvent.PhotosPicked(uris)) },
        onCameraDenied = { scope.launch { snackbarHostState.showSnackbar(cameraDeniedMessage) } }
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
                CreateAdoptionNavEvent.NavigateBack -> onNavigateBack()
                is CreateAdoptionNavEvent.NavigateToDetail -> onNavigateToDetail(event.postId)
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CreateAdoptionEvent.ErrorDismissed)
        }
    }

    var showDiscardDialog by remember { mutableStateOf(false) }
    val requestClose = {
        if (state.isDirty) showDiscardDialog = true else viewModel.onEvent(CreateAdoptionEvent.CloseClicked)
    }

    BackHandler(enabled = state.isDirty) { showDiscardDialog = true }

    if (showDiscardDialog) {
        EponaConfirmDialog(
            title = stringResource(R.string.discard_title),
            message = stringResource(R.string.discard_message),
            confirmText = stringResource(R.string.discard_confirm),
            dismissText = stringResource(R.string.discard_keep_editing),
            onConfirm = {
                showDiscardDialog = false
                viewModel.onEvent(CreateAdoptionEvent.CloseClicked)
            },
            onDismiss = { showDiscardDialog = false }
        )
    }

    CreateAdoptionContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onAddPhotosClick = photoPicker::open,
        onCloseClick = requestClose,
        modifier = modifier
    )
}

@Composable
fun CreateAdoptionContent(
    state: CreateAdoptionUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CreateAdoptionEvent) -> Unit,
    onAddPhotosClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topBarScroll = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(topBarScroll.nestedScrollConnection),
        topBar = {
            EponaTopAppBar(
                scrollBehavior = topBarScroll,
                title = stringResource(R.string.adoption_create_title),
                onCloseClick = onCloseClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                EponaFilledButton(
                    text = stringResource(R.string.adoption_publish),
                    onClick = { onEvent(CreateAdoptionEvent.PublishClicked) },
                    loading = state.isSubmitting,
                    fullWidth = true,
                    modifier = Modifier
                        .constrainedWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .constrainedWidth()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.adoption_create_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))
            PhotoPickerSection(
                photos = state.photos,
                onAddClick = onAddPhotosClick,
                onRemove = { onEvent(CreateAdoptionEvent.PhotoRemoved(it)) }
            )

            Spacer(Modifier.height(24.dp))
            EponaTextField(
                value = state.title,
                onValueChange = { onEvent(CreateAdoptionEvent.TitleChanged(it)) },
                label = stringResource(R.string.adoption_field_title),
                placeholder = stringResource(R.string.adoption_field_title_placeholder),
                supportingText = "${state.title.length}/$ADOPTION_TITLE_MAX_LENGTH",
                errorText = if (state.showErrors && !state.isTitleValid) {
                    stringResource(R.string.adoption_title_required)
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle(stringResource(R.string.pet_species_label))
            Spacer(Modifier.height(8.dp))
            SelectionChips(
                options = Species.entries,
                selected = state.species,
                label = { stringResource(it.label) },
                onSelected = { onEvent(CreateAdoptionEvent.SpeciesChanged(it)) }
            )

            Spacer(Modifier.height(16.dp))
            EponaTextField(
                value = state.breed,
                onValueChange = { onEvent(CreateAdoptionEvent.BreedChanged(it)) },
                label = stringResource(R.string.pet_breed_label),
                placeholder = stringResource(R.string.pet_breed_placeholder),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            SectionTitle(stringResource(R.string.adoption_section_about))
            Spacer(Modifier.height(12.dp))
            EponaTextField(
                value = state.petName,
                onValueChange = { onEvent(CreateAdoptionEvent.PetNameChanged(it)) },
                label = stringResource(R.string.adoption_field_name),
                supportingText = stringResource(R.string.adoption_field_name_hint),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            FieldLabel(stringResource(R.string.adoption_field_age))
            Spacer(Modifier.height(8.dp))
            OptionalChips(
                options = AgeStage.entries,
                selected = state.ageStage,
                label = { stringResource(it.label(state.species)) },
                onSelected = { onEvent(CreateAdoptionEvent.AgeStageChanged(it)) }
            )

            Spacer(Modifier.height(16.dp))
            FieldLabel(stringResource(R.string.pet_size_label))
            Spacer(Modifier.height(8.dp))
            OptionalChips(
                options = PetSize.entries,
                selected = state.size,
                label = { stringResource(it.label) },
                onSelected = { onEvent(CreateAdoptionEvent.SizeChanged(it)) }
            )

            Spacer(Modifier.height(16.dp))
            FieldLabel(stringResource(R.string.pet_gender_label))
            Spacer(Modifier.height(8.dp))
            OptionalChips(
                options = PetGender.entries,
                selected = state.gender,
                label = { stringResource(it.label) },
                onSelected = { onEvent(CreateAdoptionEvent.GenderChanged(it)) }
            )

            Spacer(Modifier.height(24.dp))
            EponaTextField(
                value = state.description,
                onValueChange = { onEvent(CreateAdoptionEvent.DescriptionChanged(it)) },
                label = stringResource(R.string.adoption_field_description),
                placeholder = stringResource(R.string.adoption_field_description_placeholder),
                singleLine = false,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            EponaTextField(
                value = state.price,
                onValueChange = { onEvent(CreateAdoptionEvent.PriceChanged(it)) },
                label = stringResource(R.string.adoption_field_price),
                placeholder = stringResource(R.string.adoption_field_price_placeholder),
                supportingText = stringResource(R.string.adoption_field_price_hint),
                errorText = if (!state.isPriceValid) stringResource(R.string.adoption_price_invalid) else null,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
            EponaTextField(
                value = state.phone,
                onValueChange = { onEvent(CreateAdoptionEvent.PhoneChanged(it)) },
                label = stringResource(R.string.create_contact_phone),
                placeholder = stringResource(R.string.create_contact_phone_placeholder),
                supportingText = stringResource(R.string.adoption_field_phone_hint),
                errorText = if (state.showErrors && !state.isPhoneValid) {
                    stringResource(R.string.adoption_phone_required)
                } else null,
                leadingIcon = { androidx.compose.material3.Icon(Icons.Outlined.Phone, contentDescription = null) },
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Chip row where nothing is selected by default and tapping the selected chip clears it —
 * these fields are optional, unlike [SelectionChips], which always has a value.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T : Enum<T>> OptionalChips(
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelected: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            EponaFilterChip(
                label = label(option),
                selected = option == selected,
                onClick = { onSelected(if (option == selected) null else option) }
            )
        }
    }
}

@EponaPreviews
@Composable
private fun CreateAdoptionContentPreview() {
    EponaTheme {
        CreateAdoptionContent(
            state = CreateAdoptionUiState(
                title = "Golden Retriever puppies in adoption",
                species = Species.DOG,
                ageStage = AgeStage.BABY,
                price = "50",
                phone = "+1 555 000 0000",
                photos = listOf(PhotoItem.Remote("https://example.com/a.jpg"))
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onAddPhotosClick = {},
            onCloseClick = {}
        )
    }
}
