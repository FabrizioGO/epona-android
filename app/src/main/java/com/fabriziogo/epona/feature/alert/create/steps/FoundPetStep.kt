package com.fabriziogo.epona.feature.alert.create.steps

import com.fabriziogo.epona.feature.alert.create.steps.components.StepHeader
import com.fabriziogo.epona.core.ui.form.ChoiceChipGroup
import com.fabriziogo.epona.core.ui.form.FormFieldLabel
import com.fabriziogo.epona.core.ui.form.FormSection
import com.fabriziogo.epona.core.ui.theme.FormSpacing
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.feature.alert.create.FoundPetForm
import com.fabriziogo.epona.core.ui.media.PhotoPickerSection

/**
 * Step 2 (FOUND): describe the stray. Deliberately no name, gender, age or
 * microchip — a finder cannot know those. Reuses the pet form's chips and the
 * working picker stack (PhotoPickerSection + PhotoThumbnailRow).
 */
@Composable
fun FoundPetStep(
    form: FoundPetForm,
    onSpeciesChanged: (Species) -> Unit,
    onBreedChanged: (String) -> Unit,
    onColorChanged: (String) -> Unit,
    onSizeChanged: (PetSize) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onAddPhotosClick: () -> Unit,
    onPhotoRemove: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        StepHeader(
            title = stringResource(R.string.create_step_found_pet_title),
            subtitle = stringResource(R.string.create_step_found_pet_subtitle)
        )

        PhotoPickerSection(
            photos = form.photos,
            onAddClick = onAddPhotosClick,
            onRemove = onPhotoRemove
        )

        FormSection(title = stringResource(R.string.create_found_section_look)) {
            FormFieldLabel(stringResource(R.string.pet_species_label))
            ChoiceChipGroup(
                options = Species.entries,
                selected = form.species,
                onSelect = { it?.let(onSpeciesChanged) },
                label = { stringResource(it.label) }
            )
            Spacer(Modifier.height(FormSpacing.FieldGap))

            FormFieldLabel(stringResource(R.string.pet_size_label))
            ChoiceChipGroup(
                options = PetSize.entries,
                selected = form.size,
                onSelect = { it?.let(onSizeChanged) },
                label = { stringResource(it.label) }
            )
            Spacer(Modifier.height(FormSpacing.FieldGap))

            EponaTextField(
                value = form.breed,
                onValueChange = onBreedChanged,
                label = stringResource(R.string.pet_breed_label),
                placeholder = stringResource(R.string.pet_breed_placeholder),
                optional = true
            )
            Spacer(Modifier.height(FormSpacing.FieldGap))

            EponaTextField(
                value = form.color,
                onValueChange = onColorChanged,
                label = stringResource(R.string.pet_color_label),
                placeholder = stringResource(R.string.pet_color_placeholder),
                optional = true
            )
            Spacer(Modifier.height(FormSpacing.FieldGap))

            EponaTextField(
                value = form.description,
                onValueChange = onDescriptionChanged,
                label = stringResource(R.string.pet_features_label),
                placeholder = stringResource(R.string.create_found_description_placeholder),
                singleLine = false,
                minLines = 3,
                maxLines = 6,
                optional = true,
                imeAction = ImeAction.Default
            )
        }
    }
}

@EponaPreviews
@Composable
private fun FoundPetStepPreview() {
    EponaTheme {
        FoundPetStep(
            form = FoundPetForm(
                species = Species.DOG,
                breed = "Labrador mix",
                color = "Golden",
                photos = listOf(PhotoItem.Remote("https://example.com/a.jpg"))
            ),
            onSpeciesChanged = {},
            onBreedChanged = {},
            onColorChanged = {},
            onSizeChanged = {},
            onDescriptionChanged = {},
            onAddPhotosClick = {},
            onPhotoRemove = {}
        )
    }
}
