package com.fabriziogo.epona.feature.sighting.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.ui.navigation.clearPickedLocation
import com.fabriziogo.epona.core.ui.navigation.pickedLocation
import com.fabriziogo.epona.feature.sighting.list.SightingListScreen
import com.fabriziogo.epona.feature.sighting.report.ReportSightingScreen

const val SIGHTING_ALERT_ID_ARG = "alertId"
const val REPORT_SIGHTING_ROUTE = "sighting/report/{$SIGHTING_ALERT_ID_ARG}"
const val SIGHTING_LIST_ROUTE = "sighting/list/{$SIGHTING_ALERT_ID_ARG}"

fun NavController.navigateToReportSighting(
    alertId: String,
    navOptions: NavOptions? = null
) {
    navigate("sighting/report/$alertId", navOptions)
}

fun NavController.navigateToSightingList(
    alertId: String,
    navOptions: NavOptions? = null
) {
    navigate("sighting/list/$alertId", navOptions)
}

fun NavGraphBuilder.sightingScreens(
    onNavigateBack: () -> Unit,
    onNavigateBackWithSuccess: () -> Unit,
    onNavigateToPickLocation: (Location?) -> Unit
) {
    composable(
        route = REPORT_SIGHTING_ROUTE,
        arguments = listOf(
            navArgument(SIGHTING_ALERT_ID_ARG) { type = NavType.StringType }
        )
    ) { entry ->
        ReportSightingScreen(
            onNavigateBack = onNavigateBack,
            onNavigateBackWithSuccess = onNavigateBackWithSuccess,
            onNavigateToPickLocation = onNavigateToPickLocation,
            pickedLocation = entry.pickedLocation(),
            onPickedLocationConsumed = { entry.clearPickedLocation() }
        )
    }

    composable(
        route = SIGHTING_LIST_ROUTE,
        arguments = listOf(
            navArgument(SIGHTING_ALERT_ID_ARG) { type = NavType.StringType }
        )
    ) {
        SightingListScreen(
            onNavigateBack = onNavigateBack
        )
    }
}


