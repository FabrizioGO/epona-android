package com.fabriziogo.epona.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.fabriziogo.epona.feature.home.HomeScreen

const val HOME_ROUTE = "home"

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HOME_ROUTE, navOptions)
}

fun NavGraphBuilder.homeScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToCreateAlert: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    composable(route = HOME_ROUTE) {
        HomeScreen(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToCreateAlert = onNavigateToCreateAlert,
            onNavigateToMap = onNavigateToMap
        )
    }
}