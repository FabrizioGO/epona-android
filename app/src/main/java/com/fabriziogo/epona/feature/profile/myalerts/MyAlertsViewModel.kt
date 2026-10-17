package com.fabriziogo.epona.feature.profile.myalerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fabriziogo.epona.core.domain.usecase.adoption.GetMyAdoptionPostsUseCase
import com.fabriziogo.epona.core.domain.usecase.alert.GetMyAlertsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyAlertsViewModel @Inject constructor(
    private val getMyAlerts: GetMyAlertsUseCase,
    private val getMyAdoptionPosts: GetMyAdoptionPostsUseCase
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

            val (alertsResult, adoptionResult) = coroutineScope {
                val alerts = async { getMyAlerts() }
                val adoption = async { getMyAdoptionPosts() }
                alerts.await() to adoption.await()
            }

            // A failed adoption request must not hide the alerts the user came here for.
            val adoptionPosts = adoptionResult
                .onFailure { Timber.w(it, "Failed to load my adoption posts") }
                .getOrDefault(emptyList())

            alertsResult
                .onSuccess { alerts ->
                    _state.update {
                        it.copy(isLoading = false, alerts = alerts, adoptionPosts = adoptionPosts)
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            adoptionPosts = adoptionPosts,
                            error = err.message ?: "Failed to load alerts"
                        )
                    }
                }
        }
    }
}
