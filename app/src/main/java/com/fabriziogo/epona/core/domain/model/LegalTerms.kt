package com.fabriziogo.epona.core.domain.model

/**
 * The version of the Terms of Service / Privacy Policy a user must accept to sign up.
 *
 * Bump [VERSION] whenever the documents published at `docs/terms.md` and
 * `docs/privacy.md` change materially, so new sign-ups record which revision they
 * agreed to. It is stored verbatim in the Supabase auth user's metadata as
 * `terms_version` — see [com.fabriziogo.epona.core.network.service.AuthService.signUpWithEmail].
 */
object LegalTerms {
    const val VERSION = "2026-09-26"
}
