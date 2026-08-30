package com.fabriziogo.epona.feature.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.EponaFAB
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.LoadingIndicator
import com.fabriziogo.epona.core.ui.components.PetCard
import com.fabriziogo.epona.feature.home.components.AlertFeedSection
import com.fabriziogo.epona.feature.home.components.AlertFilterChips
import com.fabriziogo.epona.feature.home.components.AlertStatsCard
import com.fabriziogo.epona.feature.home.components.HomeHeader

@Composable
fun HomeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCreateAlert: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }


    // Handle navigation events
    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is HomeNavigationEvent.NavigateToDetail ->
                    onNavigateToDetail(event.alertId)
                HomeNavigationEvent.NavigateToSearch ->
                    onNavigateToSearch()
                HomeNavigationEvent.NavigateToNotifications ->
                    onNavigateToNotifications()
                HomeNavigationEvent.NavigateToCreateAlert ->
                    onNavigateToCreateAlert()
                HomeNavigationEvent.NavigateToMap ->
                    onNavigateToMap()
            }
        }
    }

    // Show error in snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(HomeEvent.ErrorDismissed)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            EponaFAB(
                onClick = { viewModel.onEvent(HomeEvent.CreateAlertClicked) }
            )
        }
    ) { paddingValues ->

        when {
            // Loading state
            state.isLoading -> {
                LoadingIndicator(
                    modifier = Modifier.padding(paddingValues)
                )
            }

            // Empty state
            !state.isLoading && state.alerts.isEmpty() && state.error == null -> {
                EmptyState(
                    icon = Icons.Outlined.Lock,
                    title = stringResource(R.string.home_empty_title),
                    description = stringResource(R.string.home_empty_description),
                    modifier = Modifier.padding(paddingValues),
                    action = {
                        EponaFilledButton(
                            text = stringResource(R.string.create_alert_title),
                            onClick = {
                                viewModel.onEvent(HomeEvent.CreateAlertClicked)
                            }
                        )
                    }
                )
            }

            // Content
            else -> {
                HomeScreenContent(
                    state = state,
                    paddingValues = paddingValues,
                    onEvent = {
                        viewModel.onEvent(it)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(state : HomeUiState, paddingValues : PaddingValues,onEvent: (HomeEvent) -> Unit){

    val listState = rememberLazyListState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onEvent(HomeEvent.Refresh) },
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 88.dp // Space for FAB + nav bar
            )
        ) {
            // Header: greeting + avatar + search bar
            item(key = "header") {
                HomeHeader(
                    userName = state.userName,
                    userAvatar = state.userAvatar,
                    unreadCount = state.unreadNotificationCount,
                    onSearchClick = {
                        onEvent(HomeEvent.SearchClicked)
                    },
                    onNotificationsClick = {
                        onEvent(HomeEvent.NotificationsClicked)
                    },
                    onFilterClick = {}
                )
            }

            // Filter chips
            item(key = "filters") {
                AlertFilterChips(
                    selectedFilter = state.selectedFilter,
                    lostCount = state.lostCount,
                    foundCount = state.foundCount,
                    totalCount = state.totalActiveAlerts,
                    onFilterSelected = { filter ->
                        onEvent(
                            HomeEvent.FilterChanged(filter)
                        )
                    }
                )
            }

            // Stats banner
            item(key = "stats") {
                AlertStatsCard(
                    totalAlerts = state.totalActiveAlerts,
                    lostCount = state.lostCount,
                    foundCount = state.foundCount,
                    radiusKm = state.alertRadiusKm
                )
            }

            // Section header
            item(key = "section_header") {
                AlertFeedSection(
                    onViewMapClick = {
                        onEvent(HomeEvent.ViewMapClicked)
                    }
                )
            }

            // Alert cards
            items(
                items = state.filteredAlerts,
                key = { it.alert.id }
            ) { alertWithDetails ->
                PetCard(
                    alertWithDetails = alertWithDetails,
                    onClick = {
                        onEvent(
                            HomeEvent.AlertClicked(
                                alertWithDetails.alert.id
                            )
                        )
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0x0FFFFFF)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent(
        state = HomeUiState(),
        paddingValues = PaddingValues(),
        onEvent = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0x0FFFFFF)
@Composable
fun HomeScreenLoadingPreview() {
    HomeScreenContent(
        state = HomeUiState().copy(isLoading = true),
        paddingValues = PaddingValues(),
        onEvent = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0x0FFFFFF)
@Composable
fun HomeScreenEmptyPreview() {
    HomeScreenContent(
        state = HomeUiState().copy(isLoading = false, alerts = emptyList(), error = null),
        paddingValues = PaddingValues(),
        onEvent = {}
    )
}