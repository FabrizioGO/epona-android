package com.fabriziogo.epona.core.domain.repository

import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val isAuthenticated: Boolean

    val currentUserId: String?

    fun observeAuthState(): Flow<Boolean>

    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<User>

    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<SignUpResult>

    suspend fun signInWithGoogle(idToken: String): Result<User>

    suspend fun signOut(): Result<Unit>
}