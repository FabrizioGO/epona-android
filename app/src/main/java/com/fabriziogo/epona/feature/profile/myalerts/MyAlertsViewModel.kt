package com.fabriziogo.epona.feature.profile.myalerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.alert.GetMyAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyAlertsViewModel @Inject constructor(
    private val getMyAlerts: GetMyAlertsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyAlertsUiState())
    val state: StateFlow<MyAlertsUiState> = _state.asStateFlow()

    init { load() }

    fun onRefresh() = load()

    fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getMyAlerts()
                .onSuccess { alerts ->
                    _state.update { it.copy(isLoading = false, alerts = alerts) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.message ?: "Failed to load alerts")
                    }
                }
        }
    }
}
