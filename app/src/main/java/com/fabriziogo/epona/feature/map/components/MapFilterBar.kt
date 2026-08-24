package com.fabriziogo.epona.feature.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.ui.components.EponaFilterChip

@Composable
fun MapFilterBar(
    showLost: Boolean,
    showFound: Boolean,
    onFilterToggled: (AlertType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EponaFilterChip(
                label = "Lost",
                selected = showLost,
                onClick = { onFilterToggled(AlertType.LOST) }
            )
            EponaFilterChip(
                label = "Found",
                selected = showFound,
                onClick = { onFilterToggled(AlertType.FOUND) }
            )
        }
    }
}