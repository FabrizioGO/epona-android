package com.fabriziogo.epona.feature.profile.help.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.fabriziogo.epona.feature.profile.help.HelpScreen

const val HELP_ROUTE = "profile/help"

fun NavController.navigateToHelp(navOptions: NavOptions? = null) {
    navigate(HELP_ROUTE, navOptions)
}

fun NavGraphBuilder.helpScreen(
    onNavigateBack: () -> Unit
) {
    composable(route = HELP_ROUTE) {
        HelpScreen(onNavigateBack = onNavigateBack)
    }
}
