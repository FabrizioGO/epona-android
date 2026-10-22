package com.fabriziogo.epona.feature.detail.components

import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.fabriziogo.epona.core.ui.components.SightingCard

@Composable
fun DetailSightingsSection(
    sightings: List<SightingWithReporter>,
    isLoading: Boolean,
    currentUserId: String? = null,
    onBlockReporter: ((reporterId: String, reporterName: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.detail_sightings, sightings.size),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(Modifier.height(10.dp))

        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            sightings.isEmpty() -> {
                Text(
                    text = stringResource(R.string.detail_no_sightings),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            else -> {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sightings.forEach { sighting ->
                        SightingCard(
                            sighting = sighting,
                            onBlockClick = onBlockReporter
                                ?.takeIf { sighting.sighting.reporterId != currentUserId }
                                ?.let { block ->
                                    { block(sighting.sighting.reporterId, sighting.reporterName) }
                                }
                        )
                    }
                }
            }
        }
    }
}
