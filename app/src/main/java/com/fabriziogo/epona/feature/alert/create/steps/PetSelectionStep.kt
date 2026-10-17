package com.fabriziogo.epona.feature.alert.create.steps

import com.fabriziogo.epona.feature.alert.create.steps.components.StepHeader
import com.fabriziogo.epona.core.ui.form.SelectableOptionCard
import com.fabriziogo.epona.core.ui.components.emoji
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.FullScreenProgress

@Composable
fun PetSelectionStep(
    pets: List<Pet>,
    selectedPet: Pet?,
    isLoading: Boolean,
    onPetSelected: (Pet) -> Unit,
    onAddNewPet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        StepHeader(
            title = stringResource(R.string.create_step_pet_title),
            subtitle = stringResource(R.string.create_step_pet_subtitle)
        )

        when {
            isLoading -> FullScreenProgress()

            pets.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🐾", style = MaterialTheme.typography.displayLarge)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.create_no_pets_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.create_no_pets_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(20.dp))
                    EponaOutlinedButton(
                        stringResource(R.string.create_add_pet),
                        onClick = onAddNewPet,
                        icon = Icons.Filled.Add
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pets.forEach { pet ->
                        val isSelected = selectedPet?.id == pet.id
                        SelectableOptionCard(
                            selected = isSelected,
                            onClick = { onPetSelected(pet) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (pet.photoUrls.isNotEmpty()) {
                                        AsyncImage(
                                            model = pet.photoUrls.first(),
                                            // The name beside it already labels the row.
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } else {
                                        Text(
                                            text = pet.species.emoji(),
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.clearAndSetSemantics {}
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        pet.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        listOfNotNull(
                                            pet.breed,
                                            stringResource(pet.species.label),
                                            pet.color
                                        )
                                            .joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = stringResource(R.string.cd_selected),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    EponaOutlinedButton(
                        stringResource(R.string.create_add_new_pet),
                        onClick = onAddNewPet,
                        icon = Icons.Filled.Add,
                        fullWidth = true
                    )
                }
            }
        }
    }
}
