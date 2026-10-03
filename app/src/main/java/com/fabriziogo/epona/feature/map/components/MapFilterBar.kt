package com.fabriziogo.epona.feature.map.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.ui.extensions.pressScale
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * Floating Lost / Found toggle for the map. Selected pills use the same red / green as the
 * markers and [com.fabriziogo.epona.core.ui.components.AlertTypeBadge], so the bar doubles
 * as a legend; unselected pills keep the type color on the icon only.
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
        shape = RoundedCornerShape(50),
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MapFilterPill(
                type = AlertType.LOST,
                label = stringResource(R.string.alert_type_lost),
                icon = Icons.Outlined.Search,
                count = lostCount,
                selected = showLost,
                onClick = { onFilterToggled(AlertType.LOST) }
            )
            MapFilterPill(
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
private fun MapFilterPill(
    type: AlertType,
    label: String,
    icon: ImageVector,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val typeColor = when (type) {
        AlertType.LOST -> EponaColors.Lost
        AlertType.FOUND -> EponaColors.Found
    }
    val animation = tween<Color>(durationMillis = 150)
    val containerColor by animateColorAsState(
        targetValue = if (selected) typeColor else Color.Transparent,
        animationSpec = animation,
        label = "pillContainer"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else typeColor,
        animationSpec = animation,
        label = "pillIcon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = animation,
        label = "pillLabel"
    )

    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .semantics {
                this.selected = selected
                role = Role.Checkbox
            }
            .pressScale(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = EponaTypography.labelLarge,
            color = labelColor
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = count.toString(),
            style = EponaTypography.labelMedium,
            color = labelColor.copy(alpha = 0.7f)
        )
    }
}

@Preview(showBackground = true)
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

@Preview(showBackground = true)
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
