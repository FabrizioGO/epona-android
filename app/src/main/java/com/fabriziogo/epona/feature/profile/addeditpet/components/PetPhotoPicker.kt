package com.fabriziogo.epona.feature.profile.addeditpet.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun PetPhotoPicker(
    photoUrl: String?,
    onAddPhoto: (String) -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(110.dp)) {
        if (photoUrl != null) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(MaterialTheme.shapes.large)
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Pet photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(110.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable { onRemovePhoto() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remove photo",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .clickable {
                        // In a real app: launch a photo picker / camera
                        onAddPhoto("placeholder_pet_photo_uri")
                    },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(94.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = "Add photo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "Add Photo",
                        style = EponaTypography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
