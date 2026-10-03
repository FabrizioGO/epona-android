package com.fabriziogo.epona.feature.auth.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.fabriziogo.epona.feature.auth.forgotpassword.FORGOT_PASSWORD_EMAIL_ARG
import com.fabriziogo.epona.feature.auth.forgotpassword.ForgotPasswordScreen
import com.fabriziogo.epona.feature.auth.login.LoginScreen
import com.fabriziogo.epona.feature.auth.onboarding.OnboardingScreen
import com.fabriziogo.epona.feature.auth.register.RegisterScreen

const val AUTH_GRAPH_ROUTE = "auth"
const val ONBOARDING_ROUTE = "auth/onboarding"
const val LOGIN_ROUTE = "auth/login"
const val REGISTER_ROUTE = "auth/register"
const val FORGOT_PASSWORD_ROUTE = "auth/forgot-password?$FORGOT_PASSWORD_EMAIL_ARG={$FORGOT_PASSWORD_EMAIL_ARG}"

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(AUTH_GRAPH_ROUTE, navOptions)
}

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    navigate(LOGIN_ROUTE, navOptions)
}

fun NavController.navigateToRegister(navOptions: NavOptions? = null) {
    navigate(REGISTER_ROUTE, navOptions)
}

fun NavController.navigateToForgotPassword(email: String = "", navOptions: NavOptions? = null) {
    navigate("auth/forgot-password?$FORGOT_PASSWORD_EMAIL_ARG=${Uri.encode(email)}", navOptions)
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
                onLaunchGoogleSignIn = onLaunchGoogleSignIn,
                onNavigateToForgotPassword = { email ->
                    navController.navigateToForgotPassword(email)
                }
            )
        }

        composable(
            route = FORGOT_PASSWORD_ROUTE,
            arguments = listOf(
                navArgument(FORGOT_PASSWORD_EMAIL_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = onNavigateToHome
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