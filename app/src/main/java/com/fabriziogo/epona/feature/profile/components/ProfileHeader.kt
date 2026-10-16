package com.fabriziogo.epona.feature.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaAvatar
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * Avatar + name + email. The avatar is tappable when [onAvatarClick] is set and
 * opens the same gallery/camera picker the pet form uses; [previewUri] is the
 * just-picked cache copy shown optimistically while [isUploading] is true.
 */
@Composable
fun ProfileHeader(
    displayName: String,
    email: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    previewUri: String? = null,
    isUploading: Boolean = false,
    onAvatarClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // The avatar is the single accessible target for changing the photo; the camera
            // badge below is only a visual hint, so TalkBack does not stop on it twice.
            val clickableModifier = if (onAvatarClick != null && !isUploading) {
                Modifier.clickable(
                    onClickLabel = stringResource(R.string.profile_change_photo),
                    role = Role.Button,
                    onClick = onAvatarClick
                )
            } else {
                Modifier
            }
            EponaAvatar(
                url = previewUri ?: avatarUrl,
                name = displayName,
                size = 88.dp,
                containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .border(2.dp, MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f), CircleShape)
                    .then(clickableModifier)
            )

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(88.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (onAvatarClick != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .clearAndSetSemantics {},
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = displayName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F4F44)
@Composable
private fun ProfileHeaderPreview() {
    EponaTheme {
        ProfileHeader(
            displayName = "Fabrizio",
            email = "fabrizio@example.com",
            avatarUrl = null,
            onAvatarClick = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F4F44, name = "Uploading")
@Composable
private fun ProfileHeaderUploadingPreview() {
    EponaTheme {
        ProfileHeader(
            displayName = "Fabrizio",
            email = "fabrizio@example.com",
            avatarUrl = null,
            previewUri = "file:///preview.jpg",
            isUploading = true,
            onAvatarClick = {}
        )
    }
}
