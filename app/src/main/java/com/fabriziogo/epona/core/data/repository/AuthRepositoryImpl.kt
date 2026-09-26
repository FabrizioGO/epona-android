package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.database.EponaDatabase
import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.SignUpOutcome
import com.fabriziogo.epona.core.network.service.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val userService: UserService,
    private val database: EponaDatabase
) : AuthRepository {

    override val isAuthenticated: Boolean
        get() = authService.isAuthenticated

    override val currentUserId: String?
        get() = authService.getCurrentUserId()

    override suspend fun awaitUserId(): String? {
        authService.awaitReady()
        return authService.getCurrentUserId()
    }

    override fun observeAuthState(): Flow<Boolean> =
        authService.observeAuthState()

    override suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<User> = runCatching {
        authService.signInWithEmail(email, password)
        val userId = authService.getCurrentUserId()!!
        userService.getUser(userId).toDomain()
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        acceptedTermsVersion: String
    ): Result<SignUpResult> = runCatching {
        when (
            val outcome = authService.signUpWithEmail(
                email,
                password,
                displayName,
                acceptedTermsVersion
            )
        ) {
            is SignUpOutcome.ConfirmationRequired ->
                SignUpResult.ConfirmationRequired(email)
            is SignUpOutcome.SignedIn ->
                SignUpResult.SignedIn(userService.getUser(outcome.userId).toDomain())
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> =
        runCatching {
            authService.signInWithGoogle(idToken)
            val userId = authService.getCurrentUserId()!!
            userService.getUser(userId).toDomain()
        }

    override suspend fun signOut(): Result<Unit> = runCatching {
        authService.signOut()
        clearLocalCache()
    }

    override suspend fun deleteAccount(): Result<Unit> = runCatching {
        authService.deleteAccount()
        clearLocalCache()
    }

    /**
     * Wipes the Room cache after the server-side session is gone, so the next
     * sign-in on this device never shows a flash of a previous user's alerts,
     * pets or notifications before the network catches up.
     */
    private suspend fun clearLocalCache() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }
}