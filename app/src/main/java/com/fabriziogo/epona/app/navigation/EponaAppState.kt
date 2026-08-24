package com.fabriziogo.epona.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions

@Stable
class EponaAppState(
    val navController: NavHostController
) {
    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * Whether the current destination is a top-level screen
     * (should show bottom nav bar).
     */
    val shouldShowBottomBar: Boolean
        @Composable get() {
            val dest = currentDestination ?: return false
            return topLevelDestinations.any { top ->
                dest.hierarchy.any { it.route == top.route }
            }
        }

    /**
     * Whether the given top-level destination is currently selected.
     */
    @Composable
    fun isSelected(destination: TopLevelDestination): Boolean {
        return currentDestination?.hierarchy?.any {
            it.route == destination.route
        } == true
    }

    /**
     * Navigate to a top-level destination with proper back stack handling:
     * - Pop to start destination to avoid deep back stacks
     * - Save and restore state for each tab
     * - Don't re-navigate if already on the tab
     */
    fun navigateToTopLevel(destination: TopLevelDestination) {
        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        navController.navigate(destination.route, topLevelNavOptions)
    }
}

@Composable
fun rememberEponaAppState(
    navController: NavHostController = rememberNavController()
): EponaAppState {
    return remember(navController) {
        EponaAppState(navController)
    }
}