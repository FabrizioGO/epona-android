package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.model.LegalTerms
import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The rule this use case owns: no acceptance, no account. Everything else
 * (email/password shape) is [validateEmail]/[validatePassword], covered where
 * those live.
 */
class SignUpUseCaseTest {

    @Test
    fun `fails without accepting the terms`() = runBlocking {
        val repo = FakeAuthRepository()
        val useCase = SignUpUseCase(repo)

        val result = useCase("a@b.com", "password1", "Ada Lovelace", acceptedTerms = false)

        assertTrue(result.isFailure)
        assertFalse(repo.signUpCalled)
    }

    @Test
    fun `passes the current terms version through on acceptance`() = runBlocking {
        val repo = FakeAuthRepository()
        val useCase = SignUpUseCase(repo)

        val result = useCase("a@b.com", "password1", "Ada Lovelace", acceptedTerms = true)

        assertTrue(result.isSuccess)
        assertEquals(LegalTerms.VERSION, repo.lastAcceptedTermsVersion)
    }

    @Test
    fun `still validates email, password and name when terms are accepted`() = runBlocking {
        val repo = FakeAuthRepository()
        val useCase = SignUpUseCase(repo)

        val result = useCase("", "123", "", acceptedTerms = true)

        assertTrue(result.isFailure)
        assertFalse(repo.signUpCalled)
    }

    private class FakeAuthRepository : AuthRepository {
        var signUpCalled = false
        var lastAcceptedTermsVersion: String? = null

        override val isAuthenticated: Boolean = false
        override val currentUserId: String? = null

        override suspend fun awaitUserId(): String? = null
        override fun observeAuthState(): Flow<Boolean> = flowOf(false)

        override suspend fun signInWithEmail(email: String, password: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override suspend fun signUpWithEmail(
            email: String,
            password: String,
            displayName: String,
            acceptedTermsVersion: String
        ): Result<SignUpResult> {
            signUpCalled = true
            lastAcceptedTermsVersion = acceptedTermsVersion
            return Result.success(SignUpResult.ConfirmationRequired(email))
        }

        override suspend fun signInWithGoogle(idToken: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override suspend fun signOut(): Result<Unit> = Result.success(Unit)

        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
    }
}
