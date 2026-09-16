package com.fabriziogo.epona.feature.alert.create.steps


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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun AlertTypeStep(
    selectedType: AlertType,
    onTypeSelected: (AlertType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(top = 24.dp)) {
        Text(
            text = stringResource(R.string.create_step_type_title),
            style = EponaTypography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.create_step_type_subtitle),
            style = EponaTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AlertType.entries.forEach { type ->
                val selected = selectedType == type
                Surface(
                    onClick = { onTypeSelected(type) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = MaterialTheme.shapes.medium,
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow,
                    border = if (selected) androidx.compose.foundation.BorderStroke(
                        2.dp, MaterialTheme.colorScheme.primary
                    ) else null
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (type == AlertType.LOST) "😿" else "🐾",
                            style = EponaTypography.displaySmall
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (type == AlertType.LOST) stringResource(R.string.create_type_lost)
                            else stringResource(R.string.create_type_found),
                            style = EponaTypography.titleMedium.copy(textAlign = TextAlign.Center),
                            color = if (selected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (type == AlertType.LOST) stringResource(R.string.create_type_lost_desc)
                            else stringResource(R.string.create_type_found_desc),
                            style = EponaTypography.bodySmall.copy(textAlign = TextAlign.Center),
                            color = if (selected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
