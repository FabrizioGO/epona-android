package com.fabriziogo.epona.feature.pet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.domain.usecase.pet.GetPetUseCase
import com.fabriziogo.epona.core.domain.usecase.pet.UpdatePetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class EditPetUiState(
    val petId: String = "",
    val petName: String = "",
    val selectedSpecies: Species = Species.DOG,
    val breed: String = "",
    val color: String = "",
    val selectedSize: PetSize = PetSize.MEDIUM,
    val ageYears: String = "",
    val selectedGender: PetGender = PetGender.UNKNOWN,
    val microchipId: String = "",
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val isFormValid: Boolean = false
)

sealed class EditPetEvent {
    data class NameChanged(val name: String) : EditPetEvent()
    data class SpeciesChanged(val species: Species) : EditPetEvent()
    data class BreedChanged(val breed: String) : EditPetEvent()
    data class ColorChanged(val color: String) : EditPetEvent()
    data class SizeChanged(val size: PetSize) : EditPetEvent()
    data class AgeChanged(val age: String) : EditPetEvent()
    data class GenderChanged(val gender: PetGender) : EditPetEvent()
    data class MicrochipIdChanged(val microchipId: String) : EditPetEvent()
    data class DescriptionChanged(val description: String) : EditPetEvent()
    data class PhotosSelected(val photoUrls: List<String>) : EditPetEvent()
    object SavePet : EditPetEvent()
    object ErrorDismissed : EditPetEvent()
}

sealed class EditPetNavEvent {
    object NavigateBack : EditPetNavEvent()
    object NavigateToSuccess : EditPetNavEvent()
}

@HiltViewModel
class EditPetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPetUseCase: GetPetUseCase,
    private val updatePetUseCase: UpdatePetUseCase
) : ViewModel() {
    private val petId: String = savedStateHandle.get<String>("petId") ?: ""

    private val _state = MutableStateFlow(EditPetUiState(petId = petId))
    val state: StateFlow<EditPetUiState> = _state.asStateFlow()

    private val _navEvents = Channel<EditPetNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init {
        loadPet()
    }

    fun onEvent(event: EditPetEvent) {
        when (event) {
            is EditPetEvent.NameChanged -> {
                _state.value = _state.value.copy(
                    petName = event.name,
                    nameError = if (event.name.isBlank()) "Pet name is required" else null
                )
                validateForm()
            }
            is EditPetEvent.SpeciesChanged -> {
                _state.value = _state.value.copy(selectedSpecies = event.species)
            }
            is EditPetEvent.BreedChanged -> {
                _state.value = _state.value.copy(breed = event.breed)
            }
            is EditPetEvent.ColorChanged -> {
                _state.value = _state.value.copy(color = event.color)
            }
            is EditPetEvent.SizeChanged -> {
                _state.value = _state.value.copy(selectedSize = event.size)
            }
            is EditPetEvent.AgeChanged -> {
                _state.value = _state.value.copy(ageYears = event.age)
            }
            is EditPetEvent.GenderChanged -> {
                _state.value = _state.value.copy(selectedGender = event.gender)
            }
            is EditPetEvent.MicrochipIdChanged -> {
                _state.value = _state.value.copy(microchipId = event.microchipId)
            }
            is EditPetEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(description = event.description)
            }
            is EditPetEvent.PhotosSelected -> {
                _state.value = _state.value.copy(photoUrls = event.photoUrls)
            }
            is EditPetEvent.SavePet -> {
                savePet()
            }
            is EditPetEvent.ErrorDismissed -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun loadPet() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                getPetUseCase(petId).onSuccess { pet ->
                    _state.value = EditPetUiState(
                        petId = pet.id,
                        petName = pet.name,
                        selectedSpecies = pet.species,
                        breed = pet.breed ?: "",
                        color = pet.color ?: "",
                        selectedSize = pet.size,
                        ageYears = pet.ageYears?.toString() ?: "",
                        selectedGender = pet.gender,
                        microchipId = pet.microchipId ?: "",
                        description = pet.description ?: "",
                        photoUrls = pet.photoUrls,
                        isLoading = false,
                        isFormValid = true
                    )
                }.onFailure { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load pet"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load pet"
                )
            }
        }
    }

    private fun validateForm() {
        val isValid = _state.value.petName.isNotBlank()
        _state.value = _state.value.copy(isFormValid = isValid)
    }

    private fun savePet() {
        val current = _state.value

        // Also covers the initial load: isLoading is true until the pet is on screen,
        // and a second tap mid-update would fire a redundant round trip.
        if (current.isLoading) {
            Timber.d("EditPet: save ignored, one is already in flight")
            return
        }
        if (!current.isFormValid) {
            Timber.d("EditPet: save ignored, form is not valid")
            _state.value = current.copy(nameError = "Pet name is required")
            return
        }

        viewModelScope.launch {
            Timber.d("EditPet: updating id=%s", current.petId)
            _state.value = current.copy(isLoading = true, error = null)
            try {
                val pet = Pet(
                    id = current.petId,
                    name = current.petName.trim(),
                    species = current.selectedSpecies,
                    breed = current.breed.takeIf { it.isNotBlank() },
                    color = current.color.takeIf { it.isNotBlank() },
                    size = current.selectedSize,
                    ageYears = current.ageYears.toIntOrNull(),
                    gender = current.selectedGender,
                    microchipId = current.microchipId.takeIf { it.isNotBlank() },
                    description = current.description.takeIf { it.isNotBlank() },
                    photoUrls = current.photoUrls
                )
                updatePetUseCase(pet).onSuccess { saved ->
                    Timber.d("EditPet: updated id=%s", saved.id)
                    _state.value = _state.value.copy(isLoading = false)
                    _navEvents.send(EditPetNavEvent.NavigateToSuccess)
                }.onFailure { exception ->
                    Timber.e(exception, "EditPet: update failed")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to save pet"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "EditPet: update failed")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save pet"
                )
            }
        }
    }
}
