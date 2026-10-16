package com.fabriziogo.epona.feature.sighting.report.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaTextField

@Composable
fun SightingNoteField(
    note: String,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.sighting_note_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.sighting_note_desc),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        EponaTextField(
            value = note,
            onValueChange = onNoteChanged,
            label = stringResource(R.string.sighting_note_label),
            placeholder = stringResource(R.string.sighting_note_placeholder),
            singleLine = false,
            maxLines = 4,
            imeAction = ImeAction.Done,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
