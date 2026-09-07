package com.fabriziogo.epona.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.fabriziogo.epona.core.ui.theme.EponaTealDark
import com.fabriziogo.epona.core.ui.theme.EponaTheme
import com.fabriziogo.epona.core.ui.theme.StatusBarIcons
import com.fabriziogo.epona.feature.home.components.AlertFeedSection
import com.fabriziogo.epona.feature.home.components.AlertGridCard
import com.fabriziogo.epona.feature.home.components.HomeHero

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

    // White glyphs read against the teal hero regardless of the app theme.
    StatusBarIcons(darkIcons = false)

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
        // The hero draws its own status-bar padding so it can sit under the transparent bar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            EponaFAB(
                onClick = { viewModel.onEvent(HomeEvent.CreateAlertClicked) }
            )
        }
    ) { paddingValues ->
        HomeScreenContent(
            state = state,
            paddingValues = paddingValues,
            onEvent = { viewModel.onEvent(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    paddingValues: PaddingValues,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onEvent(HomeEvent.Refresh) },
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(bottom = 96.dp) // space for FAB + nav bar
        ) {
            // Hero: greeting + avatar, notifications/map shortcuts, headline, search, categories
            item(span = { GridItemSpan(maxLineSpan) }, key = "hero") {
                HomeHero(
                    userName = state.userName,
                    userAvatar = state.userAvatar,
                    unreadCount = state.unreadNotificationCount,
                    searchQuery = state.searchQuery,
                    selectedFilter = state.selectedFilter,
                    totalCount = state.totalActiveAlerts,
                    lostCount = state.lostCount,
                    foundCount = state.foundCount,
                    onSearchQueryChanged = { onEvent(HomeEvent.SearchQueryChanged(it)) },
                    onFilterSelected = { onEvent(HomeEvent.FilterChanged(it)) },
                    onNotificationsClick = { onEvent(HomeEvent.NotificationsClicked) },
                    onMapClick = { onEvent(HomeEvent.ViewMapClicked) }
                )
            }

            // Rounded-top sheet transition + section header. Teal peeks behind the corners.
            item(span = { GridItemSpan(maxLineSpan) }, key = "sheet_header") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EponaTealDark)
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
                        AlertFeedSection(
                            onViewMapClick = { onEvent(HomeEvent.ViewMapClicked) }
                        )
                    }
                }
            }

            when {
                state.isLoading -> item(span = { GridItemSpan(maxLineSpan) }, key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        LoadingIndicator()
                    }
                }

                state.alerts.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        EmptyState(
                            icon = Icons.Outlined.Pets,
                            title = stringResource(R.string.home_empty_title),
                            description = stringResource(R.string.home_empty_description),
                            action = {
                                EponaFilledButton(
                                    text = stringResource(R.string.create_alert_title),
                                    onClick = { onEvent(HomeEvent.CreateAlertClicked) }
                                )
                            }
                        )
                    }
                }

                state.filteredAlerts.isEmpty() -> item(
                    span = { GridItemSpan(maxLineSpan) },
                    key = "no_matches"
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        EmptyState(
                            icon = Icons.Outlined.Pets,
                            title = stringResource(R.string.home_no_matches_title),
                            description = stringResource(R.string.home_no_matches_description)
                        )
                    }
                }

                else -> itemsIndexed(
                    items = state.filteredAlerts,
                    key = { _, item -> item.alert.id }
                ) { index, alertWithDetails ->
                    AlertGridCard(
                        alertWithDetails = alertWithDetails,
                        onClick = {
                            onEvent(HomeEvent.AlertClicked(alertWithDetails.alert.id))
                        },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(
                                start = if (index % 2 == 0) 16.dp else 6.dp,
                                end = if (index % 2 == 0) 6.dp else 16.dp,
                                bottom = 12.dp
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    EponaTheme(dynamicColor = false) {
        HomeScreenContent(
            state = HomeUiState(
                isLoading = false,
                alerts = sampleAlerts,
                filteredAlerts = sampleAlerts,
                totalActiveAlerts = sampleAlerts.size,
                lostCount = 2,
                foundCount = 2,
                userName = "Fabrizio",
                unreadNotificationCount = 3
            ),
            paddingValues = PaddingValues(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    EponaTheme(dynamicColor = false) {
        HomeScreenContent(
            state = HomeUiState(isLoading = true, userName = "Fabrizio"),
            paddingValues = PaddingValues(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    EponaTheme(dynamicColor = false) {
        HomeScreenContent(
            state = HomeUiState(isLoading = false, alerts = emptyList(), userName = "Fabrizio"),
            paddingValues = PaddingValues(),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenNoMatchesPreview() {
    EponaTheme(dynamicColor = false) {
        HomeScreenContent(
            state = HomeUiState(
                isLoading = false,
                alerts = sampleAlerts,
                filteredAlerts = emptyList(),
                searchQuery = "zzz",
                userName = "Fabrizio"
            ),
            paddingValues = PaddingValues(),
            onEvent = {}
        )
    }
}
