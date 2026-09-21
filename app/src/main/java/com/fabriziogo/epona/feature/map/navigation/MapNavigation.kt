package com.fabriziogo.epona.feature.map.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.feature.map.MapScreen
import com.fabriziogo.epona.feature.map.pick.PickLocationScreen

const val MAP_ROUTE = "map"

const val PICK_LAT_ARG = "lat"
const val PICK_LNG_ARG = "lng"
const val PICK_LOCATION_ROUTE = "location/pick?lat={lat}&lng={lng}"

fun NavController.navigateToMap(navOptions: NavOptions? = null) {
    navigate(MAP_ROUTE, navOptions)
}

fun NavController.navigateToPickLocation(
    initial: Location?,
    navOptions: NavOptions? = null
) {
    val route = if (initial != null) {
        "location/pick?lat=${initial.latitude}&lng=${initial.longitude}"
    } else {
        "location/pick"
    }
    navigate(route, navOptions)
}

fun NavGraphBuilder.mapScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreateAlert: () -> Unit
) {
    composable(route = MAP_ROUTE) {
        MapScreen(
            onNavigateToDetail = onNavigateToDetail,
            onNavigateToCreateAlert = onNavigateToCreateAlert
        )
    }
}

fun NavGraphBuilder.pickLocationScreen(
    onLocationConfirmed: (Location) -> Unit,
    onNavigateBack: () -> Unit
) {
    composable(
        route = PICK_LOCATION_ROUTE,
        arguments = listOf(
            navArgument(PICK_LAT_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(PICK_LNG_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        PickLocationScreen(
            onLocationConfirmed = onLocationConfirmed,
            onNavigateBack = onNavigateBack
        )
    }
}