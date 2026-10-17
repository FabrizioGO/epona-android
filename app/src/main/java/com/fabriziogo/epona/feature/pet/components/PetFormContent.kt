package com.fabriziogo.epona.feature.pet.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.form.ChoiceChipGroup
import com.fabriziogo.epona.core.ui.form.FormFieldLabel
import com.fabriziogo.epona.core.ui.form.FormSection
import com.fabriziogo.epona.core.ui.media.PhotoPickerSection
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.FormSpacing

/** What the pet form shows; the add and edit screens each map their own state onto it. */
data class PetFormState(
    val petName: String = "",
    val selectedSpecies: Species = Species.DOG,
    val breed: String = "",
    val color: String = "",
    val selectedSize: PetSize = PetSize.MEDIUM,
    val ageYears: String = "",
    val selectedGender: PetGender = PetGender.UNKNOWN,
    val microchipId: String = "",
    val description: String = "",
    val photos: List<PhotoItem> = emptyList(),
    @StringRes val nameError: Int? = null,
    @StringRes val ageError: Int? = null,
    /** False while a save is in flight, so the fields cannot change under it. */
    val enabled: Boolean = true
)

/** What the pet form can ask for; each screen routes these to its own event type. */
class PetFormActions(
    val onNameChanged: (String) -> Unit,
    val onSpeciesChanged: (Species) -> Unit,
    val onBreedChanged: (String) -> Unit,
    val onColorChanged: (String) -> Unit,
    val onSizeChanged: (PetSize) -> Unit,
    val onAgeChanged: (String) -> Unit,
    val onGenderChanged: (PetGender) -> Unit,
    val onMicrochipChanged: (String) -> Unit,
    val onDescriptionChanged: (String) -> Unit,
    val onPhotoRemoved: (PhotoItem) -> Unit,
    val onAddPhotoClick: () -> Unit
)

/**
 * The pet form shared by the add and edit screens, laid out inside `FormScaffold`'s scrolling
 * column. Stateless: it renders [state] and reports edits through [actions], so both screens
 * stay in step. [nameFocus] and [ageFocus] let the screen focus the first invalid field.
 */
@Composable
fun ColumnScope.PetFormContent(
    state: PetFormState,
    actions: PetFormActions,
    modifier: Modifier = Modifier,
    nameFocus: FocusRequester? = null,
    ageFocus: FocusRequester? = null
) {
    Spacer(Modifier.height(FormSpacing.FieldGap))

    PhotoPickerSection(
        photos = state.photos,
        onAddClick = actions.onAddPhotoClick,
        onRemove = actions.onPhotoRemoved,
        modifier = modifier
    )

    FormSection(title = stringResource(R.string.pet_section_info)) {
        EponaTextField(
            value = state.petName,
            onValueChange = actions.onNameChanged,
            label = stringResource(R.string.pet_name_label),
            placeholder = stringResource(R.string.pet_name_placeholder),
            errorText = state.nameError?.let { stringResource(it) },
            enabled = state.enabled,
            focusRequester = nameFocus,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        FormFieldLabel(stringResource(R.string.pet_species_label))
        ChoiceChipGroup(
            options = Species.entries,
            selected = state.selectedSpecies,
            onSelect = { it?.let(actions.onSpeciesChanged) },
            label = { stringResource(it.label) }
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        EponaTextField(
            value = state.breed,
            onValueChange = actions.onBreedChanged,
            label = stringResource(R.string.pet_breed_label),
            placeholder = stringResource(R.string.pet_breed_placeholder),
            optional = true,
            enabled = state.enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        EponaTextField(
            value = state.color,
            onValueChange = actions.onColorChanged,
            label = stringResource(R.string.pet_color_label),
            placeholder = stringResource(R.string.pet_color_placeholder),
            optional = true,
            enabled = state.enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormSection(title = stringResource(R.string.pet_section_physical_traits)) {
        FormFieldLabel(stringResource(R.string.pet_size_label))
        ChoiceChipGroup(
            options = PetSize.entries,
            selected = state.selectedSize,
            onSelect = { it?.let(actions.onSizeChanged) },
            label = { stringResource(it.label) }
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        FormFieldLabel(stringResource(R.string.pet_gender_label))
        ChoiceChipGroup(
            options = PetGender.entries,
            selected = state.selectedGender,
            onSelect = { it?.let(actions.onGenderChanged) },
            label = { stringResource(it.label) }
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        EponaTextField(
            value = state.ageYears,
            onValueChange = actions.onAgeChanged,
            label = stringResource(R.string.pet_age_label),
            placeholder = stringResource(R.string.pet_age_placeholder),
            keyboardType = KeyboardType.Number,
            errorText = state.ageError?.let { stringResource(it) },
            optional = true,
            enabled = state.enabled,
            focusRequester = ageFocus,
            modifier = Modifier.fillMaxWidth()
        )
    }

    FormSection(title = stringResource(R.string.pet_section_additional_info)) {
        EponaTextField(
            value = state.microchipId,
            onValueChange = actions.onMicrochipChanged,
            label = stringResource(R.string.pet_microchip_label),
            placeholder = stringResource(R.string.pet_optional_placeholder),
            optional = true,
            enabled = state.enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(FormSpacing.FieldGap))

        EponaTextField(
            value = state.description,
            onValueChange = actions.onDescriptionChanged,
            label = stringResource(R.string.pet_features_label),
            placeholder = stringResource(R.string.pet_description_placeholder),
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            optional = true,
            enabled = state.enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@EponaPreviews
@Composable
private fun PetFormContentPreview() {
    EponaTheme {
        androidx.compose.foundation.layout.Column {
            PetFormContent(
                state = PetFormState(petName = "Luna", nameError = null),
                actions = PetFormActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
            )
        }
    }
}

@EponaPreviews
@Composable
private fun PetFormContentErrorPreview() {
    EponaTheme {
        androidx.compose.foundation.layout.Column {
            PetFormContent(
                state = PetFormState(
                    nameError = R.string.pet_name_required,
                    ageError = R.string.pet_age_invalid,
                    ageYears = "99"
                ),
                actions = PetFormActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
            )
        }
    }
}
