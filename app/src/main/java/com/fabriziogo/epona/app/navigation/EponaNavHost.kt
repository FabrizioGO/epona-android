package com.fabriziogo.epona.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.navigation.popBackStackWithMessage
import com.fabriziogo.epona.feature.alert.navigation.CREATE_ALERT_ROUTE
import com.fabriziogo.epona.feature.alert.navigation.alertScreens
import com.fabriziogo.epona.feature.alert.navigation.navigateToCreateAlert
import com.fabriziogo.epona.feature.auth.navigation.AUTH_GRAPH_ROUTE
import com.fabriziogo.epona.feature.auth.navigation.authGraph
import com.fabriziogo.epona.feature.detail.navigation.detailScreen
import com.fabriziogo.epona.feature.detail.navigation.navigateToDetail
import com.fabriziogo.epona.feature.home.navigation.HOME_ROUTE
import com.fabriziogo.epona.feature.home.navigation.homeScreen
import com.fabriziogo.epona.feature.map.navigation.mapScreen
import com.fabriziogo.epona.feature.map.navigation.navigateToMap
import com.fabriziogo.epona.feature.notifications.navigation.navigateToNotifications
import com.fabriziogo.epona.feature.notifications.navigation.notificationsScreen
import com.fabriziogo.epona.feature.pet.navigation.navigateToAddPet
import com.fabriziogo.epona.feature.pet.navigation.navigateToEditPet
import com.fabriziogo.epona.feature.pet.navigation.petScreens
import com.fabriziogo.epona.feature.profile.help.navigation.helpScreen
import com.fabriziogo.epona.feature.profile.help.navigation.navigateToHelp
import com.fabriziogo.epona.feature.profile.myalerts.navigation.myAlertsScreen
import com.fabriziogo.epona.feature.profile.myalerts.navigation.navigateToMyAlerts
import com.fabriziogo.epona.feature.profile.navigation.profileGraph
import com.fabriziogo.epona.feature.sighting.navigation.navigateToReportSighting
import com.fabriziogo.epona.feature.sighting.navigation.sightingScreens

@Composable
fun EponaNavHost(
    navController: NavHostController,
    isAuthenticated: Boolean,
    onLaunchGoogleSignIn: () -> Unit,
    onShareAlert: (String, String) -> Unit,
    onDialPhone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sightingReportedMessage = stringResource(R.string.sighting_reported_success)
    val petAddedMessage = stringResource(R.string.pet_added_success)
    val petUpdatedMessage = stringResource(R.string.pet_updated_success)

    val startDestination = if (isAuthenticated) HOME_ROUTE else AUTH_GRAPH_ROUTE

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ---- Auth ----
        authGraph(
            onNavigateToHome = {
                navController.navigate(HOME_ROUTE) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }
            },
            onLaunchGoogleSignIn = onLaunchGoogleSignIn,
            navController = navController
        )

        // ---- Home ----
        homeScreen(
            onNavigateToDetail = { alertId ->
                navController.navigateToDetail(alertId)
            },
            onNavigateToSearch = {
                // TODO: Navigate to search screen
            },
            onNavigateToNotifications = {
                navController.navigateToNotifications()
            },
            onNavigateToCreateAlert = {
                navController.navigateToCreateAlert()
            },
            onNavigateToMap = {
                navController.navigateToMap()
            }
        )

        // ---- Map ----
        mapScreen(
            onNavigateToDetail = { alertId ->
                navController.navigateToDetail(alertId)
            },
            onNavigateToCreateAlert = {
                navController.navigateToCreateAlert()
            }
        )

        // ---- Notifications ----
        notificationsScreen(
            onNavigateToAlertDetail = { alertId ->
                navController.navigateToDetail(alertId)
            }
        )

        // ---- Profile ----
        profileGraph(
            onNavigateToAuth = {
                navController.navigate(AUTH_GRAPH_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onNavigateToAddPet = {
                navController.navigateToAddPet()
            },
            onNavigateToEditPet = { petId ->
                navController.navigateToEditPet(petId)
            },
            onNavigateToMyAlerts = {
                navController.navigateToMyAlerts()
            },
            onNavigateToHelp = {
                navController.navigateToHelp()
            },
            navController = navController
        )

        // ---- Alert Detail ----
        detailScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToReportSighting = { alertId ->
                navController.navigateToReportSighting(alertId)
            },
            onNavigateToMap = { lat, lng ->
                navController.navigateToMap()
            },
            onShareAlert = onShareAlert,
            onDialPhone = onDialPhone
        )

        // ---- Create / Resolve Alert ----
        alertScreens(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAddPet = {
                navController.navigateToAddPet()
            },
            onNavigateToSuccess = { alertId ->
                navController.navigateToDetail(
                    alertId,
                    navOptions { popUpTo(CREATE_ALERT_ROUTE) { inclusive = true } }
                )
            },
            onNavigateToMatch = { alertId ->
                navController.navigateToDetail(
                    alertId,
                    navOptions { popUpTo(CREATE_ALERT_ROUTE) { inclusive = true } }
                )
            },
            onNavigateToHome = {
                navController.navigate(HOME_ROUTE) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )

        // ---- Sighting ----
        sightingScreens(
            onNavigateBack = { navController.popBackStack() },
            onNavigateBackWithSuccess = {
                navController.popBackStackWithMessage(sightingReportedMessage)
            }
        )

        // ---- Pet Management ----
        petScreens(
            onNavigateBack = { navController.popBackStack() },
            onPetAdded = { navController.popBackStackWithMessage(petAddedMessage) },
            onPetUpdated = { navController.popBackStackWithMessage(petUpdatedMessage) }
        )

        // ---- My Alerts ----
        myAlertsScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDetail = { alertId ->
                navController.navigateToDetail(alertId)
            }
        )

        // ---- Help ----
        helpScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}