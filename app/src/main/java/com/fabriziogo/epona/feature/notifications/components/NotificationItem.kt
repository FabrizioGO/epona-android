package com.fabriziogo.epona.feature.notifications.components

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Notification
import com.fabriziogo.epona.core.domain.model.NotificationType
import com.fabriziogo.epona.core.ui.components.formatTimeAgo
import com.fabriziogo.epona.core.ui.theme.EponaTheme

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadDescription = stringResource(R.string.notification_unread)

    ListItem(
        headlineContent = {
            Text(
                text = notification.title,
                style = if (notification.isRead) MaterialTheme.typography.titleSmall
                else MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        },
        supportingContent = {
            Text(
                text = notification.body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            NotificationIcon(
                type = notification.type,
                modifier = Modifier.size(44.dp)
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatTimeAgo(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!notification.isRead) {
                    Badge(modifier = Modifier.padding(top = 6.dp))
                }
            }
        },
        // Flat, tonal rows: unread is the stronger container, read is the lightest.
        colors = ListItemDefaults.colors(
            containerColor = if (notification.isRead)
                MaterialTheme.colorScheme.surfaceContainerLowest
            else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick)
            .then(
                if (notification.isRead) Modifier
                else Modifier.semantics { stateDescription = unreadDescription }
            )
    )
}


@EponaPreviews
@Composable
private fun NotificationItemPreview() {
    EponaTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            NotificationItem(
                notification = Notification(
                    id = "1",
                    type = NotificationType.SIGHTING,
                    title = "New sighting on Luna",
                    body = "Someone reported seeing Luna near Central Park.",
                    isRead = false,
                    createdAt = System.currentTimeMillis() - 300_000
                ),
                onClick = {}
            )
            NotificationItem(
                notification = Notification(
                    id = "2",
                    type = NotificationType.RESOLVED,
                    title = "Alert resolved",
                    body = "Max was found and is back home.",
                    isRead = true,
                    createdAt = System.currentTimeMillis() - 86_400_000
                ),
                onClick = {}
            )
        }
    }
}
