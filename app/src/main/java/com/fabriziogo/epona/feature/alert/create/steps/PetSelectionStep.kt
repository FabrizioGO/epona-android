package com.fabriziogo.epona.feature.alert.create.steps

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.theme.EponaTypography

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
        Text(
            stringResource(R.string.create_step_pet_title),
            style = EponaTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.create_step_pet_subtitle),
            style = EponaTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        when {
            isLoading -> LoadingIndicator()

            pets.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🐾", style = EponaTypography.displayLarge)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.create_no_pets_title),
                        style = EponaTypography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.create_no_pets_desc),
                        style = EponaTypography.bodyMedium,
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    pets.forEach { pet ->
                        val isSelected = selectedPet?.id == pet.id
                        Surface(
                            onClick = { onPetSelected(pet) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainer,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(
                                2.dp, MaterialTheme.colorScheme.primary
                            ) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
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
                                            contentDescription = pet.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    } else {
                                        Text(
                                            text = when (pet.species.value) {
                                                "dog" -> "🐕"; "cat" -> "🐈"
                                                "bird" -> "🐦"; "rabbit" -> "🐇"
                                                else -> "🐾"
                                            },
                                            style = EponaTypography.titleMedium
                                        )
                                    }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        pet.name,
                                        style = EponaTypography.titleMedium,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        listOfNotNull(
                                            pet.breed,
                                            stringResource(pet.species.label),
                                            pet.color
                                        )
                                            .joinToString(" · "),
                                        style = EponaTypography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = stringResource(R.string.cd_selected),
                                        tint = MaterialTheme.colorScheme.primary,
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
