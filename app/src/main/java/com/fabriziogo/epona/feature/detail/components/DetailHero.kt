package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.ui.components.emoji
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.core.ui.theme.StatusBarIcons

/** Artwork height below the status bar; the hero itself grows to cover the bar too. */
private val HeroContentHeight = 260.dp

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

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // White glyphs read against the scrim below regardless of the photo or the theme.
    StatusBarIcons(darkIcons = false)

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Grows by the status-bar height so the artwork keeps its full height below
            // the bar instead of losing its top slice to it.
            .height(HeroContentHeight + statusBarHeight)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (pet.photoUrls.isNotEmpty()) {
            AsyncImage(
                model = pet.photoUrls.first(),
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = pet.species.emoji(),
                style = EponaTypography.displayLarge.copy(
                    fontSize = EponaTypography.displayLarge.fontSize * 2
                )
            )
        }

        // Keeps the status-bar glyphs legible over whatever photo happens to be here.
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(statusBarHeight + 72.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )

        // Back button
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 12.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
            )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.action_back)
            )
        }

        // Share button
        IconButton(
            onClick = onShareClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 12.dp, top = 12.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
            )
        ) {
            Icon(
                Icons.Outlined.Share,
                contentDescription = stringResource(R.string.action_share)
            )
        }
    }
}
