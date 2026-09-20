package com.fabriziogo.epona.feature.alert.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.domain.model.MAX_PHOTOS_PER_ENTITY
import com.fabriziogo.epona.core.domain.model.Pet
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import com.fabriziogo.epona.core.domain.usecase.alert.CreateAlertUseCase
import com.fabriziogo.epona.core.domain.usecase.alert.CreateFoundAlertUseCase
import com.fabriziogo.epona.core.domain.usecase.alert.FindMatchingAlertsUseCase
import com.fabriziogo.epona.core.domain.usecase.pet.GetMyPetsUseCase
import com.fabriziogo.epona.core.media.ImageProcessor
import com.fabriziogo.epona.core.media.PhotoItem
import com.fabriziogo.epona.core.media.localUris
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateAlertViewModel @Inject constructor(
    private val getMyPets: GetMyPetsUseCase,
    private val createAlert: CreateAlertUseCase,
    private val createFoundAlert: CreateFoundAlertUseCase,
    private val findMatchingAlerts: FindMatchingAlertsUseCase,
    private val locationRepo: LocationRepository,
    private val imageProcessor: ImageProcessor
) : ViewModel() {

    private val _state = MutableStateFlow(CreateAlertUiState())
    val state: StateFlow<CreateAlertUiState> = _state.asStateFlow()

    private val _navEvents = Channel<CreateAlertNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init { loadMyPets() }

    fun onEvent(event: CreateAlertEvent) {
        when (event) {
            CreateAlertEvent.NextStep -> nextStep()
            CreateAlertEvent.PreviousStep -> previousStep()
            CreateAlertEvent.CloseClicked ->
                emitNav(CreateAlertNavEvent.NavigateBack)

            // Changing the type drops the custody answer: it belongs to a found
            // report, and left behind it would claim on the review step that a
            // lost pet is safe at someone's house.
            is CreateAlertEvent.AlertTypeSelected -> _state.update {
                if (it.alertType == event.type) it
                else it.copy(alertType = event.type, custody = null)
            }

            is CreateAlertEvent.PetSelected ->
                _state.update { it.copy(selectedPet = event.pet) }
            CreateAlertEvent.AddNewPetClicked ->
                emitNav(CreateAlertNavEvent.NavigateToAddPet)

            is CreateAlertEvent.FoundPetSpeciesChanged ->
                _state.update { it.copy(foundPet = it.foundPet.copy(species = event.species)) }
            is CreateAlertEvent.FoundPetBreedChanged ->
                _state.update { it.copy(foundPet = it.foundPet.copy(breed = event.breed)) }
            is CreateAlertEvent.FoundPetColorChanged ->
                _state.update { it.copy(foundPet = it.foundPet.copy(color = event.color)) }
            is CreateAlertEvent.FoundPetSizeChanged ->
                _state.update { it.copy(foundPet = it.foundPet.copy(size = event.size)) }
            is CreateAlertEvent.FoundPetDescriptionChanged ->
                _state.update { it.copy(foundPet = it.foundPet.copy(description = event.description)) }
            is CreateAlertEvent.FoundPhotosPicked -> addFoundPhotos(event.uris)
            is CreateAlertEvent.FoundPhotoRemoved -> removeFoundPhoto(event.photo)

            CreateAlertEvent.UseCurrentLocation -> autoDetectLocation()
            CreateAlertEvent.LocationPermissionDenied -> _state.update {
                it.copy(
                    isLoadingLocation = false,
                    locationError = "Location permission denied — pick the location " +
                            "on the map, or enable it in Settings."
                )
            }
            is CreateAlertEvent.LocationPicked -> setLocation(event.location)
            is CreateAlertEvent.CustodySelected ->
                _state.update { it.copy(custody = event.custody) }

            is CreateAlertEvent.DescriptionChanged ->
                _state.update { it.copy(description = event.text) }

            is CreateAlertEvent.MatchSelected ->
                emitNav(CreateAlertNavEvent.NavigateToMatch(event.alertId))

            is CreateAlertEvent.PhoneChanged ->
                _state.update { it.copy(contactPhone = event.phone) }
            is CreateAlertEvent.RewardChanged ->
                _state.update { it.copy(reward = event.reward) }
            CreateAlertEvent.PublishClicked -> publish()

            CreateAlertEvent.ErrorDismissed ->
                _state.update { it.copy(error = null) }
        }
    }

    private fun loadMyPets() {
        viewModelScope.launch {
            getMyPets()
                // Same reason as MyPets: an uncaught failure here would take the
                // wizard down with it and leave the pet step loading forever.
                .catch { error ->
                    Timber.e(error, "CreateAlert: could not load pets")
                    _state.update {
                        it.copy(
                            isLoadingPets = false,
                            error = error.message ?: "Could not load your pets"
                        )
                    }
                }
                .collect { pets ->
                    _state.update {
                        it.copy(
                            myPets = pets,
                            isLoadingPets = false,
                            selectedPet = if (pets.size == 1) pets.first()
                            else it.selectedPet
                        )
                    }
                }
        }
    }

    private fun nextStep() {
        if (!_state.value.canProceed) return
        if (_state.value.currentStep < _state.value.totalSteps) {
            _state.update { it.copy(currentStep = it.currentStep + 1) }
            if (_state.value.currentStep == MATCHES_STEP) loadMatches()
        }
    }

    private fun previousStep() {
        if (_state.value.currentStep > 1) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        } else {
            emitNav(CreateAlertNavEvent.NavigateBack)
        }
    }

    /**
     * Nearby posts of the opposite type that might be the same animal, keyed on
     * the current species/breed/color/location — so returning to this step after
     * an edit refetches. A failed lookup surfaces inline and never blocks
     * publishing.
     */
    private fun loadMatches() {
        val s = _state.value
        val loc = s.location ?: return
        val (species, breed, color, lookingFor) = if (s.alertType == AlertType.LOST) {
            val pet = s.selectedPet ?: return
            MatchQuery(pet.species, pet.breed, pet.color, AlertType.FOUND)
        } else {
            MatchQuery(
                s.foundPet.species,
                s.foundPet.breed.takeIf { it.isNotBlank() },
                s.foundPet.color.takeIf { it.isNotBlank() },
                AlertType.LOST
            )
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMatches = true, matchesError = null) }
            findMatchingAlerts(
                species = species,
                breed = breed,
                color = color,
                location = loc,
                lookingFor = lookingFor
            )
                .onSuccess { matches ->
                    _state.update { it.copy(isLoadingMatches = false, matches = matches) }
                }
                .onFailure { err ->
                    Timber.w(err, "CreateAlert: match lookup failed")
                    _state.update {
                        it.copy(
                            isLoadingMatches = false,
                            matches = emptyList(),
                            matchesError = err.message ?: "Could not load nearby posts"
                        )
                    }
                }
        }
    }

    private data class MatchQuery(
        val species: com.fabriziogo.epona.core.domain.model.Species,
        val breed: String?,
        val color: String?,
        val lookingFor: AlertType
    )

    private fun autoDetectLocation() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLocation = true, locationError = null) }
            locationRepo.getCurrentLocation()
                .onSuccess { loc ->
                    _state.update { it.copy(location = loc, isLoadingLocation = false) }
                    locationRepo.reverseGeocode(loc.latitude, loc.longitude)
                        .onSuccess { addr -> _state.update { it.copy(address = addr) } }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoadingLocation = false,
                            locationError = "Could not get location"
                        )
                    }
                }
        }
    }

    private fun setLocation(location: Location) {
        _state.update {
            it.copy(
                location = location,
                address = location.address ?: it.address,
                locationError = null
            )
        }
        if (location.address == null) {
            viewModelScope.launch {
                locationRepo.reverseGeocode(location.latitude, location.longitude)
                    .onSuccess { addr -> _state.update { it.copy(address = addr) } }
            }
        }
    }

    /**
     * Decoding happens here rather than in the picker: it is disk work, and a scope
     * tied to the composition would drop the photos on a rotation mid-decode.
     * Same pair as AddPetViewModel / ReportSightingViewModel.
     */
    private fun addFoundPhotos(uris: List<Uri>) {
        val free = MAX_PHOTOS_PER_ENTITY - _state.value.foundPet.photos.size
        if (free <= 0 || uris.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(foundPet = it.foundPet.copy(isProcessingPhotos = true)) }
            val wanted = uris.take(free)
            val added = wanted.mapNotNull { imageProcessor.process(it).getOrNull() }
            _state.update {
                it.copy(
                    foundPet = it.foundPet.copy(
                        photos = it.foundPet.photos + added.map { image -> PhotoItem.Local(image) },
                        isProcessingPhotos = false
                    ),
                    // One unreadable pick should not sink the rest of the selection.
                    error = if (added.size < wanted.size) PHOTO_ERROR else it.error
                )
            }
        }
    }

    private fun removeFoundPhoto(photo: PhotoItem) {
        if (photo is PhotoItem.Local) imageProcessor.delete(listOf(photo.image.uri))
        _state.update { s ->
            s.copy(foundPet = s.foundPet.copy(photos = s.foundPet.photos.filterNot { it.key == photo.key }))
        }
    }

    private fun publish() {
        val s = _state.value

        // A second tap while the first publish is still in flight would create a
        // duplicate alert, so the in-flight publish owns the button until it
        // settles. AddPetViewModel and ReportSightingViewModel both have this
        // guard; publish() did not while canProceed was unconditionally true.
        if (s.isSubmitting) return

        val loc = s.location ?: run {
            _state.update { it.copy(error = "Please set the last seen location") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

            val result = if (s.alertType == AlertType.LOST) {
                val pet = s.selectedPet ?: run {
                    _state.update { it.copy(isSubmitting = false, error = "Please select a pet") }
                    return@launch
                }
                val alert = Alert(
                    petId = pet.id,
                    type = s.alertType,
                    lastSeenLocation = loc,
                    lastSeenAddress = s.address.ifBlank { null },
                    description = s.description.ifBlank { null },
                    reward = s.reward.toDoubleOrNull(),
                    contactPhone = s.contactPhone.ifBlank { null }
                )
                createAlert(alert)
            } else {
                val form = s.foundPet
                val pet = Pet(
                    species = form.species,
                    breed = form.breed.takeIf { it.isNotBlank() },
                    color = form.color.takeIf { it.isNotBlank() },
                    size = form.size,
                    description = form.description.takeIf { it.isNotBlank() },
                    // Local URIs. CreateFoundAlertUseCase uploads them and
                    // substitutes the public URLs before the RPC.
                    photoUrls = form.photos.map { it.model }
                )
                val alert = Alert(
                    type = AlertType.FOUND,
                    foundCustody = s.custody,
                    lastSeenLocation = loc,
                    lastSeenAddress = s.address.ifBlank { null },
                    description = s.description.ifBlank { null },
                    contactPhone = s.contactPhone.ifBlank { null }
                )
                createFoundAlert(pet, alert)
            }

            result
                .onSuccess { created ->
                    // Uploaded and persisted, so the cache copies are dead weight.
                    // Only on success: a failed publish keeps them for the retry.
                    if (s.alertType == AlertType.FOUND) {
                        imageProcessor.delete(s.foundPet.photos.localUris())
                    }
                    _state.update { it.copy(isSubmitting = false, isSuccess = true) }
                    _navEvents.send(CreateAlertNavEvent.NavigateToSuccess(created.id))
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            error = err.message ?: "Failed to publish alert"
                        )
                    }
                }
        }
    }

    private fun emitNav(event: CreateAlertNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }

    private companion object {
        const val MATCHES_STEP = 4
        const val PHOTO_ERROR = "Some photos could not be added"
    }
}
