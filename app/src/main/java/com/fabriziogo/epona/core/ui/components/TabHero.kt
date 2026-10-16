package com.fabriziogo.epona.core.ui.components

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.ui.theme.EponaTheme

/**
 * Header shared by the top-level tabs: title (+ optional subtitle) with action buttons,
 * then free-form [content]. Painted with `primaryContainer`; text uses `onPrimaryContainer`
 * so it stays legible in light, dark and Material You themes.
 */
@Composable
fun TabHero(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    contentInset: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .statusBarsPadding()
            .padding(horizontal = 20.dp + contentInset)
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = contentColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
        }
        content()
    }
}

/** Circular action button for use on a [TabHero]-style hero; meets the 48dp touch target. */
@Composable
fun HeroIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge {
                        Text(badgeCount.toString())
                    }
                }
            }
        ) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}

/** Rounded-top surface sheet that overlaps the hero; teal peeks behind the corners. */
@Composable
fun HeroSheetEdge(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        bottomStart = ZeroCornerSize,
                        bottomEnd = ZeroCornerSize
                    )
                )
                .padding(top = 20.dp)
        ) {
            content()
        }
    }
}

@EponaPreviews
@Composable
private fun TabHeroPreview() {
    EponaTheme(dynamicColor = false) {
        TabHero(
            title = "Notifications",
            subtitle = "3 unread",
            actions = {
                HeroIconButton(
                    icon = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    onClick = {},
                    badgeCount = 2
                )
            }
        )
    }
}

@EponaPreviews
@Composable
private fun HeroSheetEdgePreview() {
    EponaTheme(dynamicColor = false) {
        HeroSheetEdge {
            Text(
                "Section",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}
