package com.fabriziogo.epona.feature.alert.create.steps.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import com.fabriziogo.epona.core.ui.form.SelectableOptionCard
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.FoundCustody
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * "Where is the pet now?" — the FOUND-only question that decides whether the
 * post accepts sightings.
 *
 * Stacked rather than side by side like AlertTypeStep: each option's
 * consequence is a sentence the finder needs to read, not a caption.
 */
@Composable
fun CustodySelector(
    selected: FoundCustody?,
    onSelected: (FoundCustody) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.create_custody_question),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FoundCustody.entries.forEach { custody ->
                val isSelected = selected == custody
                SelectableOptionCard(
                    selected = isSelected,
                    onClick = { onSelected(custody) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = stringResource(custody.label),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(custody.description),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Unanswered")
@Composable
private fun CustodySelectorUnansweredPreview() {
    EponaTheme { CustodySelector(selected = null, onSelected = {}) }
}

@Preview(showBackground = true, name = "With finder")
@Composable
private fun CustodySelectorWithFinderPreview() {
    EponaTheme {
        CustodySelector(selected = FoundCustody.WITH_FINDER, onSelected = {})
    }
}

@Preview(showBackground = true, name = "At location")
@Composable
private fun CustodySelectorAtLocationPreview() {
    EponaTheme {
        CustodySelector(selected = FoundCustody.AT_LOCATION, onSelected = {})
    }
}
