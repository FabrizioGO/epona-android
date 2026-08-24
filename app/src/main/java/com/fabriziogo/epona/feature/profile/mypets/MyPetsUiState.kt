package com.fabriziogo.epona.feature.profile.mypets

import com.fabriziogo.epona.core.domain.model.Pet

data class MyPetsUiState(
    val isLoading: Boolean = true,
    val pets: List<Pet> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val petToDelete: Pet? = null,
    val error: String? = null
)
