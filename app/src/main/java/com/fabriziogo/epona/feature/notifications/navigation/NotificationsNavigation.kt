package com.fabriziogo.epona.feature.notifications.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.fabriziogo.epona.feature.notifications.NotificationsScreen

const val NOTIFICATIONS_ROUTE = "notifications"

fun NavController.navigateToNotifications(navOptions: NavOptions? = null) {
    navigate(NOTIFICATIONS_ROUTE, navOptions)
}

fun NavGraphBuilder.notificationsScreen(
    onNavigateToAlertDetail: (String) -> Unit
) {
    composable(route = NOTIFICATIONS_ROUTE) {
        NotificationsScreen(
            onNavigateToAlertDetail = onNavigateToAlertDetail
        )
    }
}
