package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * The pet's lasting identifying traits (markings, scars, chip, temperament).
 *
 * Rendered as a factual card because it describes the animal, not this alert.
 * The alert's own narrative lives in [DetailAlertStory].
 */
@Composable
fun DetailPetFeatures(
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Outlined.Pets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.pet_features_label),
                    style = EponaTypography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = description,
                style = EponaTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailPetFeaturesPreview() {
    EponaTheme {
        DetailPetFeatures(
            description = "White patch on the left ear, small scar on the nose, microchipped. Shy with strangers.",
            modifier = Modifier.padding(20.dp)
        )
    }
}
