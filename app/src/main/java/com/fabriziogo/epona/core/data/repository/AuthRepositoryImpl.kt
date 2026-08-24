package com.fabriziogo.epona.core.data.repository

import com.fabriziogo.epona.core.data.mapper.toDomain
import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.network.service.AuthService
import com.fabriziogo.epona.core.network.service.SignUpOutcome
import com.fabriziogo.epona.core.network.service.UserService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val userService: UserService
) : AuthRepository {

    override val isAuthenticated: Boolean
        get() = authService.isAuthenticated

    override val currentUserId: String?
        get() = authService.getCurrentUserId()

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
        displayName: String
    ): Result<SignUpResult> = runCatching {
        when (val outcome = authService.signUpWithEmail(email, password, displayName)) {
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
    }
}