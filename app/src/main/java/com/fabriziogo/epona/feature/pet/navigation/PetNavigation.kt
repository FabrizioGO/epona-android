package com.fabriziogo.epona.feature.pet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fabriziogo.epona.feature.pet.AddPetScreen
import com.fabriziogo.epona.feature.pet.EditPetScreen

const val ADD_PET_ROUTE = "pet/add"
const val EDIT_PET_ROUTE = "pet/edit/{petId}"

fun NavController.navigateToAddPet() {
    navigate(ADD_PET_ROUTE)
}

fun NavController.navigateToEditPet(petId: String) {
    navigate("pet/edit/$petId")
}

fun NavGraphBuilder.petScreens(
    onNavigateBack: () -> Unit,
    onPetAdded: () -> Unit,
    onPetUpdated: () -> Unit
) {
    composable(route = ADD_PET_ROUTE) {
        AddPetScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSuccess = onPetAdded
        )
    }

    composable(
        route = EDIT_PET_ROUTE,
        arguments = listOf(
            navArgument("petId") { type = NavType.StringType }
        )
    ) {
        EditPetScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSuccess = onPetUpdated
        )
    }
}
