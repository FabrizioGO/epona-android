package com.fabriziogo.epona.feature.profile.addeditpet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.usecase.pet.GetPetUseCase
import com.fabriziogo.epona.core.domain.usecase.pet.CreatePetUseCase
import com.fabriziogo.epona.core.domain.usecase.pet.UpdatePetUseCase
import com.fabriziogo.epona.feature.profile.addeditpet.navigation.PET_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditPetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPet: GetPetUseCase,
    private val createPet: CreatePetUseCase,
    private val updatePet: UpdatePetUseCase
) : ViewModel() {

    private val petId: String? = savedStateHandle[PET_ID_ARG]

    private var originalPet: Pet? = null

    private val _state = MutableStateFlow(AddEditPetUiState(isEditMode = petId != null))
    val state: StateFlow<AddEditPetUiState> = _state.asStateFlow()

    private val _navEvents = Channel<AddEditPetNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init {
        petId?.let { loadPet(it) }
    }

    fun onEvent(event: AddEditPetEvent) {
        when (event) {
            is AddEditPetEvent.NameChanged ->
                _state.update { it.copy(name = event.value, nameError = null) }

            is AddEditPetEvent.SpeciesSelected ->
                _state.update { it.copy(species = event.value) }

            is AddEditPetEvent.BreedChanged ->
                _state.update { it.copy(breed = event.value) }

            is AddEditPetEvent.ColorChanged ->
                _state.update { it.copy(color = event.value) }

            is AddEditPetEvent.SizeSelected ->
                _state.update { it.copy(size = event.value) }

            is AddEditPetEvent.AgeChanged ->
                _state.update { it.copy(ageYears = event.value.filter { c -> c.isDigit() }) }

            is AddEditPetEvent.GenderSelected ->
                _state.update { it.copy(gender = event.value) }

            is AddEditPetEvent.MicrochipChanged ->
                _state.update { it.copy(microchipId = event.value) }

            is AddEditPetEvent.DescriptionChanged ->
                _state.update { it.copy(description = event.value) }

            is AddEditPetEvent.PhotoAdded ->
                _state.update { it.copy(photoUrl = event.uri) }

            AddEditPetEvent.PhotoRemoved ->
                _state.update { it.copy(photoUrl = null) }

            AddEditPetEvent.SaveClicked -> save()

            AddEditPetEvent.BackClicked -> emitNav(AddEditPetNavEvent.NavigateBack)

            AddEditPetEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    private fun loadPet(id: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getPet(id)
                .onSuccess { pet ->
                    originalPet = pet
                    _state.update {
                        it.copy(
                            isLoading = false,
                            name = pet.name,
                            species = pet.species,
                            breed = pet.breed.orEmpty(),
                            color = pet.color.orEmpty(),
                            size = pet.size,
                            ageYears = pet.ageYears?.toString().orEmpty(),
                            gender = pet.gender,
                            microchipId = pet.microchipId.orEmpty(),
                            description = pet.description.orEmpty(),
                            photoUrl = pet.photoUrls.firstOrNull()
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.message ?: "Failed to load pet")
                    }
                }
        }
    }

    private fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(nameError = "Name is required") }
            return
        }

        val pet = Pet(
            id = originalPet?.id ?: "",
            ownerId = originalPet?.ownerId ?: "",
            name = s.name.trim(),
            species = s.species,
            breed = s.breed.trim().ifBlank { null },
            color = s.color.trim().ifBlank { null },
            size = s.size,
            ageYears = s.ageYears.toIntOrNull(),
            gender = s.gender,
            microchipId = s.microchipId.trim().ifBlank { null },
            description = s.description.trim().ifBlank { null },
            photoUrls = listOfNotNull(s.photoUrl),
            createdAt = originalPet?.createdAt ?: 0L,
            updatedAt = originalPet?.updatedAt ?: 0L
        )

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }

            val result = if (s.isEditMode) updatePet(pet) else createPet(pet)

            result
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                    _navEvents.send(AddEditPetNavEvent.NavigateBackWithSuccess)
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isSaving = false,
                            error = err.message ?: "Failed to save pet"
                        )
                    }
                }
        }
    }

    private fun emitNav(event: AddEditPetNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }
}
