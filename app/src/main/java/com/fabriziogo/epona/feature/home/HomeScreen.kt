package com.fabriziogo.epona.feature.home

import com.fabriziogo.epona.core.ui.preview.EponaPreviews
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.components.EmptyState
import com.fabriziogo.epona.core.ui.components.EponaFAB
import com.fabriziogo.epona.core.ui.components.EponaFilledButton
import com.fabriziogo.epona.core.ui.components.HeroSheetEdge
import com.fabriziogo.epona.core.ui.components.FullScreenProgress
import com.fabriziogo.epona.core.ui.layout.fullBleed
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

    // The hero is painted with primaryContainer, so pick glyphs that contrast with it.
    StatusBarIcons(darkIcons = MaterialTheme.colorScheme.primaryContainer.luminance() > 0.5f)

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
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // 16dp gutter on phones; on wide windows the cards are centred within MaxContentWidth.
        val sidePadding = maxOf(GridGutter, (maxWidth - MaxContentWidth) / 2)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            state = gridState,
            // No verticalArrangement spacing: it would open a gap between the hero and the
            // sheet edge. Cards carry their own bottom padding instead.
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            // bottom: space for FAB + nav bar
            contentPadding = PaddingValues(start = sidePadding, end = sidePadding, bottom = 96.dp)
        ) {
            // Hero: greeting + avatar, notifications/map shortcuts, headline, search, categories
            item(span = { GridItemSpan(maxLineSpan) }, key = "hero") {
                HomeHero(
                    modifier = Modifier.fullBleed(sidePadding),
                    // The hero background is full width; keep its content in the cards' column.
                    contentInset = sidePadding - GridGutter,
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
                HeroSheetEdge(modifier = Modifier.fullBleed(sidePadding)) {
                    AlertFeedSection(
                        onViewMapClick = { onEvent(HomeEvent.ViewMapClicked) },
                        horizontalPadding = sidePadding
                    )
                }
            }

            when {
                state.isLoading -> item(span = { GridItemSpan(maxLineSpan) }, key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        FullScreenProgress()
                    }
                }

                state.alerts.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                    Box(modifier = Modifier.fillMaxWidth()) {
                        EmptyState(
                            icon = Icons.Outlined.Pets,
                            title = stringResource(R.string.home_no_matches_title),
                            description = stringResource(R.string.home_no_matches_description)
                        )
                    }
                }

                else -> items(
                    items = state.filteredAlerts,
                    key = { it.alert.id }
                ) { alertWithDetails ->
                    AlertGridCard(
                        alertWithDetails = alertWithDetails,
                        onClick = {
                            onEvent(HomeEvent.AlertClicked(alertWithDetails.alert.id))
                        },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
        }
        }
    }
}

private val GridGutter = 16.dp
private val MaxContentWidth = 840.dp


@EponaPreviews
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

@Preview(showBackground = true, widthDp = 840, heightDp = 900)
@Composable
private fun HomeScreenTabletPreview() {
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

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    EponaTheme(darkTheme = true, dynamicColor = false) {
        HomeScreenContent(
            state = HomeUiState(
                isLoading = false,
                alerts = sampleAlerts,
                filteredAlerts = sampleAlerts,
                totalActiveAlerts = sampleAlerts.size,
                lostCount = 2,
                foundCount = 2,
                userName = "Fabrizio"
            ),
            paddingValues = PaddingValues(),
            onEvent = {}
        )
    }
}

@EponaPreviews
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

@EponaPreviews
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

@EponaPreviews
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
