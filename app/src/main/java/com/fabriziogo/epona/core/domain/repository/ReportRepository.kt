package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.ReportReason

interface ReportRepository {

    /**
     * Flags the alert [alertId] for moderation. Reporting the same alert twice
     * succeeds both times from the caller's point of view -- see the
     * implementation.
     */
    suspend fun reportContent(
        alertId: String,
        reason: ReportReason,
        details: String?
    ): Result<Unit>
}
