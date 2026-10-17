package com.fabriziogo.epona.feature.adoption.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fabriziogo.epona.feature.adoption.create.CreateAdoptionScreen
import com.fabriziogo.epona.feature.adoption.detail.AdoptionDetailScreen

const val ADOPTION_ID_ARG = "postId"
const val ADOPTION_CREATE_ROUTE = "adoption/create"
const val ADOPTION_DETAIL_ROUTE = "adoption/detail/{$ADOPTION_ID_ARG}"

fun NavController.navigateToCreateAdoption(navOptions: NavOptions? = null) {
    navigate(ADOPTION_CREATE_ROUTE, navOptions)
}

fun NavController.navigateToAdoptionDetail(postId: String, navOptions: NavOptions? = null) {
    navigate("adoption/detail/$postId", navOptions)
}

fun NavGraphBuilder.adoptionScreens(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onShare: (String, String) -> Unit,
    onDialPhone: (String) -> Unit
) {
    composable(route = ADOPTION_CREATE_ROUTE) {
        CreateAdoptionScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail
        )
    }

    composable(
        route = ADOPTION_DETAIL_ROUTE,
        arguments = listOf(navArgument(ADOPTION_ID_ARG) { type = NavType.StringType })
    ) {
        AdoptionDetailScreen(
            onNavigateBack = onNavigateBack,
            onShare = onShare,
            onDialPhone = onDialPhone
        )
    }
}
