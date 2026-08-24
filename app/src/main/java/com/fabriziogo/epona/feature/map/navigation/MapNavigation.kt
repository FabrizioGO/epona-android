package com.fabriziogo.epona.feature.map.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.fabriziogo.epona.feature.map.MapScreen

const val MAP_ROUTE = "map"

fun NavController.navigateToMap(navOptions: NavOptions? = null) {
    navigate(MAP_ROUTE, navOptions)
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