package com.fabriziogo.epona.core.domain.model

/**
 * Outcome of an email sign-up.
 *
 * Supabase only signs the new account in immediately when email confirmation is
 * disabled for the project. With confirmation enabled the account is created but
 * has no session until the emailed link is opened.
 */
sealed interface SignUpResult {

    /** The account was created and is already signed in. */
    data class SignedIn(val user: User) : SignUpResult

    /** The account was created but needs the confirmation link sent to [email]. */
    data class ConfirmationRequired(val email: String) : SignUpResult
}
