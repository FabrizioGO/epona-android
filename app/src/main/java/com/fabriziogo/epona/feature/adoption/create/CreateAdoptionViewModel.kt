package com.fabriziogo.epona.feature.adoption.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.AdoptionPost
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.repository.UserRepository
import com.fabriziogo.epona.core.domain.usecase.adoption.ADOPTION_TITLE_MAX_LENGTH
import com.fabriziogo.epona.core.domain.usecase.adoption.CreateAdoptionPostUseCase
import com.fabriziogo.epona.core.media.ImageProcessor
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.media.localUris
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateAdoptionViewModel @Inject constructor(
    private val createAdoptionPost: CreateAdoptionPostUseCase,
    private val userRepository: UserRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _state = MutableStateFlow(CreateAdoptionUiState())
    val state: StateFlow<CreateAdoptionUiState> = _state.asStateFlow()

    private val _navEvents = Channel<CreateAdoptionNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init { prefillPhone() }

    fun onEvent(event: CreateAdoptionEvent) {
        when (event) {
            is CreateAdoptionEvent.TitleChanged ->
                _state.update { it.copy(title = event.title.take(ADOPTION_TITLE_MAX_LENGTH)) }
            is CreateAdoptionEvent.SpeciesChanged -> _state.update { it.copy(species = event.species) }
            is CreateAdoptionEvent.BreedChanged -> _state.update { it.copy(breed = event.breed) }
            is CreateAdoptionEvent.PetNameChanged -> _state.update { it.copy(petName = event.name) }
            is CreateAdoptionEvent.AgeStageChanged -> _state.update { it.copy(ageStage = event.stage) }
            is CreateAdoptionEvent.SizeChanged -> _state.update { it.copy(size = event.size) }
            is CreateAdoptionEvent.GenderChanged -> _state.update { it.copy(gender = event.gender) }
            is CreateAdoptionEvent.DescriptionChanged -> _state.update { it.copy(description = event.description) }
            is CreateAdoptionEvent.PriceChanged ->
                // Only what a decimal amount can contain; the keyboard hint alone does not stop a paste.
                _state.update { s ->
                    s.copy(price = event.price.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(PRICE_MAX_CHARS))
                }
            is CreateAdoptionEvent.PhoneChanged -> _state.update { it.copy(phone = event.phone) }
            is CreateAdoptionEvent.PhotosPicked -> addPhotos(event.uris)
            is CreateAdoptionEvent.PhotoRemoved -> removePhoto(event.photo)
            CreateAdoptionEvent.PublishClicked -> publish()
            CreateAdoptionEvent.CloseClicked -> close()
            CreateAdoptionEvent.ErrorDismissed -> _state.update { it.copy(error = null) }
        }
    }

    /** The profile phone is a starting point; the user may post a different contact number. */
    private fun prefillPhone() {
        viewModelScope.launch {
            val phone = userRepository.observeCurrentUser().firstOrNull()?.phone
            if (!phone.isNullOrBlank()) {
                // Do not overwrite something typed while the user was loading.
                _state.update { if (it.phone.isBlank()) it.copy(phone = phone) else it }
            }
        }
    }

    /** Decoding is disk work, so it lives here and survives a rotation mid-decode. */
    private fun addPhotos(uris: List<Uri>) {
        val free = MAX_PHOTOS_PER_ENTITY - _state.value.photos.size
        if (free <= 0 || uris.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isProcessingPhotos = true) }
            val wanted = uris.take(free)
            val added = wanted.mapNotNull { imageProcessor.process(it).getOrNull() }
            _state.update {
                it.copy(
                    photos = it.photos + added.map { image -> PhotoItem.Local(image) },
                    isProcessingPhotos = false,
                    error = if (added.size < wanted.size) PHOTO_ERROR else it.error
                )
            }
        }
    }

    private fun removePhoto(photo: PhotoItem) {
        if (photo is PhotoItem.Local) imageProcessor.delete(listOf(photo.image.uri))
        _state.update { s -> s.copy(photos = s.photos.filterNot { it.key == photo.key }) }
    }

    private fun close() {
        // Nothing was published, so the decoded cache files are dead weight.
        imageProcessor.delete(_state.value.photos.localUris())
        viewModelScope.launch { _navEvents.send(CreateAdoptionNavEvent.NavigateBack) }
    }

    private fun publish() {
        val s = _state.value
        // A second tap while the first publish is in flight would create a duplicate post.
        if (s.isSubmitting) return
        if (!s.canPublish) {
            _state.update { it.copy(showErrors = true) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            val post = AdoptionPost(
                title = s.trimmedTitle,
                species = s.species,
                breed = s.breed.ifBlank { null },
                petName = s.petName.ifBlank { null },
                ageStage = s.ageStage,
                size = s.size,
                gender = s.gender,
                description = s.description.ifBlank { null },
                price = s.priceValue,
                // Local URIs; CreateAdoptionPostUseCase uploads them and swaps in public URLs.
                photoUrls = s.photos.map { it.model },
                contactPhone = s.phone.trim()
            )

            runCatching { createAdoptionPost(post) }
                .getOrElse { Result.failure(it) }
                .onSuccess { created ->
                    imageProcessor.delete(s.photos.localUris())
                    _state.update { it.copy(isSubmitting = false) }
                    _navEvents.send(CreateAdoptionNavEvent.NavigateToDetail(created.id))
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isSubmitting = false, error = err.message ?: "Failed to publish post")
                    }
                }
        }
    }

    private companion object {
        const val PHOTO_ERROR = "Some photos could not be added"
        const val PRICE_MAX_CHARS = 10
    }
}
