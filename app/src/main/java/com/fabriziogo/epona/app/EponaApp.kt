package com.fabriziogo.epona.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.app.navigation.EponaNavHost
import com.fabriziogo.epona.app.navigation.rememberEponaAppState
import com.fabriziogo.epona.feature.detail.navigation.navigateToDetail
import com.fabriziogo.epona.core.ui.components.EponaNavigationBar
import com.fabriziogo.epona.core.ui.components.NavBarItem

@Composable
fun EponaApp(
    deepLinkAlertId: String?,
    onLaunchGoogleSignIn: () -> Unit,
    onShareAlert: (String, String) -> Unit,
    onDialPhone: (String) -> Unit,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val appState = rememberEponaAppState()
    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val unreadCount by appViewModel.unreadCount.collectAsStateWithLifecycle()

    // Handle deep link on first composition
    LaunchedEffect(deepLinkAlertId) {
        if (deepLinkAlertId != null && authState) {
            appState.navController.navigateToDetail(deepLinkAlertId)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // Every screen owns its own insets. Some of them (alert detail, map) deliberately
        // draw under the transparent status bar, and the rest already pad themselves
        // through their own Scaffold or TopAppBar, so padding the NavHost here as well
        // would add the status-bar height to each of them a second time.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = appState.shouldShowBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                EponaNavigationBar(
                    items = appState.topLevelDestinations.map { dest ->
                        NavBarItem(
                            route = dest.route,
                            label = stringResource(dest.labelRes),
                            icon = dest.icon,
                            selectedIcon = dest.selectedIcon,
                            badgeCount = if (dest == com.fabriziogo.epona.app.navigation.TopLevelDestination.ALERTS)
                                unreadCount else 0
                        )
                    },
                    currentRoute = appState.topLevelDestinations.firstOrNull { dest ->
                        appState.isSelected(dest)
                    }?.route ?: "",
                    onItemClick = { route ->
                        val dest = appState.topLevelDestinations.first { it.route == route }
                        appState.navigateToTopLevel(dest)
                    }
                )
            }
        }
    ) { padding ->
        EponaNavHost(
            navController = appState.navController,
            isAuthenticated = authState,
            deepLinkAlertId = deepLinkAlertId,
            onLaunchGoogleSignIn = onLaunchGoogleSignIn,
            onShareAlert = onShareAlert,
            onDialPhone = onDialPhone,
            // The bottom bar carries the navigation-bar inset, so consuming its height here
            // keeps screens from padding for that bar on top of it.
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        )
    }
}