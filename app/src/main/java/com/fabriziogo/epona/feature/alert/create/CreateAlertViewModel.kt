package com.fabriziogo.epona.feature.alert.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.Alert
import com.fabriziogo.epona.core.domain.repository.LocationRepository
import com.fabriziogo.epona.core.domain.usecase.alert.CreateAlertUseCase
import com.fabriziogo.epona.core.domain.usecase.pet.GetMyPetsUseCase
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
    private val locationRepo: LocationRepository
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

            is CreateAlertEvent.AlertTypeSelected ->
                _state.update { it.copy(alertType = event.type) }

            is CreateAlertEvent.PetSelected ->
                _state.update { it.copy(selectedPet = event.pet) }
            CreateAlertEvent.AddNewPetClicked ->
                emitNav(CreateAlertNavEvent.NavigateToAddPet)

            is CreateAlertEvent.PhotoAdded -> addPhoto(event.uri)
            is CreateAlertEvent.PhotoRemoved -> removePhoto(event.index)
            CreateAlertEvent.UseCurrentLocation -> autoDetectLocation()
            CreateAlertEvent.LocationPermissionDenied -> _state.update {
                it.copy(
                    isLoadingLocation = false,
                    locationError = "Location permission denied — pick the location " +
                            "on the map, or enable it in Settings."
                )
            }
            is CreateAlertEvent.LocationPicked -> setLocation(event.location)
            is CreateAlertEvent.DescriptionChanged ->
                _state.update { it.copy(description = event.text) }

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
        }
    }

    private fun previousStep() {
        if (_state.value.currentStep > 1) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        } else {
            emitNav(CreateAlertNavEvent.NavigateBack)
        }
    }

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

    private fun setLocation(location: com.fabriziogo.epona.core.domain.model.Location) {
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

    private fun addPhoto(uri: String) {
        if (_state.value.photoUris.size >= 5) {
            _state.update { it.copy(error = "Maximum 5 photos allowed") }
            return
        }
        _state.update { it.copy(photoUris = it.photoUris + uri) }
    }

    private fun removePhoto(index: Int) {
        _state.update {
            it.copy(photoUris = it.photoUris.toMutableList().apply { removeAt(index) })
        }
    }

    private fun publish() {
        val s = _state.value
        val pet = s.selectedPet ?: run {
            _state.update { it.copy(error = "Please select a pet") }
            return
        }
        val loc = s.location ?: run {
            _state.update { it.copy(error = "Please set the last seen location") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, error = null) }

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
                .onSuccess { created ->
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
}