package com.fabriziogo.epona.core.domain.usecase.auth

import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteAccountUseCaseTest {

    @Test
    fun `delegates to the repository and returns its success`() = runBlocking {
        val repo = FakeAuthRepository(result = Result.success(Unit))
        val useCase = DeleteAccountUseCase(repo)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertTrue(repo.deleteAccountCalled)
    }

    @Test
    fun `propagates a repository failure rather than swallowing it`() = runBlocking {
        val error = IllegalStateException("network down")
        val repo = FakeAuthRepository(result = Result.failure(error))
        val useCase = DeleteAccountUseCase(repo)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    private class FakeAuthRepository(
        private val result: Result<Unit>
    ) : AuthRepository {
        var deleteAccountCalled = false

        override val isAuthenticated: Boolean = true
        override val currentUserId: String? = "user-1"

        override suspend fun awaitUserId(): String? = currentUserId
        override fun observeAuthState(): Flow<Boolean> = flowOf(true)

        override suspend fun signInWithEmail(email: String, password: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override suspend fun signUpWithEmail(
            email: String,
            password: String,
            displayName: String,
            acceptedTermsVersion: String
        ): Result<SignUpResult> = Result.failure(UnsupportedOperationException())

        override suspend fun signInWithGoogle(idToken: String): Result<User> =
            Result.failure(UnsupportedOperationException())

        override suspend fun signOut(): Result<Unit> = Result.success(Unit)

        override suspend fun sendPasswordResetCode(email: String): Result<Unit> =
            Result.success(Unit)

        override suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit> =
            Result.success(Unit)

        override suspend fun updatePassword(newPassword: String): Result<Unit> =
            Result.success(Unit)

        override suspend fun deleteAccount(): Result<Unit> {
            deleteAccountCalled = true
            return result
        }
    }
}
