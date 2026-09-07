package com.fabriziogo.epona.core.ui.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * Asks where the next photo should come from. The camera row is left out when no
 * app on the device answers ACTION_IMAGE_CAPTURE, so the sheet never offers a
 * route that would dead-end.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCameraAvailable: Boolean = true
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        modifier = modifier
    ) {
        PhotoSourceSheetContent(
            onGalleryClick = onGalleryClick,
            onCameraClick = onCameraClick,
            isCameraAvailable = isCameraAvailable
        )
    }
}

@Composable
private fun PhotoSourceSheetContent(
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    isCameraAvailable: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.photo_source_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
        )

        PhotoSourceRow(
            icon = Icons.Outlined.PhotoLibrary,
            label = stringResource(R.string.photo_source_gallery),
            onClick = onGalleryClick
        )

        if (isCameraAvailable) {
            PhotoSourceRow(
                icon = Icons.Outlined.CameraAlt,
                label = stringResource(R.string.photo_source_camera),
                onClick = onCameraClick
            )
        }
    }
}

@Composable
private fun PhotoSourceRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.size(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoSourceSheetContentPreview() {
    EponaTheme {
        Column {
            Spacer(Modifier.height(16.dp))
            PhotoSourceSheetContent(
                onGalleryClick = {},
                onCameraClick = {},
                isCameraAvailable = true
            )
        }
    }
}

@Preview(showBackground = true, name = "No camera app")
@Composable
private fun PhotoSourceSheetContentNoCameraPreview() {
    EponaTheme {
        Column {
            Spacer(Modifier.height(16.dp))
            PhotoSourceSheetContent(
                onGalleryClick = {},
                onCameraClick = {},
                isCameraAvailable = false
            )
        }
    }
}
