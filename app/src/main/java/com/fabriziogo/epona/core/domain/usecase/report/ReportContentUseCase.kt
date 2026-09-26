package com.fabriziogo.epona.core.domain.usecase.report

import com.fabriziogo.epona.core.domain.model.ReportReason
import com.fabriziogo.epona.core.domain.repository.ReportRepository
import javax.inject.Inject

class ReportContentUseCase @Inject constructor(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(
        alertId: String,
        reason: ReportReason,
        details: String? = null
    ): Result<Unit> {
        val trimmedDetails = details?.trim()?.takeIf { it.isNotEmpty() }
        if (trimmedDetails != null && trimmedDetails.length > MAX_DETAILS_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Details must be $MAX_DETAILS_LENGTH characters or fewer")
            )
        }
        return reportRepository.reportContent(alertId, reason, trimmedDetails)
    }

    private companion object {
        // Matches content_reports_details_length in
        // supabase/migrations/20260926000000_content_reports.sql.
        const val MAX_DETAILS_LENGTH = 500
    }
}
