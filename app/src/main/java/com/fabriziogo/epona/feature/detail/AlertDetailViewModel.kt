package com.fabriziogo.epona.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.acceptsSightings
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.usecase.alert.DeleteAlertUseCase
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
    private val deleteAlert: DeleteAlertUseCase,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val alertId: String = checkNotNull(savedStateHandle[ALERT_ID_ARG])

    private val _state = MutableStateFlow(AlertDetailUiState())
    val state: StateFlow<AlertDetailUiState> = _state.asStateFlow()

    private val _navEvents = Channel<DetailNavEvent>(Channel.BUFFERED)
    val navEvents = _navEvents.receiveAsFlow()

    /** Resolving and retrying both reload the detail; the trail starts only once. */
    private var sightingsStarted = false

    /**
     * Sightings are fetched from [loadAlertDetail]'s success rather than here,
     * because whether this alert accepts any is not known until the detail
     * arrives — a found pet the finder took home cannot be spotted by anyone.
     * Nothing is lost by waiting: the whole screen is a spinner until then.
     */
    init {
        loadAlertDetail()
    }

    fun onEvent(event: AlertDetailEvent) {
        when (event) {
            AlertDetailEvent.BackClicked ->
                emitNav(DetailNavEvent.NavigateBack)

            AlertDetailEvent.ShareClicked -> shareAlert()

            AlertDetailEvent.ContactOwnerClicked -> contactOwner()

            AlertDetailEvent.ReportSightingClicked -> reportSighting()

            AlertDetailEvent.ViewOnMapClicked -> viewOnMap()

            AlertDetailEvent.ResolveClicked ->
                _state.update { it.copy(showResolveDialog = true) }

            AlertDetailEvent.ResolveConfirmed -> resolveCurrentAlert()

            AlertDetailEvent.ResolveDismissed ->
                _state.update { it.copy(showResolveDialog = false) }

            AlertDetailEvent.DeleteClicked ->
                _state.update { it.copy(showDeleteDialog = true) }

            AlertDetailEvent.DeleteConfirmed -> deleteCurrentAlert()

            AlertDetailEvent.DeleteDismissed ->
                _state.update { it.copy(showDeleteDialog = false) }

            // Only the detail: it is what decides whether there is a trail to load.
            AlertDetailEvent.RetryLoad -> loadAlertDetail()

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
                    if (detail.alert.acceptsSightings) startSightingsOnce()
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

    /**
     * The buttons are hidden when the finder has the pet, but a screen loaded
     * before the post changed can still send this. create_sighting refuses it
     * server-side too — this just avoids walking the user into that error.
     */
    private fun reportSighting() {
        val alert = _state.value.alertDetail?.alert ?: return
        if (!alert.acceptsSightings) return
        emitNav(DetailNavEvent.NavigateToReportSighting(alertId))
    }

    private fun startSightingsOnce() {
        if (sightingsStarted) return
        sightingsStarted = true
        loadSightings()
        observeRealtimeSightings()
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

    private fun deleteCurrentAlert() {
        viewModelScope.launch {
            _state.update { it.copy(isDeleting = true, showDeleteDialog = false) }

            deleteAlert(alertId)
                .onSuccess {
                    emitNav(DetailNavEvent.NavigateBack)
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isDeleting = false,
                            error = err.message ?: "Failed to delete alert"
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
        val text = "$typeLabel: ${pet.name.ifBlank { pet.species.value }} (${pet.breed ?: pet.species.value}) " +
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