package com.fabriziogo.epona.feature.profile.components

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.ui.theme.EponaTheme

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
    val iconBg = if (isDestructive)
        MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.secondaryContainer

    val iconTint = if (isDestructive)
        MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSecondaryContainer

    ListItem(
        headlineContent = { Text(label) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = {
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
        },
        trailingContent = if (showChevron) {
            {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }
        } else null,
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            headlineColor = if (isDestructive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
    )

    Spacer(modifier = Modifier.size(8.dp))
}

@EponaPreviews
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
