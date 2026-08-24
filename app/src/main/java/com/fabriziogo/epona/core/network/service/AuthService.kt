package com.fabriziogo.epona.core.network.service

import com.fabriziogo.epona.core.network.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val supabase: SupabaseProvider
) {
    private val auth get() = supabase.client.auth

    val isAuthenticated: Boolean
        get() = auth.currentSessionOrNull() != null

    fun observeAuthState(): Flow<Boolean> =
        auth.sessionStatus.map { status ->
            status is SessionStatus.Authenticated
        }

    suspend fun signInWithEmail(email: String, password: String): UserInfo {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        return auth.retrieveUserForCurrentSession()
    }

    /**
     * Creates a new account.
     *
     * Supabase only returns a session when email confirmation is disabled for the
     * project. When confirmation is enabled (the default) the account is created
     * unconfirmed and no session exists yet, so callers must not assume the user
     * is signed in afterwards.
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): SignUpOutcome {
        val signedUpUser = auth.signUpWith(Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject {
                put("display_name", displayName)
            }
        }
        val session = auth.currentSessionOrNull()
            ?: return SignUpOutcome.ConfirmationRequired
        val userId = session.user?.id
            ?: signedUpUser?.id
            ?: error("Sign-up returned a session without a user")
        return SignUpOutcome.SignedIn(userId)
    }

    suspend fun signInWithGoogle(idToken: String): UserInfo {
        /*auth.signInWith(Google) {
            this.idToken = idToken
        }*/
        return auth.retrieveUserForCurrentSession()
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? =
        auth.currentUserOrNull()?.id

    suspend fun refreshSession() {
        auth.refreshCurrentSession()
    }
}

/**
 * Whether a sign-up produced a usable session or is waiting on email confirmation.
 */
sealed interface SignUpOutcome {

    /** A session was created; [userId] identifies the signed-in user. */
    data class SignedIn(val userId: String) : SignUpOutcome

    /** No session was created; the user must open the confirmation email first. */
    data object ConfirmationRequired : SignUpOutcome
}