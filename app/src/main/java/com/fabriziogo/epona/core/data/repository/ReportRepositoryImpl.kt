package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.domain.model.ReportReason
import com.fabriziogo.epona.core.domain.repository.ReportRepository
import com.fabriziogo.epona.core.network.dto.ContentReportInsertParams
import com.fabriziogo.epona.core.network.service.ReportService
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportService: ReportService
) : ReportRepository {

    override suspend fun reportContent(
        alertId: String,
        reason: ReportReason,
        details: String?
    ): Result<Unit> = runCatching {
        reportService.reportContent(
            ContentReportInsertParams(
                alertId = alertId,
                reason = reason.value,
                details = details?.trim()?.takeIf { it.isNotEmpty() }
            )
        )
    }.recoverCatching { err ->
        // 23505 = unique_violation on content_reports_reporter_alert_key: this
        // user already flagged this alert. From the caller's point of view
        // that is not a failure -- their report is already on file.
        if (err is PostgrestRestException && err.code == "23505") Unit else throw err
    }
}
