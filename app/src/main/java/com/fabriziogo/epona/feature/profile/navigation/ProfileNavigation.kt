package com.fabriziogo.epona.feature.profile.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.fabriziogo.epona.core.ui.navigation.clearSnackbarMessage
import com.fabriziogo.epona.core.ui.navigation.snackbarMessageAsState
import com.fabriziogo.epona.feature.profile.ProfileScreen
import com.fabriziogo.epona.feature.profile.mypets.MyPetsScreen
import com.fabriziogo.epona.feature.profile.settings.SettingsScreen

const val PROFILE_GRAPH_ROUTE = "profile_graph"
const val PROFILE_ROUTE = "profile"
const val MY_PETS_ROUTE = "profile/my_pets"
const val SETTINGS_ROUTE = "profile/settings"

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    navigate(PROFILE_ROUTE, navOptions)
}

fun NavGraphBuilder.profileGraph(
    onNavigateToAuth: () -> Unit,
    onNavigateToAddPet: () -> Unit,
    onNavigateToEditPet: (String) -> Unit,
    onNavigateToMyAlerts: () -> Unit,
    onNavigateToHelp: () -> Unit,
    navController: NavController
) {
    navigation(
        startDestination = PROFILE_ROUTE,
        route = PROFILE_GRAPH_ROUTE
    ) {
        composable(route = PROFILE_ROUTE) {
            ProfileScreen(
                onNavigateToMyPets = {
                    navController.navigate(MY_PETS_ROUTE)
                },
                onNavigateToMyAlerts = onNavigateToMyAlerts,
                onNavigateToSettings = {
                    navController.navigate(SETTINGS_ROUTE)
                },
                onNavigateToHelp = onNavigateToHelp,
                onNavigateToAuth = onNavigateToAuth
            )
        }

        composable(route = MY_PETS_ROUTE) { entry ->
            val confirmation by entry.snackbarMessageAsState()
            MyPetsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddPet = onNavigateToAddPet,
                onNavigateToEditPet = onNavigateToEditPet,
                confirmationMessage = confirmation,
                onConfirmationShown = { entry.clearSnackbarMessage() }
            )
        }

        composable(route = SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
