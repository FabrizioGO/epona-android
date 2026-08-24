package com.fabriziogo.epona.feature.detail.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fabriziogo.epona.core.ui.navigation.clearSnackbarMessage
import com.fabriziogo.epona.core.ui.navigation.snackbarMessageAsState
import com.fabriziogo.epona.feature.detail.AlertDetailScreen

const val ALERT_ID_ARG = "alertId"
const val DETAIL_ROUTE = "detail/{$ALERT_ID_ARG}"

fun NavController.navigateToDetail(
    alertId: String,
    navOptions: NavOptions? = null
) {
    navigate("detail/$alertId", navOptions)
}

fun NavGraphBuilder.detailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReportSighting: (String) -> Unit,
    onNavigateToMap: (Double, Double) -> Unit,
    onShareAlert: (String, String) -> Unit,
    onDialPhone: (String) -> Unit
) {
    composable(
        route = DETAIL_ROUTE,
        arguments = listOf(
            navArgument(ALERT_ID_ARG) { type = NavType.StringType }
        )
    ) { entry ->
        val confirmation by entry.snackbarMessageAsState()
        AlertDetailScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToReportSighting = onNavigateToReportSighting,
            onNavigateToMap = onNavigateToMap,
            onShareAlert = onShareAlert,
            onDialPhone = onDialPhone,
            confirmationMessage = confirmation,
            onConfirmationShown = { entry.clearSnackbarMessage() }
        )
    }
}