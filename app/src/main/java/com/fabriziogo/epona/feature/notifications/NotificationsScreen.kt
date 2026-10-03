package com.fabriziogo.epona.feature.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.domain.model.Notification
import com.fabriziogo.epona.core.domain.model.NotificationType
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.HeroIconButton
import com.fabriziogo.epona.core.ui.components.HeroSheetEdge
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.components.TabHero
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.EponaTypography
import com.fabriziogo.epona.core.ui.theme.StatusBarIcons
import com.fabriziogo.epona.feature.notifications.components.NotificationItem

@Composable
fun NotificationsScreen(
    onNavigateToAlertDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // White glyphs read against the teal hero regardless of the app theme.
    StatusBarIcons(darkIcons = false)

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is NotificationsNavEvent.NavigateToAlertDetail ->
                    onNavigateToAlertDetail(event.alertId)
            }
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(NotificationsEvent.ErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        // The hero draws its own status-bar padding so it can sit under the transparent bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        NotificationsScreenContent(
            state = state,
            paddingValues = padding,
            onEvent = viewModel::onEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreenContent(
    state: NotificationsUiState,
    paddingValues: PaddingValues,
    onEvent: (NotificationsEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onEvent(NotificationsEvent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "hero") {
                TabHero(
                    title = stringResource(R.string.notifications_title),
                    subtitle = if (state.isLoading) null else if (state.unreadCount > 0)
                        pluralStringResource(
                            R.plurals.notifications_unread_count,
                            state.unreadCount,
                            state.unreadCount
                        )
                    else stringResource(R.string.notifications_all_caught_up),
                    actions = {
                        if (state.unreadCount > 0) {
                            HeroIconButton(
                                icon = Icons.Outlined.DoneAll,
                                contentDescription = stringResource(R.string.notifications_mark_all_read),
                                onClick = { onEvent(NotificationsEvent.MarkAllRead) }
                            )
                        }
                    }
                )
            }

            item(key = "sheet_header") {
                HeroSheetEdge {
                    Text(
                        text = stringResource(R.string.notifications_section_recent),
                        style = EponaTypography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            when {
                state.isLoading -> item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        LoadingIndicator()
                    }
                }

                // Inside the refresh box, not beside it: an empty cache is the one
                // state where the user most wants to pull, and it was the only state
                // where pulling did nothing.
                state.notifications.isEmpty() -> item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        EmptyState(
                            icon = Icons.Outlined.Notifications,
                            title = stringResource(R.string.notifications_empty_title),
                            description = stringResource(R.string.notifications_empty_desc)
                        )
                    }
                }

                else -> items(
                    items = state.notifications,
                    key = { it.id }
                ) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            onEvent(NotificationsEvent.NotificationClicked(notification))
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

private val sampleNotifications = listOf(
    Notification(
        id = "1",
        type = NotificationType.SIGHTING,
        title = "New sighting on Luna",
        body = "Someone reported seeing Luna near Central Park.",
        isRead = false,
        createdAt = System.currentTimeMillis() - 5 * 60_000
    ),
    Notification(
        id = "2",
        type = NotificationType.NEW_ALERT,
        title = "Lost dog nearby",
        body = "A golden retriever was reported lost 800 m from you.",
        isRead = false,
        createdAt = System.currentTimeMillis() - 3 * 3_600_000
    ),
    Notification(
        id = "3",
        type = NotificationType.RESOLVED,
        title = "Alert resolved",
        body = "Max was found and is back home.",
        isRead = true,
        createdAt = System.currentTimeMillis() - 26 * 3_600_000
    )
)

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    EponaTheme(dynamicColor = false) {
        NotificationsScreenContent(
            state = NotificationsUiState(
                isLoading = false,
                notifications = sampleNotifications,
                unreadCount = 2
            ),
            paddingValues = PaddingValues(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenEmptyPreview() {
    EponaTheme(dynamicColor = false) {
        NotificationsScreenContent(
            state = NotificationsUiState(isLoading = false),
            paddingValues = PaddingValues(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenLoadingPreview() {
    EponaTheme(dynamicColor = false) {
        NotificationsScreenContent(
            state = NotificationsUiState(isLoading = true),
            paddingValues = PaddingValues(),
            onEvent = {}
        )
    }
}
