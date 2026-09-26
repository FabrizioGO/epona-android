package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import com.fabriziogo.epona.core.network.dto.ContentReportInsertParams
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val table get() = supabase.client.postgrest["content_reports"]

    /**
     * No `select()` on this insert: the RLS policy on content_reports grants
     * INSERT only, not SELECT, so asking for the row back (Prefer:
     * return=representation) would fail even though the insert itself
     * succeeds. See ReportRepositoryImpl for how a duplicate report (23505,
     * from content_reports_reporter_alert_key) is handled as success.
     */
    suspend fun reportContent(params: ContentReportInsertParams) {
        table.insert(params)
    }
}
