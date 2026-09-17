package com.fabriziogo.epona.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Notification
import com.fabriziogo.epona.core.domain.usecase.notification.GetNotificationsUseCase
import com.fabriziogo.epona.core.domain.usecase.notification.MarkNotificationReadUseCase
import com.fabriziogo.epona.core.domain.usecase.notification.MarkNotificationsReadUseCase
import com.fabriziogo.epona.core.domain.usecase.notification.ObserveUnreadCountUseCase
import com.fabriziogo.epona.core.domain.usecase.notification.RefreshNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markAllRead: MarkNotificationsReadUseCase,
    private val observeUnreadCount: ObserveUnreadCountUseCase,
    private val refreshNotifications: RefreshNotificationsUseCase,
    private val markRead: MarkNotificationReadUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private val _navEvents = Channel<NotificationsNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init {
        observeNotifications()
        observeUnread()
    }

    fun onEvent(event: NotificationsEvent) {
        when (event) {
            NotificationsEvent.Refresh -> refresh()
            NotificationsEvent.MarkAllRead -> markAllAsRead()
            is NotificationsEvent.NotificationClicked ->
                onNotificationClicked(event.notification)
            NotificationsEvent.ErrorDismissed ->
                _state.update { it.copy(error = null) }
        }
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            getNotifications().collect { notifications ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        notifications = notifications
                    )
                }
            }
        }
    }

    private fun observeUnread() {
        viewModelScope.launch {
            observeUnreadCount().collect { count ->
                _state.update { it.copy(unreadCount = count) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            refreshNotifications()
                .onFailure { err ->
                    _state.update {
                        it.copy(error = err.message ?: "Failed to refresh notifications")
                    }
                }
            // The list itself is not assigned here: the refresh writes Room and the
            // Room flow in `observeNotifications` delivers the result. One writer,
            // one reader, no chance of the two disagreeing.
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            markAllRead()
                .onFailure { err ->
                    _state.update {
                        it.copy(error = err.message ?: "Failed to mark as read")
                    }
                }
        }
    }

    private fun onNotificationClicked(notification: Notification) {
        // Navigation first: the read flag is bookkeeping, and a slow round trip must
        // not sit between the tap and the alert.
        notification.alertId?.let { alertId ->
            viewModelScope.launch {
                _navEvents.send(
                    NotificationsNavEvent.NavigateToAlertDetail(alertId)
                )
            }
        }

        if (!notification.isRead) {
            viewModelScope.launch {
                markRead(notification.id)
                    .onFailure { err ->
                        // Silent on purpose: the row stays unread and the next refresh
                        // or realtime tick will show it as such, which is the truth.
                        Timber.w(err, "Could not mark notification ${notification.id} read")
                    }
            }
        }
    }
}