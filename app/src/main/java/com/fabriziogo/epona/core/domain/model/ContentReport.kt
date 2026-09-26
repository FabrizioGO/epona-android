package com.fabriziogo.epona.core.domain.model

import com.fabriziogo.epona.R

/**
 * Why an alert was flagged. `value` is the wire string stored in
 * `content_reports.reason` -- see supabase/migrations/20260926000000_content_reports.sql.
 */
enum class ReportReason(val value: String, val label: Int) {
    SCAM_OR_FRAUD("scam_or_fraud", R.string.report_reason_scam_or_fraud),
    FALSE_INFORMATION("false_information", R.string.report_reason_false_information),
    INAPPROPRIATE("inappropriate", R.string.report_reason_inappropriate),
    HARASSMENT("harassment", R.string.report_reason_harassment),
    ANIMAL_CRUELTY("animal_cruelty", R.string.report_reason_animal_cruelty),
    SPAM("spam", R.string.report_reason_spam),
    OTHER("other", R.string.report_reason_other)
}
