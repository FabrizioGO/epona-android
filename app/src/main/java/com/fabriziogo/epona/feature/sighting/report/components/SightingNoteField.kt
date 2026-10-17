package com.fabriziogo.epona.feature.sighting.report.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.form.FormSection
import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import com.fabriziogo.epona.core.ui.theme.EponaTheme

@Composable
fun SightingNoteField(
    note: String,
    onNoteChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FormSection(
        title = stringResource(R.string.sighting_note_title),
        supporting = stringResource(R.string.sighting_note_desc),
        modifier = modifier
    ) {
        EponaTextField(
            value = note,
            onValueChange = onNoteChanged,
            label = stringResource(R.string.sighting_note_label),
            placeholder = stringResource(R.string.sighting_note_placeholder),
            singleLine = false,
            minLines = 3,
            maxLines = 6,
            optional = true,
            imeAction = ImeAction.Default,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@EponaPreviews
@Composable
private fun SightingNoteFieldPreview() {
    EponaTheme { SightingNoteField(note = "", onNoteChanged = {}) }
}
