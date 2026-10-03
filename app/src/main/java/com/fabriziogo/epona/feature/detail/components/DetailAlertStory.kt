package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * The poster's own account of this specific alert, shown as a quote with an
 * accent bar in the alert type's color and attributed to the poster.
 *
 * The pet's lasting traits are a separate card, see [DetailPetFeatures].
 */
@Composable
fun DetailAlertStory(
    description: String,
    alertType: AlertType,
    ownerName: String,
    modifier: Modifier = Modifier
) {
    val title = when (alertType) {
        AlertType.LOST -> R.string.alert_story_label_lost
        AlertType.FOUND -> R.string.alert_story_label_found
    }
    val accent = when (alertType) {
        AlertType.LOST -> EponaColors.Lost
        AlertType.FOUND -> EponaColors.Found
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(title),
            style = EponaTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Spacer(
                Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(accent)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = description,
                    style = EponaTypography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.detail_story_attribution, ownerName),
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Lost")
@Composable
private fun DetailAlertStoryLostPreview() {
    EponaTheme {
        DetailAlertStory(
            description = "Slipped out of the garden gate around 6pm. Wearing a red collar, last seen heading towards the park.",
            alertType = AlertType.LOST,
            ownerName = "Maria",
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true, name = "Found")
@Composable
private fun DetailAlertStoryFoundPreview() {
    EponaTheme {
        DetailAlertStory(
            description = "Found wandering near the bus stop, hungry but calm. Safe with me tonight.",
            alertType = AlertType.FOUND,
            ownerName = "Jorge",
            modifier = Modifier.padding(20.dp)
        )
    }
}
