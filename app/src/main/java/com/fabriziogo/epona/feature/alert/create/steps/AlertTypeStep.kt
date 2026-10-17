package com.fabriziogo.epona.feature.alert.create.steps


import com.fabriziogo.epona.core.ui.form.SelectableOptionCard
import com.fabriziogo.epona.feature.alert.create.steps.components.StepHeader
import androidx.compose.ui.semantics.heading
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType

@Composable
fun AlertTypeStep(
    selectedType: AlertType,
    onTypeSelected: (AlertType) -> Unit,
    onAdoptionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        StepHeader(
            title = stringResource(R.string.create_step_type_title),
            subtitle = stringResource(R.string.create_step_type_subtitle)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AlertType.entries.forEach { type ->
                val selected = selectedType == type
                SelectableOptionCard(
                    selected = selected,
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (type == AlertType.LOST) "😿" else "🐾",
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.clearAndSetSemantics {}
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (type == AlertType.LOST) stringResource(R.string.create_type_lost)
                            else stringResource(R.string.create_type_found),
                            style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                            color = if (selected)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (type == AlertType.LOST) stringResource(R.string.create_type_lost_desc)
                            else stringResource(R.string.create_type_found_desc),
                            style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                            color = if (selected)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        AdoptionEntryCard(onClick = onAdoptionClick)
    }
}

/**
 * Not a third [AlertType]: an adoption post has no location or pet record, so it leaves
 * this wizard for its own form instead of selecting a type here.
 */
@Composable
private fun AdoptionEntryCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🏡",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.clearAndSetSemantics {}
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.create_type_adoption),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.create_type_adoption_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
