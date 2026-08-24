package com.fabriziogo.epona.feature.profile.addeditpet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fabriziogo.epona.feature.profile.addeditpet.AddEditPetScreen

const val PET_ID_ARG = "petId"
const val ADD_EDIT_PET_ROUTE = "pet/addEdit?petId={$PET_ID_ARG}"

fun NavController.navigateToAddPet(navOptions: NavOptions? = null) {
    navigate("pet/addEdit", navOptions)
}

fun NavController.navigateToEditPet(
    petId: String,
    navOptions: NavOptions? = null
) {
    navigate("pet/addEdit?petId=$petId", navOptions)
}

fun NavGraphBuilder.addEditPetScreen(
    onNavigateBack: () -> Unit,
    onNavigateBackWithSuccess: () -> Unit
) {
    composable(
        route = ADD_EDIT_PET_ROUTE,
        arguments = listOf(
            navArgument(PET_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) {
        AddEditPetScreen(
            onNavigateBack = onNavigateBack,
            onNavigateBackWithSuccess = onNavigateBackWithSuccess
        )
    }
}
