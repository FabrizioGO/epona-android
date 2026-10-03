package com.fabriziogo.epona.feature.notifications.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.domain.model.Notification
import com.fabriziogo.epona.core.domain.model.NotificationType
import com.fabriziogo.epona.core.ui.components.formatTimeAgo
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (notification.isRead)
            MaterialTheme.colorScheme.surfaceContainerLowest
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
            .compositeOver(MaterialTheme.colorScheme.surfaceContainerLowest),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            NotificationIcon(
                type = notification.type,
                modifier = Modifier.size(44.dp)
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = if (notification.isRead) EponaTypography.titleSmall
                        else EponaTypography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notification.isRead) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }

                Text(
                    text = notification.body,
                    style = EponaTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Text(
                    text = formatTimeAgo(notification.createdAt),
                    style = EponaTypography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
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
