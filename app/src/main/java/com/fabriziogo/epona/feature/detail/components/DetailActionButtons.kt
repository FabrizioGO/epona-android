package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.EponaOutlinedButton
import com.fabriziogo.epona.core.ui.components.EponaTonalButton

@Composable
fun DetailActionButtons(
    isOwner: Boolean,
    isResolved: Boolean,
    isLost: Boolean,
    isResolving: Boolean,
    onContactClick: () -> Unit,
    onReportSightingClick: () -> Unit,
    onResolveClick: () -> Unit,
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
                    text = "Contact Owner",
                    onClick = onContactClick,
                    icon = Icons.Outlined.Phone,
                    modifier = Modifier.weight(1f),
                    fullWidth = true
                )
                EponaOutlinedButton(
                    text = "",
                    onClick = onReportSightingClick,
                    icon = Icons.Outlined.Visibility
                )
            }

            // Report sighting (for lost pets)
            if (isLost) {
                EponaTonalButton(
                    text = "Report a Sighting",
                    onClick = onReportSightingClick,
                    icon = Icons.Outlined.CameraAlt,
                    fullWidth = true
                )
            }

            // Resolve (owner only)
            if (isOwner) {
                Spacer(Modifier.height(4.dp))
                EponaOutlinedButton(
                    text = "Mark as Reunited",
                    onClick = onResolveClick,
                    icon = Icons.Filled.CheckCircle,
                    fullWidth = true
                )
            }
        } else {
            // Resolved state
            EponaTonalButton(
                text = "Pet has been reunited! 🎉",
                onClick = {},
                fullWidth = true
            )
        }
    }
}