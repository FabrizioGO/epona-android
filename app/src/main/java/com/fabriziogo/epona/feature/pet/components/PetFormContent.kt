package com.fabriziogo.epona.feature.pet.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.layout.constrainedWidth
import com.fabriziogo.epona.core.media.PhotoItem

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
    val nameError: String? = null
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
 * The scrolling pet form shared by the add and edit screens. Stateless: it renders [state]
 * and reports edits through [actions], so both screens stay in step.
 */
@Composable
fun PetFormContent(
    state: PetFormState,
    actions: PetFormActions,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues)
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .constrainedWidth()
                .padding(16.dp)
        ) {
            // ---- Basic Info Section ----
            SectionTitle(stringResource(R.string.pet_section_info))

            EponaTextField(
                value = state.petName,
                onValueChange = actions.onNameChanged,
                label = stringResource(R.string.pet_name_label),
                placeholder = stringResource(R.string.pet_name_placeholder),
                errorText = state.nameError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            FieldLabel(stringResource(R.string.pet_species_label))
            SelectionChips(
                options = Species.entries,
                selected = state.selectedSpecies,
                label = { stringResource(it.label) },
                onSelected = actions.onSpeciesChanged,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            EponaTextField(
                value = state.breed,
                onValueChange = actions.onBreedChanged,
                label = stringResource(R.string.pet_breed_label),
                placeholder = stringResource(R.string.pet_breed_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            EponaTextField(
                value = state.color,
                onValueChange = actions.onColorChanged,
                label = stringResource(R.string.pet_color_label),
                placeholder = stringResource(R.string.pet_color_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Physical Traits Section ----
            SectionTitle(stringResource(R.string.pet_section_physical_traits))

            FieldLabel(stringResource(R.string.pet_size_label))
            SelectionChips(
                options = PetSize.entries,
                selected = state.selectedSize,
                label = { stringResource(it.label) },
                onSelected = actions.onSizeChanged,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            EponaTextField(
                value = state.ageYears,
                onValueChange = actions.onAgeChanged,
                label = stringResource(R.string.pet_age_label),
                placeholder = stringResource(R.string.pet_age_placeholder),
                keyboardType = KeyboardType.Number,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            FieldLabel(stringResource(R.string.pet_gender_label))
            SelectionChips(
                options = PetGender.entries,
                selected = state.selectedGender,
                label = { stringResource(it.label) },
                onSelected = actions.onGenderChanged,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ---- Additional Info Section ----
            SectionTitle(stringResource(R.string.pet_section_additional_info))

            EponaTextField(
                value = state.microchipId,
                onValueChange = actions.onMicrochipChanged,
                label = stringResource(R.string.pet_microchip_label),
                placeholder = stringResource(R.string.pet_optional_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            EponaTextField(
                value = state.description,
                onValueChange = actions.onDescriptionChanged,
                label = stringResource(R.string.pet_features_label),
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
                onAddClick = actions.onAddPhotoClick,
                onRemove = actions.onPhotoRemoved,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(bottom = 12.dp)
            .semantics { heading() }
    )
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
