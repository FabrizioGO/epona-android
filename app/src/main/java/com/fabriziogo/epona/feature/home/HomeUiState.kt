package com.fabriziogo.epona.feature.home

import com.fabriziogo.epona.core.domain.model.AlertType
import com.fabriziogo.epona.core.domain.model.AlertWithDetails
import com.fabriziogo.epona.core.domain.model.Location

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val alerts: List<AlertWithDetails> = emptyList(),
    val filteredAlerts: List<AlertWithDetails> = emptyList(),
    val selectedFilter: AlertFilter = AlertFilter.ALL,
    val userLocation: Location? = null,
    val alertRadiusKm: Int = 10,
    val totalActiveAlerts: Int = 0,
    val lostCount: Int = 0,
    val foundCount: Int = 0,
    val userName: String = "",
    val userAvatar: String? = null,
    val unreadNotificationCount: Int = 0,
    val error: String? = null
)

enum class AlertFilter(val label: String) {
    ALL("All"),
    LOST("Lost"),
    FOUND("Found");

    fun toAlertType(): AlertType? = when (this) {
        ALL -> null
        LOST -> AlertType.LOST
        FOUND -> AlertType.FOUND
    }
}