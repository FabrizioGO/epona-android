package com.fabriziogo.epona.feature.alert.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fabriziogo.epona.feature.alert.create.CreateAlertScreen
import com.fabriziogo.epona.feature.alert.resolve.ResolveAlertScreen

const val CREATE_ALERT_ROUTE = "alert/create"
const val RESOLVE_ALERT_ROUTE = "alert/resolve/{alertId}"

fun NavController.navigateToCreateAlert(navOptions: NavOptions? = null) {
    navigate(CREATE_ALERT_ROUTE, navOptions)
}

fun NavController.navigateToResolveAlert(
    alertId: String,
    navOptions: NavOptions? = null
) {
    navigate("alert/resolve/$alertId", navOptions)
}

fun NavGraphBuilder.alertScreens(
    onNavigateBack: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToSuccess: (String) -> Unit,
    onNavigateToHome: () -> Unit
) {
    composable(route = CREATE_ALERT_ROUTE) {
        CreateAlertScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddPet = onNavigateToAddPet,
            onNavigateToSuccess = onNavigateToSuccess
        )
    }

    composable(
        route = RESOLVE_ALERT_ROUTE,
        arguments = listOf(
            navArgument("alertId") { type = NavType.StringType }
        )
    ) {
        ResolveAlertScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToHome = onNavigateToHome
        )
    }
}