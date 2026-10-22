package com.fabriziogo.epona.core.domain.usecase.block

import com.fabriziogo.epona.core.domain.model.BlockedUser
import com.fabriziogo.epona.core.domain.model.SignUpResult
import com.fabriziogo.epona.core.domain.model.User
import com.fabriziogo.epona.core.domain.repository.AuthRepository
import com.fabriziogo.epona.core.domain.repository.BlockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockUserUseCaseTest {

    @Test
    fun `blocks another user through the repository`() = runBlocking {
        val repo = FakeBlockRepository()
        val useCase = BlockUserUseCase(repo, FakeAuthRepository(currentUserId = "me"))

        val result = useCase("someone-else")

        assertTrue(result.isSuccess)
        assertEquals(listOf("someone-else"), repo.blocked)
    }

    @Test
    fun `refuses to block yourself without calling the repository`() = runBlocking {
        val repo = FakeBlockRepository()
        val useCase = BlockUserUseCase(repo, FakeAuthRepository(currentUserId = "me"))

        val result = useCase("me")

        assertTrue(result.isFailure)
        assertTrue(repo.blocked.isEmpty())
    }

    @Test
    fun `refuses a blank id without calling the repository`() = runBlocking {
        val repo = FakeBlockRepository()
        val useCase = BlockUserUseCase(repo, FakeAuthRepository(currentUserId = "me"))

        val result = useCase("  ")

        assertTrue(result.isFailure)
        assertTrue(repo.blocked.isEmpty())
    }

    private class FakeBlockRepository : BlockRepository {
        val blocked = mutableListOf<String>()

        override suspend fun blockUser(userId: String): Result<Unit> {
            blocked += userId
            return Result.success(Unit)
        }

        override suspend fun unblockUser(userId: String): Result<Unit> = Result.success(Unit)

        override suspend fun getBlockedUsers(): Result<List<BlockedUser>> =
            Result.success(emptyList())
    }

    private class FakeAuthRepository(override val currentUserId: String?) : AuthRepository {
        override val isAuthenticated: Boolean = true
        override suspend fun awaitUserId(): String? = currentUserId
        override fun observeAuthState(): Flow<Boolean> = error("unused")
        override suspend fun signInWithEmail(email: String, password: String): Result<User> =
            error("unused")
        override suspend fun signUpWithEmail(
            email: String,
            password: String,
            displayName: String,
            acceptedTermsVersion: String
        ): Result<SignUpResult> = error("unused")
        override suspend fun signInWithGoogle(idToken: String): Result<User> = error("unused")
        override suspend fun signOut(): Result<Unit> = error("unused")
        override suspend fun sendPasswordResetCode(email: String): Result<Unit> = error("unused")
        override suspend fun verifyPasswordResetCode(email: String, code: String): Result<Unit> =
            error("unused")
        override suspend fun updatePassword(newPassword: String): Result<Unit> = error("unused")
        override suspend fun deleteAccount(): Result<Unit> = error("unused")
    }
}
