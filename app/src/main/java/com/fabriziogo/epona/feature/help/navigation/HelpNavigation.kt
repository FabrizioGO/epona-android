package com.fabriziogo.epona.feature.help.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.fabriziogo.epona.feature.help.HelpScreen

const val HELP_ROUTE = "help"

fun NavController.navigateToHelp() {
    navigate(HELP_ROUTE)
}

fun NavGraphBuilder.helpScreen(
    onNavigateBack: () -> Unit
) {
    composable(route = HELP_ROUTE) {
        HelpScreen(
            onNavigateBack = onNavigateBack
        )
    }
}
