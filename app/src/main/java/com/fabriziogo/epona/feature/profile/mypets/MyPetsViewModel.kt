package com.fabriziogo.epona.feature.profile.mypets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.usecase.pet.DeletePetUseCase
import com.fabriziogo.epona.core.domain.usecase.pet.GetMyPetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyPetsViewModel @Inject constructor(
    private val getMyPets: GetMyPetsUseCase,
    private val deletePet: DeletePetUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyPetsUiState())
    val state: StateFlow<MyPetsUiState> = _state.asStateFlow()

    init { observePets() }

    private fun observePets() {
        viewModelScope.launch {
            getMyPets()
                // Unhandled, the failure escapes viewModelScope and isLoading stays
                // true — the screen spins forever instead of saying what went wrong.
                .catch { error ->
                    Timber.e(error, "MyPets: could not load pets")
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Could not load your pets"
                        )
                    }
                }
                .collect { pets ->
                    _state.update {
                        it.copy(isLoading = false, pets = pets)
                    }
                }
        }
    }

    fun onDeleteRequest(pet: Pet) {
        _state.update { it.copy(showDeleteDialog = true, petToDelete = pet) }
    }

    fun onDeleteConfirmed() {
        val pet = _state.value.petToDelete ?: return
        viewModelScope.launch {
            _state.update { it.copy(showDeleteDialog = false, petToDelete = null) }
            deletePet(pet.id)
                .onFailure { err ->
                    _state.update { it.copy(error = err.message) }
                }
        }
    }

    fun onDeleteDismissed() {
        _state.update { it.copy(showDeleteDialog = false, petToDelete = null) }
    }

    fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }
}
