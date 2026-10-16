package com.fabriziogo.epona.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fabriziogo.epona.app.navigation.EponaNavHost
import com.fabriziogo.epona.app.navigation.TopLevelDestination
import com.fabriziogo.epona.app.navigation.rememberEponaAppState
import com.fabriziogo.epona.feature.detail.navigation.navigateToDetail
import com.fabriziogo.epona.core.ui.permission.RequestNotificationPermissionOnceSignedIn
import com.fabriziogo.epona.core.ui.permission.rememberNotificationPermissionState

@Composable
fun EponaApp(
    deepLinkAlertId: String?,
    onDeepLinkHandled: () -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    onShareAlert: (String, String) -> Unit,
    onDialPhone: (String) -> Unit,
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val appState = rememberEponaAppState()
    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val unreadCount by appViewModel.unreadCount.collectAsStateWithLifecycle()
    val isAuthenticated = authState == true

    val notificationPermission = rememberNotificationPermissionState()
    RequestNotificationPermissionOnceSignedIn(notificationPermission, isAuthenticated)

    // Keyed on the session too, so a tap that launched the app from dead waits for the
    // session to restore instead of being dropped.
    LaunchedEffect(deepLinkAlertId, isAuthenticated) {
        val alertId = deepLinkAlertId ?: return@LaunchedEffect
        if (!isAuthenticated) return@LaunchedEffect
        appState.navController.navigateToDetail(alertId)
        onDeepLinkHandled()
    }

    // The nav graph's start destination is decided once, when it is first composed, and
    // never moves back to Home afterwards. `authState` is null until the stored session
    // has finished restoring, so nothing is composed before the real value is in --
    // otherwise a signed-in user would be fixed to the onboarding graph.
    if (authState == null) return

    // Bar on phones, rail on wider windows; hidden on screens that are not top-level tabs.
    // Every screen owns its own insets (some, like alert detail and map, draw under the
    // transparent status bar), so the content is not padded for the status bar here.
    val showSuite = appState.shouldShowBottomBar
    val suiteType = if (showSuite) {
        NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
    } else {
        NavigationSuiteType.None
    }

    // isSelected reads composition state, so resolve it here; the items block is not composable.
    val selectedByDestination = appState.topLevelDestinations.associateWith { appState.isSelected(it) }

    NavigationSuiteScaffold(
        modifier = modifier.fillMaxSize(),
        layoutType = suiteType,
        navigationSuiteItems = {
            appState.topLevelDestinations.forEach { dest ->
                val selected = selectedByDestination[dest] == true
                item(
                    selected = selected,
                    onClick = { appState.navigateToTopLevel(dest) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (dest == TopLevelDestination.ALERTS && unreadCount > 0) {
                                    Badge { Text(unreadCount.toString()) }
                                }
                            }
                        ) {
                            // The label below names the tab, so the icon stays silent.
                            Icon(
                                imageVector = if (selected) dest.selectedIcon else dest.icon,
                                contentDescription = null
                            )
                        }
                    },
                    label = { Text(stringResource(dest.labelRes)) }
                )
            }
        }
    ) {
        EponaNavHost(
            navController = appState.navController,
            isAuthenticated = isAuthenticated,
            onNavigateToTopLevel = appState::navigateToTopLevel,
            onLaunchGoogleSignIn = onLaunchGoogleSignIn,
            onShareAlert = onShareAlert,
            onDialPhone = onDialPhone,
            // With a bottom bar the bar already carries the navigation-bar inset, so consume
            // it here to keep screens from padding for it a second time.
            modifier = if (suiteType == NavigationSuiteType.NavigationBar) {
                Modifier.consumeWindowInsets(WindowInsets.navigationBars)
            } else {
                Modifier
            }
        )
    }
}