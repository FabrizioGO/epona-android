package com.fabriziogo.epona.core.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.ui.theme.EponaColors
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

/**
 * Teal header shared by the top-level tabs: title (+ optional subtitle) with action buttons,
 * then free-form [content]. Always-dark like the Home hero, so text is hardcoded white.
 */
@Composable
fun TabHero(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
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
                    style = EponaTypography.headlineMedium,
                    color = Color.White
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = EponaTypography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
        }
        content()
    }
}

/** White circular action button for use on a teal hero. */
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
        modifier = modifier.size(44.dp),
        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(containerColor = EponaColors.Lost) {
                        Text(badgeCount.toString())
                    }
                }
            }
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.primaryContainer
            )
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
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .padding(top = 20.dp)
        ) {
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0F4F44)
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

@Preview(showBackground = true)
@Composable
private fun HeroSheetEdgePreview() {
    EponaTheme(dynamicColor = false) {
        HeroSheetEdge {
            Text("Section", style = EponaTypography.titleLarge, modifier = Modifier.padding(20.dp))
        }
    }
}
