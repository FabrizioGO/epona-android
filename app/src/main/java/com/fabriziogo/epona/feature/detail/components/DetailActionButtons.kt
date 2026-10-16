package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTonalButton

@Composable
fun DetailActionButtons(
    isOwner: Boolean,
    isResolved: Boolean,
    /**
     * False for a found pet the finder took with them — see Alert.acceptsSightings.
     * Every sighting entry point below is gated on it, including the eye icon, which
     * used to be offered on found alerts regardless.
     */
    acceptsSightings: Boolean,
    isResolving: Boolean,
    onContactClick: () -> Unit,
    onReportSightingClick: () -> Unit,
    onResolveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!isResolved) {
            // Contact owner + sighting eye icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                EponaFilledButton(
                    text = stringResource(R.string.detail_contact_owner),
                    onClick = onContactClick,
                    icon = Icons.Outlined.Phone,
                    modifier = Modifier.weight(1f),
                    fullWidth = true
                )
                // With no sighting to report, contact is the only thing left to do
                // here, so it takes the whole row rather than leaving a dead stub.
                if (isOwner) {
                    FilledTonalIconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete)
                        )
                    }
                }
            }

            if (acceptsSightings) {
                EponaTonalButton(
                    text = stringResource(R.string.detail_report_sighting),
                    onClick = onReportSightingClick,
                    icon = Icons.Outlined.CameraAlt,
                    fullWidth = true
                )
            }

            // Resolve (owner only)
            if (isOwner) {
                Spacer(Modifier.height(4.dp))
                EponaOutlinedButton(
                    text = stringResource(R.string.detail_mark_reunited),
                    onClick = onResolveClick,
                    icon = Icons.Filled.CheckCircle,
                    fullWidth = true
                )
            }
        } else {
            // Resolved state: a status, not an action, so it must not look tappable.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = stringResource(R.string.detail_reunited),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
    }
}