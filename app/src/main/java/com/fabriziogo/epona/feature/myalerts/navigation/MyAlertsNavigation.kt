package com.fabriziogo.epona.feature.myalerts.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fabriziogo.epona.feature.myalerts.MyAlertsScreen

const val MY_ALERTS_ROUTE = "alerts/my"

fun NavController.navigateToMyAlerts() {
    navigate(MY_ALERTS_ROUTE)
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
