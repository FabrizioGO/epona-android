package com.fabriziogo.epona.feature.alert.create.steps

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
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.feature.alert.create.FoundPetForm
import com.fabriziogo.epona.feature.pet.components.PhotoPickerSection
import com.fabriziogo.epona.feature.pet.components.SelectionChips

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
        Text(
            stringResource(R.string.create_step_found_pet_title),
            style = EponaTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.create_step_found_pet_subtitle),
            style = EponaTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        Text(
            stringResource(R.string.pet_species_label),
            style = EponaTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        SelectionChips(
            options = Species.entries,
            selected = form.species,
            label = { stringResource(it.label) },
            onSelected = onSpeciesChanged
        )

        Spacer(Modifier.height(16.dp))

        EponaTextField(
            value = form.breed,
            onValueChange = onBreedChanged,
            label = stringResource(R.string.pet_breed_label),
            placeholder = stringResource(R.string.pet_breed_placeholder)
        )

        Spacer(Modifier.height(12.dp))

        EponaTextField(
            value = form.color,
            onValueChange = onColorChanged,
            label = stringResource(R.string.pet_color_label),
            placeholder = stringResource(R.string.pet_color_placeholder)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            stringResource(R.string.pet_size_label),
            style = EponaTypography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        SelectionChips(
            options = PetSize.entries,
            selected = form.size,
            label = { stringResource(it.label) },
            onSelected = onSizeChanged
        )

        Spacer(Modifier.height(24.dp))

        PhotoPickerSection(
            photos = form.photos,
            onAddClick = onAddPhotosClick,
            onRemove = onPhotoRemove
        )

        Spacer(Modifier.height(24.dp))

        EponaTextField(
            value = form.description,
            onValueChange = onDescriptionChanged,
            label = stringResource(R.string.create_description),
            placeholder = stringResource(R.string.create_found_description_placeholder),
            singleLine = false,
            maxLines = 5,
            imeAction = ImeAction.Done
        )
    }
}

@Preview(showBackground = true)
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
