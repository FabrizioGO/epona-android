package com.fabriziogo.epona.feature.sighting.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.model.SightingWithReporter
import com.fabriziogo.epona.core.domain.usecase.sighting.ObserveSightingsUseCase
import com.fabriziogo.epona.feature.sighting.navigation.SIGHTING_ALERT_ID_ARG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SightingListUiState(
    val isLoading: Boolean = true,
    val sightings: List<SightingWithReporter> = emptyList()
)

@HiltViewModel
class SightingListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSightings: ObserveSightingsUseCase
) : ViewModel() {

    private val alertId: String = checkNotNull(savedStateHandle[SIGHTING_ALERT_ID_ARG])

    private val _state = MutableStateFlow(SightingListUiState())
    val state: StateFlow<SightingListUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeSightings(alertId).collect { sightings ->
                _state.update {
                    it.copy(isLoading = false, sightings = sightings)
                }
            }
        }
    }
}