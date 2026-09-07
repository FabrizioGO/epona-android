package com.fabriziogo.epona.core.ui.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.media.LocalImage
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * The thumbnails picked so far, followed by the tile that adds another. Shared by
 * the pet form and the sighting report so the two cannot drift apart on the cap or
 * on how a photo is removed.
 *
 * The add tile disappears at [maxPhotos] and comes back as soon as one is removed,
 * which is the only affordance telling the user the limit was reached.
 */
@Composable
fun PhotoThumbnailRow(
    photos: List<PhotoItem>,
    onAddClick: () -> Unit,
    onRemove: (PhotoItem) -> Unit,
    modifier: Modifier = Modifier,
    maxPhotos: Int = MAX_PHOTOS_PER_ENTITY,
    thumbnailSize: Dp = 100.dp
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Keyed on PhotoItem.key rather than the URL: picking the same image twice
        // yields two entries, and duplicate keys crash a lazy list.
        items(photos, key = { it.key }) { photo ->
            PhotoThumbnail(
                photo = photo,
                size = thumbnailSize,
                onRemove = { onRemove(photo) }
            )
        }

        if (photos.size < maxPhotos) {
            item(key = ADD_TILE_KEY) {
                AddPhotoTile(size = thumbnailSize, onClick = onAddClick)
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    photo: PhotoItem,
    size: Dp,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.medium)
    ) {
        AsyncImage(
            model = photo.model,
            contentDescription = stringResource(R.string.cd_pet_photo),
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(size)
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_remove_photo),
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
private fun AddPhotoTile(
    size: Dp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(size)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Outlined.AddAPhoto,
                contentDescription = stringResource(R.string.cd_add_photo),
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private const val ADD_TILE_KEY = "add_photo_tile"

@Preview(showBackground = true)
@Composable
private fun PhotoThumbnailRowPreview() {
    EponaTheme {
        Row(Modifier.padding(16.dp)) {
            PhotoThumbnailRow(
                photos = listOf(
                    PhotoItem.Remote("https://example.com/a.jpg"),
                    PhotoItem.Local(LocalImage("1", "file:///b.jpg", "b.jpg"))
                ),
                onAddClick = {},
                onRemove = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Full")
@Composable
private fun PhotoThumbnailRowFullPreview() {
    EponaTheme {
        Row(Modifier.padding(16.dp)) {
            PhotoThumbnailRow(
                photos = List(MAX_PHOTOS_PER_ENTITY) {
                    PhotoItem.Local(LocalImage("$it", "file:///$it.jpg", "$it.jpg"))
                },
                onAddClick = {},
                onRemove = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun PhotoThumbnailRowEmptyPreview() {
    EponaTheme {
        Row(Modifier.padding(16.dp)) {
            PhotoThumbnailRow(photos = emptyList(), onAddClick = {}, onRemove = {})
        }
    }
}
