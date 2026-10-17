package com.fabriziogo.epona.feature.profile.myalerts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.AdoptionPostWithOwner
import com.fabriziogo.epona.core.domain.model.AdoptionStatus
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.AdoptedStatusBadge
import com.fabriziogo.epona.core.ui.components.AdoptionBadge
import com.fabriziogo.epona.core.ui.components.AdoptionPriceBadge
import com.fabriziogo.epona.core.ui.components.EponaCard
import com.fabriziogo.epona.core.ui.components.emoji
import com.fabriziogo.epona.core.ui.components.formatTimeAgo
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.extendedColors

/** Row for one of the user's own adoption posts; the counterpart of [MyAlertCard]. */
@Composable
fun MyAdoptionCard(
    item: AdoptionPostWithOwner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val post = item.post

    EponaCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.extendedColors.adoptionContainer),
                contentAlignment = Alignment.Center
            ) {
                if (post.photoUrls.isNotEmpty()) {
                    AsyncImage(
                        model = post.photoUrls.first(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    Text(
                        text = post.species.emoji(),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.clearAndSetSemantics {}
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AdoptionBadge()
                    Spacer(Modifier.width(8.dp))
                    if (post.status == AdoptionStatus.ADOPTED) {
                        AdoptedStatusBadge()
                    } else {
                        AdoptionPriceBadge(price = post.price)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = formatTimeAgo(post.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyAdoptionCardPreview() {
    EponaTheme {
        MyAdoptionCard(
            item = AdoptionPostWithOwner(
                post = AdoptionPost(
                    id = "1",
                    title = "Golden Retriever puppies in adoption",
                    species = Species.DOG,
                    price = 50.0,
                    contactPhone = "0000-000-0000",
                    createdAt = System.currentTimeMillis() - 3_600_000
                ),
                ownerName = "Alex"
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
