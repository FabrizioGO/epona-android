package com.fabriziogo.epona.feature.pet

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.model.PetGender
import com.fabriziogo.epona.core.domain.model.PetSize
import com.fabriziogo.epona.core.domain.model.Species
import com.fabriziogo.epona.core.domain.usecase.pet.CreatePetUseCase
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

data class AddPetUiState(
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
     * Photos attached so far. Local entries are uploaded by CreatePetUseCase when
     * the form is saved; on this screen they are always local.
     */
    val photos: List<PhotoItem> = emptyList(),
    val isProcessingPhotos: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val isFormValid: Boolean = false
)

sealed class AddPetEvent {
    data class NameChanged(val name: String) : AddPetEvent()
    data class SpeciesChanged(val species: Species) : AddPetEvent()
    data class BreedChanged(val breed: String) : AddPetEvent()
    data class ColorChanged(val color: String) : AddPetEvent()
    data class SizeChanged(val size: PetSize) : AddPetEvent()
    data class AgeChanged(val age: String) : AddPetEvent()
    data class GenderChanged(val gender: PetGender) : AddPetEvent()
    data class MicrochipIdChanged(val microchipId: String) : AddPetEvent()
    data class DescriptionChanged(val description: String) : AddPetEvent()
    data class PhotosPicked(val uris: List<Uri>) : AddPetEvent()
    data class PhotoRemoved(val photo: PhotoItem) : AddPetEvent()
    object SavePet : AddPetEvent()
    object ErrorDismissed : AddPetEvent()
}

sealed class AddPetNavEvent {
    object NavigateBack : AddPetNavEvent()
    object NavigateToSuccess : AddPetNavEvent()
}

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val createPetUseCase: CreatePetUseCase,
    private val imageProcessor: ImageProcessor
) : ViewModel() {
    private val _state = MutableStateFlow(AddPetUiState())
    val state: StateFlow<AddPetUiState> = _state.asStateFlow()

    private val _navEvents = Channel<AddPetNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    fun onEvent(event: AddPetEvent) {
        when (event) {
            is AddPetEvent.NameChanged -> {
                _state.value = _state.value.copy(
                    petName = event.name,
                    nameError = if (event.name.isBlank()) "Pet name is required" else null
                )
                validateForm()
            }
            is AddPetEvent.SpeciesChanged -> {
                _state.value = _state.value.copy(selectedSpecies = event.species)
            }
            is AddPetEvent.BreedChanged -> {
                _state.value = _state.value.copy(breed = event.breed)
            }
            is AddPetEvent.ColorChanged -> {
                _state.value = _state.value.copy(color = event.color)
            }
            is AddPetEvent.SizeChanged -> {
                _state.value = _state.value.copy(selectedSize = event.size)
            }
            is AddPetEvent.AgeChanged -> {
                _state.value = _state.value.copy(ageYears = event.age)
            }
            is AddPetEvent.GenderChanged -> {
                _state.value = _state.value.copy(selectedGender = event.gender)
            }
            is AddPetEvent.MicrochipIdChanged -> {
                _state.value = _state.value.copy(microchipId = event.microchipId)
            }
            is AddPetEvent.DescriptionChanged -> {
                _state.value = _state.value.copy(description = event.description)
            }
            is AddPetEvent.PhotosPicked -> addPhotos(event.uris)
            is AddPetEvent.PhotoRemoved -> removePhoto(event.photo)
            is AddPetEvent.SavePet -> {
                savePet()
            }
            is AddPetEvent.ErrorDismissed -> {
                _state.value = _state.value.copy(error = null)
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

        // A second tap while the first insert is still in flight would create a
        // duplicate pet, so the in-flight save owns the button until it settles.
        if (current.isLoading) {
            Timber.d("AddPet: save ignored, one is already in flight")
            return
        }
        if (!current.isFormValid) {
            Timber.d("AddPet: save ignored, form is not valid")
            _state.value = current.copy(nameError = "Pet name is required")
            return
        }

        viewModelScope.launch {
            Timber.d("AddPet: saving '%s'", current.petName)
            _state.value = current.copy(isLoading = true, error = null)
            try {
                val pet = Pet(
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
                createPetUseCase(pet).onSuccess { saved ->
                    Timber.d("AddPet: saved id=%s", saved.id)
                    // Uploaded and persisted, so the cache copies are dead weight.
                    // Only on success: a failed save keeps them for the retry.
                    imageProcessor.delete(current.photos.localUris())
                    _state.value = _state.value.copy(isLoading = false)
                    _navEvents.send(AddPetNavEvent.NavigateToSuccess)
                }.onFailure { exception ->
                    Timber.e(exception, "AddPet: save failed")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to save pet"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "AddPet: save failed")
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
