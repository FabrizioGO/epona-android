package com.fabriziogo.epona.feature.profile.myalerts.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.fabriziogo.epona.feature.profile.myalerts.MyAlertsScreen

const val MY_ALERTS_ROUTE = "profile/my_alerts"

fun NavController.navigateToMyAlerts(navOptions: NavOptions? = null) {
    navigate(MY_ALERTS_ROUTE, navOptions)
}

fun NavGraphBuilder.myAlertsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    composable(route = MY_ALERTS_ROUTE) {
        MyAlertsScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail
        )
    }
}
