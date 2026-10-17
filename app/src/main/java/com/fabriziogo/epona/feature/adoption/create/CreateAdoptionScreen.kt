package com.fabriziogo.epona.feature.adoption.create

import com.fabriziogo.epona.core.ui.text.ErrorSnackbarEffect
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.form.ChoiceChipGroup
import com.fabriziogo.epona.core.ui.form.FormBottomBar
import com.fabriziogo.epona.core.ui.form.FormFieldLabel
import com.fabriziogo.epona.core.ui.form.FormScaffold
import com.fabriziogo.epona.core.ui.form.FormSection
import com.fabriziogo.epona.core.ui.media.PhotoPickerSection
import com.fabriziogo.epona.core.ui.media.PhotoSourceSheet
import com.fabriziogo.epona.core.ui.media.rememberMediaPickerState
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.FormSpacing
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

    ErrorSnackbarEffect(state.error, snackbarHostState) { viewModel.onEvent(CreateAdoptionEvent.ErrorDismissed) }

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

    // The first Publish tap turns errors on; focus the first invalid field then.
    val titleFocus = remember { FocusRequester() }
    val phoneFocus = remember { FocusRequester() }
    LaunchedEffect(state.showErrors) {
        if (!state.showErrors) return@LaunchedEffect
        when {
            !state.isTitleValid -> titleFocus.requestFocus()
            !state.isPhoneValid -> phoneFocus.requestFocus()
        }
    }

    CreateAdoptionContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onAddPhotosClick = photoPicker::open,
        onCloseClick = requestClose,
        modifier = modifier,
        titleFocus = titleFocus,
        phoneFocus = phoneFocus
    )
}

@Composable
fun CreateAdoptionContent(
    state: CreateAdoptionUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (CreateAdoptionEvent) -> Unit,
    onAddPhotosClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    titleFocus: FocusRequester? = null,
    phoneFocus: FocusRequester? = null
) {
    FormScaffold(
        title = stringResource(R.string.adoption_create_title),
        onClose = onCloseClick,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        bottomBar = {
            FormBottomBar(
                primaryText = stringResource(R.string.adoption_publish),
                onPrimary = { onEvent(CreateAdoptionEvent.PublishClicked) },
                loading = state.isSubmitting
            )
        }
    ) {
        CreateAdoptionFields(
            state = state,
            onEvent = onEvent,
            onAddPhotosClick = onAddPhotosClick,
            titleFocus = titleFocus,
            phoneFocus = phoneFocus
        )
    }
}

@Composable
private fun ColumnScope.CreateAdoptionFields(
    state: CreateAdoptionUiState,
    onEvent: (CreateAdoptionEvent) -> Unit,
    onAddPhotosClick: () -> Unit,
    titleFocus: FocusRequester?,
    phoneFocus: FocusRequester?
) {
    val enabled = !state.isSubmitting

    Text(
        text = stringResource(R.string.adoption_create_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(FormSpacing.SectionGap))

    PhotoPickerSection(
        photos = state.photos,
        onAddClick = onAddPhotosClick,
        onRemove = { onEvent(CreateAdoptionEvent.PhotoRemoved(it)) }
    )

    FormSection(title = stringResource(R.string.pet_section_info)) {
        EponaTextField(
            value = state.title,
            onValueChange = { onEvent(CreateAdoptionEvent.TitleChanged(it)) },
            label = stringResource(R.string.adoption_field_title),
            placeholder = stringResource(R.string.adoption_field_title_placeholder),
            characterCount = state.title.length to ADOPTION_TITLE_MAX_LENGTH,
            errorText = if (state.showErrors && !state.isTitleValid) {
                stringResource(R.string.adoption_title_required)
            } else null,
            enabled = enabled,
            focusRequester = titleFocus,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        FormFieldLabel(stringResource(R.string.pet_species_label))
        ChoiceChipGroup(
            options = Species.entries,
            selected = state.species,
            onSelect = { it?.let { species -> onEvent(CreateAdoptionEvent.SpeciesChanged(species)) } },
            label = { stringResource(it.label) }
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        EponaTextField(
            value = state.breed,
            onValueChange = { onEvent(CreateAdoptionEvent.BreedChanged(it)) },
            label = stringResource(R.string.pet_breed_label),
            placeholder = stringResource(R.string.pet_breed_placeholder),
            optional = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormSection(title = stringResource(R.string.adoption_section_about)) {
        EponaTextField(
            value = state.petName,
            onValueChange = { onEvent(CreateAdoptionEvent.PetNameChanged(it)) },
            label = stringResource(R.string.adoption_field_name),
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        FormFieldLabel(stringResource(R.string.adoption_field_age), optional = true)
        ChoiceChipGroup(
            options = AgeStage.entries,
            selected = state.ageStage,
            onSelect = { onEvent(CreateAdoptionEvent.AgeStageChanged(it)) },
            label = { stringResource(it.label(state.species)) },
            allowDeselect = true
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        FormFieldLabel(stringResource(R.string.pet_size_label), optional = true)
        ChoiceChipGroup(
            options = PetSize.entries,
            selected = state.size,
            onSelect = { onEvent(CreateAdoptionEvent.SizeChanged(it)) },
            label = { stringResource(it.label) },
            allowDeselect = true
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        FormFieldLabel(stringResource(R.string.pet_gender_label), optional = true)
        ChoiceChipGroup(
            options = PetGender.entries,
            selected = state.gender,
            onSelect = { onEvent(CreateAdoptionEvent.GenderChanged(it)) },
            label = { stringResource(it.label) },
            allowDeselect = true
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        EponaTextField(
            value = state.description,
            onValueChange = { onEvent(CreateAdoptionEvent.DescriptionChanged(it)) },
            label = stringResource(R.string.adoption_field_description),
            placeholder = stringResource(R.string.adoption_field_description_placeholder),
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormSection(title = stringResource(R.string.adoption_section_contact)) {
        EponaTextField(
            value = state.price,
            onValueChange = { onEvent(CreateAdoptionEvent.PriceChanged(it)) },
            label = stringResource(R.string.adoption_field_price),
            placeholder = stringResource(R.string.adoption_field_price_placeholder),
            supportingText = stringResource(R.string.adoption_field_price_hint),
            errorText = if (!state.isPriceValid) stringResource(R.string.adoption_price_invalid) else null,
            keyboardType = KeyboardType.Decimal,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        EponaTextField(
            value = state.phone,
            onValueChange = { onEvent(CreateAdoptionEvent.PhoneChanged(it)) },
            label = stringResource(R.string.create_contact_phone),
            placeholder = stringResource(R.string.create_contact_phone_placeholder),
            supportingText = stringResource(R.string.adoption_field_phone_hint),
            errorText = if (state.showErrors && !state.isPhoneValid) {
                stringResource(R.string.adoption_phone_required)
            } else null,
            leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done,
            enabled = enabled,
            focusRequester = phoneFocus,
            modifier = Modifier.fillMaxWidth()
        )
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
                phone = "0000-000-0000",
                photos = listOf(PhotoItem.Remote("https://example.com/a.jpg"))
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onAddPhotosClick = {},
            onCloseClick = {}
        )
    }
}

@EponaPreviews
@Composable
private fun CreateAdoptionContentErrorPreview() {
    EponaTheme {
        CreateAdoptionContent(
            state = CreateAdoptionUiState(showErrors = true),
            snackbarHostState = remember { SnackbarHostState() },
            onEvent = {},
            onAddPhotosClick = {},
            onCloseClick = {}
        )
    }
}
