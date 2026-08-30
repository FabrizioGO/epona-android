package com.fabriziogo.epona.feature.pet.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.ui.media.PhotoThumbnailRow
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * The photos section of the pet form. Stateless: the screen owns the picker and
 * decides what an "add" tap does.
 */
@Composable
fun PhotoPickerSection(
    photos: List<PhotoItem>,
    onAddClick: () -> Unit,
    onRemove: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.pet_photos_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.pet_photos_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

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

@Preview(showBackground = true)
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

@Preview(showBackground = true, name = "Empty")
@Composable
private fun PhotoPickerSectionEmptyPreview() {
    EponaTheme {
        PhotoPickerSection(photos = emptyList(), onAddClick = {}, onRemove = {})
    }
}
