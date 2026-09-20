package com.fabriziogo.epona.feature.alert.resolve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.alert.GetAlertDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResolveAlertViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAlertDetail: GetAlertDetailUseCase
) : ViewModel() {

    private val alertId: String = checkNotNull(savedStateHandle["alertId"])

    private val _state = MutableStateFlow(ResolveAlertUiState())
    val state: StateFlow<ResolveAlertUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getAlertDetail(alertId)
                .onSuccess { detail ->
                    _state.update { it.copy(isLoading = false, petName = detail.pet.name) }
                }
                .onFailure {
                    // Blank so the screen falls back to resolve_default_pet_name.
                    _state.update { it.copy(isLoading = false, petName = "") }
                }
        }
    }
}