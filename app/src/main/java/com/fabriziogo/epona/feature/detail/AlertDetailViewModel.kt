package com.fabriziogo.epona.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.usecase.alert.GetAlertDetailUseCase
import com.fabriziogo.epona.core.domain.usecase.alert.ResolveAlertUseCase
import com.fabriziogo.epona.core.domain.usecase.sighting.GetSightingTrailUseCase
import com.fabriziogo.epona.core.domain.usecase.sighting.ObserveSightingsUseCase
import com.fabriziogo.epona.feature.detail.navigation.ALERT_ID_ARG
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
class AlertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlertDetail: GetAlertDetailUseCase,
    private val getSightingTrail: GetSightingTrailUseCase,
    private val observeSightings: ObserveSightingsUseCase,
    private val resolveAlert: ResolveAlertUseCase,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val alertId: String = checkNotNull(savedStateHandle[ALERT_ID_ARG])

    private val _state = MutableStateFlow(AlertDetailUiState())
    val state: StateFlow<AlertDetailUiState> = _state.asStateFlow()

    private val _navEvents = Channel<DetailNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    init {
        loadAlertDetail()
        loadSightings()
        observeRealtimeSightings()
    }

    fun onEvent(event: AlertDetailEvent) {
        when (event) {
            AlertDetailEvent.BackClicked ->
                emitNav(DetailNavEvent.NavigateBack)

            AlertDetailEvent.ShareClicked -> shareAlert()

            AlertDetailEvent.ContactOwnerClicked -> contactOwner()

            AlertDetailEvent.ReportSightingClicked ->
                emitNav(DetailNavEvent.NavigateToReportSighting(alertId))

            AlertDetailEvent.ViewOnMapClicked -> viewOnMap()

            AlertDetailEvent.ResolveClicked ->
                _state.update { it.copy(showResolveDialog = true) }

            AlertDetailEvent.ResolveConfirmed -> resolveCurrentAlert()

            AlertDetailEvent.ResolveDismissed ->
                _state.update { it.copy(showResolveDialog = false) }

            AlertDetailEvent.RetryLoad -> {
                loadAlertDetail()
                loadSightings()
            }

            AlertDetailEvent.ErrorDismissed ->
                _state.update { it.copy(error = null) }
        }
    }

    private fun loadAlertDetail() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getAlertDetail(alertId)
                .onSuccess { detail ->
                    val currentUserId = authRepo.currentUserId
                    _state.update {
                        it.copy(
                            isLoading = false,
                            alertDetail = detail,
                            isCurrentUserOwner = detail.alert.userId == currentUserId
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = err.message ?: "Failed to load alert"
                        )
                    }
                }
        }
    }

    private fun loadSightings() {
        viewModelScope.launch {
            _state.update { it.copy(isSightingsLoading = true) }

            getSightingTrail(alertId)
                .onSuccess { sightings ->
                    _state.update {
                        it.copy(
                            sightings = sightings,
                            isSightingsLoading = false
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isSightingsLoading = false) }
                }
        }
    }

    private fun observeRealtimeSightings() {
        viewModelScope.launch {
            observeSightings(alertId).collect { sightings ->
                _state.update { it.copy(sightings = sightings) }
            }
        }
    }

    private fun resolveCurrentAlert() {
        viewModelScope.launch {
            _state.update { it.copy(isResolving = true, showResolveDialog = false) }

            resolveAlert(alertId)
                .onSuccess {
                    // Reload to reflect resolved status
                    loadAlertDetail()
                    _state.update { it.copy(isResolving = false) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isResolving = false,
                            error = err.message ?: "Failed to resolve alert"
                        )
                    }
                }
        }
    }

    private fun shareAlert() {
        val detail = _state.value.alertDetail ?: return
        val pet = detail.pet
        val alert = detail.alert
        val typeLabel = if (alert.type.value == "lost") "LOST" else "FOUND"
        val text = "$typeLabel: ${pet.name} (${pet.breed ?: pet.species.value}) " +
            "near ${alert.lastSeenAddress ?: "unknown location"}. " +
            "Help bring them home! #Epona"
        val url = "https://epona.app/alert/${alert.id}"
        emitNav(DetailNavEvent.ShareAlert(text, url))
    }

    private fun contactOwner() {
        val phone = _state.value.alertDetail?.alert?.contactPhone
        if (phone != null) {
            emitNav(DetailNavEvent.DialPhone(phone))
        } else {
            _state.update { it.copy(error = "No contact number available") }
        }
    }

    private fun viewOnMap() {
        val loc = _state.value.alertDetail?.alert?.lastSeenLocation ?: return
        emitNav(DetailNavEvent.NavigateToMap(loc.latitude, loc.longitude))
    }

    private fun emitNav(event: DetailNavEvent) {
        viewModelScope.launch { _navEvents.send(event) }
    }
}