package com.fabriziogo.epona.feature.map.components

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.extendedColors

/**
 * Floating Lost / Found toggle for the map. Selected chips use the same red / green as the
 * markers and [com.fabriziogo.epona.core.ui.components.AlertTypeBadge], so the bar doubles
 * as a legend; unselected chips keep the type color on the icon only.
 */
@Composable
fun MapFilterBar(
    showLost: Boolean,
    showFound: Boolean,
    lostCount: Int,
    foundCount: Int,
    onFilterToggled: (AlertType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        shadowElevation = 3.dp,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MapFilterChip(
                type = AlertType.LOST,
                label = stringResource(R.string.alert_type_lost),
                icon = Icons.Outlined.Search,
                count = lostCount,
                selected = showLost,
                onClick = { onFilterToggled(AlertType.LOST) }
            )
            MapFilterChip(
                type = AlertType.FOUND,
                label = stringResource(R.string.alert_type_found),
                icon = Icons.Outlined.CheckCircle,
                count = foundCount,
                selected = showFound,
                onClick = { onFilterToggled(AlertType.FOUND) }
            )
        }
    }
}

@Composable
private fun MapFilterChip(
    type: AlertType,
    label: String,
    icon: ImageVector,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val extended = MaterialTheme.extendedColors
    val typeColor = when (type) {
        AlertType.LOST -> extended.lost
        AlertType.FOUND -> extended.found
    }
    val onTypeColor = when (type) {
        AlertType.LOST -> extended.onLost
        AlertType.FOUND -> extended.onFound
    }

    // FilterChip supplies the state layer, the checked semantics and the 48dp touch target.
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text("$label $count") },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = typeColor,
            selectedContainerColor = typeColor,
            selectedLabelColor = onTypeColor,
            selectedLeadingIconColor = onTypeColor
        ),
        border = null
    )
}

@EponaPreviews
@Composable
private fun MapFilterBarBothPreview() {
    EponaTheme(dynamicColor = false) {
        MapFilterBar(
            showLost = true,
            showFound = true,
            lostCount = 8,
            foundCount = 4,
            onFilterToggled = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@EponaPreviews
@Composable
private fun MapFilterBarFoundOffPreview() {
    EponaTheme(dynamicColor = false) {
        MapFilterBar(
            showLost = true,
            showFound = false,
            lostCount = 8,
            foundCount = 4,
            onFilterToggled = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
