package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.ReportReason
import com.fabriziogo.epona.core.ui.components.EponaTextField
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * The in-app UGC reporting flow Google Play requires -- see DetailHero's overflow
 * menu (alerts) and SightingCard's flag action (sightings) for the two entry
 * points, and ReportContentUseCase for the 500-character cap this enforces on
 * [details] too.
 */
@Composable
fun FlagContentDialog(
    isSubmitting: Boolean,
    onSubmit: (reason: ReportReason, details: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by rememberSaveable { mutableStateOf<ReportReason?>(null) }
    var details by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.report_dialog_title), style = EponaTypography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                ReportReason.entries.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedReason == reason, onClick = null)
                        Text(stringResource(reason.label), style = EponaTypography.bodyMedium)
                    }
                }

                Spacer(Modifier.height(8.dp))

                EponaTextField(
                    value = details,
                    onValueChange = { if (it.length <= MAX_DETAILS_LENGTH) details = it },
                    label = stringResource(R.string.report_dialog_details_label),
                    supportingText = "${details.length}/$MAX_DETAILS_LENGTH",
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedReason?.let { onSubmit(it, details) } },
                enabled = selectedReason != null && !isSubmitting
            ) {
                Text(stringResource(R.string.report_dialog_submit))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        shape = MaterialTheme.shapes.large
    )
}

// Matches content_reports_details_length in
// supabase/migrations/20260926000000_content_reports.sql.
private const val MAX_DETAILS_LENGTH = 500
