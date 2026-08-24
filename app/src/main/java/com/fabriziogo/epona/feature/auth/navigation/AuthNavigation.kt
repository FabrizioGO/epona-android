package com.fabriziogo.epona.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.fabriziogo.epona.feature.auth.login.LoginScreen
import com.fabriziogo.epona.feature.auth.onboarding.OnboardingScreen
import com.fabriziogo.epona.feature.auth.register.RegisterScreen

const val AUTH_GRAPH_ROUTE = "auth"
const val ONBOARDING_ROUTE = "auth/onboarding"
const val LOGIN_ROUTE = "auth/login"
const val REGISTER_ROUTE = "auth/register"

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(AUTH_GRAPH_ROUTE, navOptions)
}

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    navigate(LOGIN_ROUTE, navOptions)
}

fun NavController.navigateToRegister(navOptions: NavOptions? = null) {
    navigate(REGISTER_ROUTE, navOptions)
}

fun NavGraphBuilder.authGraph(
    onNavigateToHome: () -> Unit,
    onLaunchGoogleSignIn: () -> Unit,
    navController: NavController
) {
    navigation(
        startDestination = ONBOARDING_ROUTE,
        route = AUTH_GRAPH_ROUTE
    ) {
        composable(route = ONBOARDING_ROUTE) {
            OnboardingScreen(
                onNavigateToLogin = {
                    navController.navigateToLogin()
                },
                onNavigateToRegister = {
                    navController.navigateToRegister()
                }
            )
        }

        composable(route = LOGIN_ROUTE) {
            LoginScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateToRegister = {
                    navController.navigateToRegister(
                        NavOptions.Builder()
                            .setPopUpTo(LOGIN_ROUTE, inclusive = true)
                            .build()
                    )
                },
                onLaunchGoogleSignIn = onLaunchGoogleSignIn
            )
        }

        composable(route = REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateToHome = onNavigateToHome,
                onNavigateToLogin = {
                    navController.navigateToLogin(
                        NavOptions.Builder()
                            .setPopUpTo(REGISTER_ROUTE, inclusive = true)
                            .build()
                    )
                },
                onLaunchGoogleSignIn = onLaunchGoogleSignIn
            )
        }
    }
}