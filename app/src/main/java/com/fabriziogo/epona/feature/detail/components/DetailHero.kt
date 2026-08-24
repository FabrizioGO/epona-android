package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun DetailHero(
    pet: Pet,
    alertType: AlertType,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (alertType == AlertType.LOST)
        EponaColors.LostContainer else EponaColors.FoundContainer

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (pet.photoUrls.isNotEmpty()) {
            AsyncImage(
                model = pet.photoUrls.first(),
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(260.dp)
            )
        } else {
            Text(
                text = when (pet.species.value) {
                    "dog" -> "🐕"
                    "cat" -> "🐈"
                    "bird" -> "🐦"
                    "rabbit" -> "🐇"
                    else -> "🐾"
                },
                style = EponaTypography.displayLarge.copy(
                    fontSize = EponaTypography.displayLarge.fontSize * 2
                )
            )
        }

        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
            )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }

        // Share button
        IconButton(
            onClick = onShareClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 12.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
            )
        ) {
            Icon(
                Icons.Outlined.Share,
                contentDescription = "Share"
            )
        }
    }
}
