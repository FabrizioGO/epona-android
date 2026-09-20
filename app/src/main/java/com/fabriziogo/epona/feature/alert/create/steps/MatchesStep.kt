package com.fabriziogo.epona.feature.alert.create.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.components.PetCard
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * Step 4 (shared): nearby alerts of the opposite type that might be the same
 * animal. Tapping a row opens that alert's detail screen (with the wizard
 * popped) so the user can compare photos and report a sighting there.
 *
 * A failed lookup never blocks publishing — it surfaces inline while Continue
 * stays enabled. The empty state carries reassuring copy; the primary Continue
 * button below is the "none of these" affordance.
 */
@Composable
fun MatchesStep(
    matches: List<AlertWithDetails>,
    isLoading: Boolean,
    error: String?,
    lookingFor: AlertType,
    onMatchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            stringResource(R.string.create_step_matches_title),
            style = EponaTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.create_step_matches_subtitle),
            style = EponaTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))

        when {
            isLoading -> LoadingIndicator(modifier = Modifier.fillMaxWidth())

            matches.isEmpty() -> EmptyState(
                icon = Icons.Outlined.Search,
                title = stringResource(R.string.create_matches_empty_title),
                description = error
                    ?: stringResource(R.string.create_matches_empty_desc),
                modifier = Modifier.fillMaxWidth()
            )

            else -> {
                Text(
                    text = stringResource(
                        if (lookingFor == AlertType.LOST) R.string.create_matches_hint
                        else R.string.create_matches_hint_lost
                    ),
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    matches.forEach { match ->
                        PetCard(
                            alertWithDetails = match,
                            onClick = { onMatchClick(match.alert.id) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.create_matches_none),
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

private fun previewMatch(id: String, breed: String?) = AlertWithDetails(
    alert = Alert(
        id = id,
        type = AlertType.LOST,
        lastSeenLocation = Location(40.4, -3.7, "Retiro Park, Madrid"),
        lastSeenAddress = "Retiro Park, Madrid",
        createdAt = System.currentTimeMillis()
    ),
    pet = Pet(name = "Luna", species = Species.DOG, breed = breed, color = "Golden"),
    ownerName = "María",
    distanceMeters = 850.0
)

@Preview(showBackground = true)
@Composable
private fun MatchesStepLoadingPreview() {
    EponaTheme {
        MatchesStep(
            matches = emptyList(),
            isLoading = true,
            error = null,
            lookingFor = AlertType.LOST,
            onMatchClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchesStepEmptyPreview() {
    EponaTheme {
        MatchesStep(
            matches = emptyList(),
            isLoading = false,
            error = null,
            lookingFor = AlertType.LOST,
            onMatchClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchesStepPopulatedPreview() {
    EponaTheme {
        MatchesStep(
            matches = listOf(
                previewMatch("1", "Labrador"),
                previewMatch("2", "Golden Retriever")
            ),
            isLoading = false,
            error = null,
            lookingFor = AlertType.LOST,
            onMatchClick = {}
        )
    }
}
