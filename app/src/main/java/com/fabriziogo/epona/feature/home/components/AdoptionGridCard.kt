package com.fabriziogo.epona.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.model.AgeStage
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.AdoptionBadge
import com.fabriziogo.epona.core.ui.components.AdoptionPriceBadge
import com.fabriziogo.epona.core.ui.components.emoji
import com.fabriziogo.epona.core.ui.components.formatTimeAgo
import com.fabriziogo.epona.core.ui.extensions.pressScale
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.extendedColors

/** Photo overlays sit on a dark scrim, so their content stays white in every theme. */
private val OnScrim = Color.White

/**
 * Feed card for an adoption post. Same shape and scrim as [AlertGridCard]; shows the title,
 * species and age stage, the price (or "Free") and how long ago it was posted instead of a
 * distance.
 */
@Composable
fun AdoptionGridCard(
    item: AdoptionPostWithOwner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val post = item.post

    Box(
        modifier = modifier
            .aspectRatio(0.72f)
            .clip(MaterialTheme.shapes.medium)
            .pressScale(onClick = onClick)
    ) {
        if (post.photoUrls.isNotEmpty()) {
            AsyncImage(
                model = post.photoUrls.first(),
                // The title is shown on the card, so the photo is not read out separately.
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.extendedColors.adoptionContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(text = post.species.emoji(), style = MaterialTheme.typography.displaySmall)
            }
        }

        // Scrim so the bottom text stays legible over any photo.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.45f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.75f)
                    )
                )
        )

        AdoptionBadge(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )

        AdoptionPriceBadge(
            price = post.price,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                color = OnScrim,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.size(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = speciesAndAge(post),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnScrim.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = formatTimeAgo(post.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnScrim.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun speciesAndAge(post: AdoptionPost): String {
    val species = stringResource(post.species.label)
    val stage = post.ageStage?.let { stringResource(it.label(post.species)) }
    return if (stage != null) "$species · $stage" else species
}

@EponaPreviews
@Composable
private fun AdoptionGridCardPreview() {
    EponaTheme(dynamicColor = false) {
        AdoptionGridCard(
            item = AdoptionPostWithOwner(
                post = AdoptionPost(
                    id = "1",
                    title = "Golden Retriever puppies in adoption",
                    species = Species.DOG,
                    ageStage = AgeStage.BABY,
                    price = null,
                    contactPhone = "+1 555 000 0000",
                    createdAt = System.currentTimeMillis() - 7_200_000
                ),
                ownerName = "Alex"
            ),
            onClick = {},
            modifier = Modifier.size(width = 180.dp, height = 250.dp)
        )
    }
}
