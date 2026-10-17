package com.fabriziogo.epona.core.ui.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.ui.form.FormSection
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * The photos section shared by the pet, found-pet and adoption forms. Stateless: the screen
 * owns the picker and decides what an "add" tap does.
 *
 * Empty, it is one large tap target; once a photo is attached it becomes the thumbnail row.
 */
@Composable
fun PhotoPickerSection(
    photos: List<PhotoItem>,
    onAddClick: () -> Unit,
    onRemove: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    FormSection(
        title = stringResource(R.string.pet_photos_title),
        supporting = stringResource(R.string.pet_photos_desc),
        modifier = modifier
    ) {
        if (photos.isEmpty()) {
            PhotoHero(onClick = onAddClick)
        } else {
            PhotoThumbnailRow(
                photos = photos,
                onAddClick = onAddClick,
                onRemove = onRemove
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.pet_photos_selected_count,
                    photos.size,
                    MAX_PHOTOS_PER_ENTITY
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PhotoHero(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(role = Role.Button, onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.AddAPhoto,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.photo_hero_add),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@EponaPreviews
@Composable
private fun PhotoPickerSectionPreview() {
    EponaTheme {
        PhotoPickerSection(
            photos = listOf(PhotoItem.Remote("https://example.com/a.jpg")),
            onAddClick = {},
            onRemove = {}
        )
    }
}

@EponaPreviews
@Composable
private fun PhotoPickerSectionEmptyPreview() {
    EponaTheme {
        PhotoPickerSection(photos = emptyList(), onAddClick = {}, onRemove = {})
    }
}
