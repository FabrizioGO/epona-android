package com.fabriziogo.epona.feature.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isDestructive: Boolean = false,
    showChevron: Boolean = true,
    onClick: () -> Unit
) {
    val contentColor = if (isDestructive)
        MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface

    val iconBg = if (isDestructive)
        MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)

    val iconTint = if (isDestructive)
        MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = iconBg,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = EponaTypography.bodyLarge,
                    color = contentColor
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = EponaTypography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showChevron) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    Spacer(modifier = Modifier.size(8.dp))
}

@Preview(showBackground = true)
@Composable
private fun SettingsItemPreview() {
    EponaTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsItem(
                icon = Icons.Outlined.Settings,
                label = "Settings",
                subtitle = "Radius and notifications",
                onClick = {}
            )
            SettingsItem(
                icon = Icons.Outlined.DeleteForever,
                label = "Delete account",
                isDestructive = true,
                showChevron = false,
                onClick = {}
            )
        }
    }
}
