package com.fabriziogo.epona.feature.detail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun DetailDescription(
    alertDescription: String?,
    petDescription: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Description",
            style = EponaTypography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(8.dp))

        alertDescription?.let {
            Text(
                text = it,
                style = EponaTypography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = EponaTypography.bodyLarge.lineHeight
            )
        }

        if (alertDescription != null && petDescription != null) {
            Spacer(Modifier.height(8.dp))
        }

        petDescription?.let {
            Text(
                text = it,
                style = EponaTypography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
