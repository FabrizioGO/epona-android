package com.fabriziogo.epona.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A plain table insert (not an RPC), so field names are the column names -- see
 * supabase/migrations/20260926000000_content_reports.sql. reporter_id, id,
 * status and created_at are all filled server-side by column DEFAULTs and are
 * deliberately absent here: the RLS grant only gives `authenticated` INSERT on
 * the three columns below.
 */
@Serializable
data class ContentReportInsertParams(
    @SerialName("alert_id")
    val alertId: String,
    val reason: String,
    val details: String? = null
)
