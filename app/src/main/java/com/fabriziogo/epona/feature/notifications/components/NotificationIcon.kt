package com.fabriziogo.epona.feature.notifications.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.fabriziogo.epona.core.domain.model.NotificationType
import com.fabriziogo.epona.core.ui.theme.extendedColors

@Composable
fun NotificationIcon(
    type: NotificationType,
    modifier: Modifier = Modifier
) {
    val extended = MaterialTheme.extendedColors
    val bg = when (type) {
        NotificationType.NEW_ALERT -> extended.lostContainer
        NotificationType.SIGHTING -> MaterialTheme.colorScheme.secondaryContainer
        NotificationType.RESOLVED -> extended.foundContainer
        NotificationType.MESSAGE -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val emoji = when (type) {
        NotificationType.NEW_ALERT -> "📢"
        NotificationType.SIGHTING -> "👁️"
        NotificationType.RESOLVED -> "🎉"
        NotificationType.MESSAGE -> "💬"
    }

    Surface(
        modifier = modifier.size(44.dp),
        shape = MaterialTheme.shapes.medium,
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center) {
            // The emoji is decorative; the notification text carries the meaning.
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
    }
}
