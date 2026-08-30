package com.fabriziogo.epona.feature.sighting.report

import com.fabriziogo.epona.core.domain.model.Location
import com.fabriziogo.epona.core.media.PhotoItem

data class ReportSightingUiState(
    val alertId: String = "",
    val petName: String = "",
    /** Local picks; ReportSightingUseCase uploads them when the report is sent. */
    val photos: List<PhotoItem> = emptyList(),
    val isProcessingPhotos: Boolean = false,
    val location: Location? = null,
    val address: String = "",
    val note: String = "",
    val isLoadingLocation: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val locationError: String? = null,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = location != null && !isSubmitting
}