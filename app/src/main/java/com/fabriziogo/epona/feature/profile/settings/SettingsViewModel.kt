package com.fabriziogo.epona.feature.profile.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.auth.GetCurrentUserUseCase
import com.fabriziogo.epona.core.domain.usecase.user.UpdateAlertRadiusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val updateAlertRadius: UpdateAlertRadiusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init { loadSettings() }

    private fun loadSettings() {
        viewModelScope.launch {
            getCurrentUser().onSuccess { user ->
                _state.update {
                    it.copy(alertRadiusKm = user.alertRadiusKm)
                }
            }
        }
    }

    fun onRadiusChanged(km: Int) {
        _state.update { it.copy(alertRadiusKm = km) }
    }

    fun onRadiusSaved() {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            updateAlertRadius(_state.value.alertRadiusKm)
                .onSuccess {
                    _state.update { it.copy(isSaving = false) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isSaving = false, error = err.message)
                    }
                }
        }
    }

    fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }
}
