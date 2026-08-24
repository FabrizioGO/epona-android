package com.fabriziogo.epona.feature.notifications

import com.fabriziogo.epona.core.domain.model.Notification

sealed interface NotificationsEvent {
    data object Refresh : NotificationsEvent
    data object MarkAllRead : NotificationsEvent
    data class NotificationClicked(val notification: Notification) : NotificationsEvent
    data object ErrorDismissed : NotificationsEvent
}

sealed interface NotificationsNavEvent {
    data class NavigateToAlertDetail(val alertId: String) : NotificationsNavEvent
}