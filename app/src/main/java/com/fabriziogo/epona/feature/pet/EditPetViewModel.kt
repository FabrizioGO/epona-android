package com.fabriziogo.epona.feature.pet

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.domain.usecase.pet.GetPetUseCase
import com.fabriziogo.epona.core.domain.usecase.pet.UpdatePetUseCase
import com.fabriziogo.epona.core.media.ImageProcessor
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.media.localUris
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
    /**
     * Photos attached so far. Unlike the add screen this list mixes entries already
     * on the server with newly picked local ones; UpdatePetUseCase uploads only the
     * latter and substitutes them in place, so the order here is the order saved.
     */
    val photos: List<PhotoItem> = emptyList(),
    val isProcessingPhotos: Boolean = false,
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
    data class PhotosPicked(val uris: List<Uri>) : EditPetEvent()
    data class PhotoRemoved(val photo: PhotoItem) : EditPetEvent()
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
    private val updatePetUseCase: UpdatePetUseCase,
    private val imageProcessor: ImageProcessor
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
            is EditPetEvent.PhotosPicked -> addPhotos(event.uris)
            is EditPetEvent.PhotoRemoved -> removePhoto(event.photo)
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
                        photos = pet.photoUrls.map { PhotoItem.Remote(it) },
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

    /**
     * Decoding happens here rather than in the picker: it is disk work, and a scope
     * tied to the composition would drop the photos on a rotation mid-decode.
     */
    private fun addPhotos(uris: List<Uri>) {
        val free = MAX_PHOTOS_PER_ENTITY - _state.value.photos.size
        if (free <= 0 || uris.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessingPhotos = true)
            val wanted = uris.take(free)
            val added = wanted.mapNotNull { imageProcessor.process(it).getOrNull() }
            _state.value = _state.value.copy(
                photos = _state.value.photos + added.map { PhotoItem.Local(it) },
                isProcessingPhotos = false,
                // One unreadable pick should not sink the rest of the selection.
                error = if (added.size < wanted.size) PHOTO_ERROR else _state.value.error
            )
        }
    }

    /**
     * Removing a remote photo only drops it from this list — the save that follows
     * writes the shorter array, and the object in the bucket is left alone.
     */
    private fun removePhoto(photo: PhotoItem) {
        if (photo is PhotoItem.Local) imageProcessor.delete(listOf(photo.image.uri))
        _state.value = _state.value.copy(
            photos = _state.value.photos.filterNot { it.key == photo.key }
        )
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
                    photoUrls = current.photos.map { it.model }
                )
                updatePetUseCase(pet).onSuccess { saved ->
                    Timber.d("EditPet: updated id=%s", saved.id)
                    // Uploaded and persisted, so the cache copies are dead weight.
                    // Only on success: a failed save keeps them for the retry.
                    imageProcessor.delete(current.photos.localUris())
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

    private companion object {
        const val PHOTO_ERROR = "Some photos could not be added"
    }
}
