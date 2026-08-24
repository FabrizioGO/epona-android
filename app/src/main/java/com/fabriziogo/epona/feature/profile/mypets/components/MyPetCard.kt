package com.fabriziogo.epona.feature.profile.mypets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.ui.components.EponaCard
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun MyPetCard(
    pet: Pet,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EponaCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (pet.photoUrls.isNotEmpty()) {
                    AsyncImage(
                        model = pet.photoUrls.first(),
                        contentDescription = pet.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(60.dp)
                    )
                } else {
                    Text(
                        text = when (pet.species.value) {
                            "dog" -> "🐕"; "cat" -> "🐈"
                            "bird" -> "🐦"; "rabbit" -> "🐇"
                            else -> "🐾"
                        },
                        style = EponaTypography.headlineSmall
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = EponaTypography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = listOfNotNull(
                        pet.breed,
                        pet.species.label,
                        pet.color
                    ).joinToString(" · "),
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                pet.microchipId?.let {
                    Text(
                        text = "Chip: $it",
                        style = EponaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Actions
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
