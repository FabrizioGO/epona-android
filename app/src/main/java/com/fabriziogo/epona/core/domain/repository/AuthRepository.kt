package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val isAuthenticated: Boolean

    val currentUserId: String?

    /**
     * The signed-in user id, waiting out session restoration first.
     *
     * [currentUserId] reports whatever the session holds at that instant, and at cold
     * start that is null until the stored session has been loaded. Anything that gates a
     * network read on the user id has to wait, or it reports a signed-in user as
     * unauthenticated for the first moments after launch.
     */
    suspend fun awaitUserId(): String?

    fun observeAuthState(): Flow<Boolean>

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<User>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        acceptedTermsVersion: String
    ): Result<SignUpResult>

    suspend fun signInWithGoogle(idToken: String): Result<User>

    suspend fun signOut(): Result<Unit>

    /**
     * Deletes the signed-in user's account, every row and photo it owns on the
     * server, and the local Room cache. Irreversible.
     */
    suspend fun deleteAccount(): Result<Unit>
}