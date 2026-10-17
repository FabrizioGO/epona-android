package com.fabriziogo.epona.feature.notifications

import com.fabriziogo.epona.core.ui.text.UiText

import com.fabriziogo.epona.core.domain.model.Notification

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val error: UiText? = null
)