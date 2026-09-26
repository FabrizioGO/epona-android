package com.fabriziogo.epona.feature.notifications.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fabriziogo.epona.feature.notifications.NotificationsScreen

const val NOTIFICATIONS_ROUTE = "notifications"

fun NavGraphBuilder.notificationsScreen(
    onNavigateToAlertDetail: (String) -> Unit
) {
    composable(route = NOTIFICATIONS_ROUTE) {
        NotificationsScreen(
            onNavigateToAlertDetail = onNavigateToAlertDetail
        )
    }
}
