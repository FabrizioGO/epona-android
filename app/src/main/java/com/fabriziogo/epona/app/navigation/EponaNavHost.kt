package com.fabriziogo.epona.app.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.fabriziogo.epona.R
import com.fabriziogo.epona.core.ui.navigation.popBackStackWithLocation
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
import com.fabriziogo.epona.feature.map.navigation.focusMapOn
import com.fabriziogo.epona.feature.map.navigation.mapScreen
import com.fabriziogo.epona.feature.map.navigation.navigateToPickLocation
import com.fabriziogo.epona.feature.map.navigation.pickLocationScreen
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

// MD3 motion easing (see m3.material.io/styles/motion/easing-and-duration/tokens-specs).
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
private val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

@Composable
fun EponaNavHost(
    navController: NavHostController,
    isAuthenticated: Boolean,
    onNavigateToTopLevel: (TopLevelDestination) -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    onShareAlert: (String, String) -> Unit,
    onDialPhone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sightingReportedMessage = stringResource(R.string.sighting_reported_success)
    val petAddedMessage = stringResource(R.string.pet_added_success)
    val petUpdatedMessage = stringResource(R.string.pet_updated_success)

    // NavHost rebuilds its graph, and resets the back stack to the start destination,
    // whenever this value changes. Signing in must therefore not change it: every sign-in
    // path navigates to Home itself, and the password-reset flow signs the user in
    // (verifying the emailed code creates a session) while they still have to choose a
    // new password. Only a sign-out moves it, back to the auth graph.
    var startDestination by remember {
        mutableStateOf(if (isAuthenticated) HOME_ROUTE else AUTH_GRAPH_ROUTE)
    }
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) startDestination = AUTH_GRAPH_ROUTE
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        // Short shared-axis style transitions (a slight horizontal shift plus fade) instead
        // of the default plain fade, with the MD3 emphasized-decelerate easing on entry.
        enterTransition = {
            slideInHorizontally(tween(300, easing = EmphasizedDecelerate)) { it / 10 } +
                fadeIn(tween(300))
        },
        exitTransition = {
            slideOutHorizontally(tween(200, easing = EmphasizedAccelerate)) { -it / 10 } +
                fadeOut(tween(200))
        },
        popEnterTransition = {
            slideInHorizontally(tween(300, easing = EmphasizedDecelerate)) { -it / 10 } +
                fadeIn(tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(tween(200, easing = EmphasizedAccelerate)) { it / 10 } +
                fadeOut(tween(200))
        }
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
                onNavigateToTopLevel(TopLevelDestination.ALERTS)
            },
            onNavigateToCreateAlert = {
                navController.navigateToCreateAlert()
            },
            onNavigateToMap = {
                onNavigateToTopLevel(TopLevelDestination.MAP)
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
        pickLocationScreen(
            onLocationConfirmed = { navController.popBackStackWithLocation(it) },
            onNavigateBack = { navController.popBackStack() }
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
                onNavigateToTopLevel(TopLevelDestination.MAP)
                navController.focusMapOn(lat, lng)
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
            onNavigateToPickLocation = { navController.navigateToPickLocation(it) },
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
            },
            onNavigateToPickLocation = { navController.navigateToPickLocation(it) }
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