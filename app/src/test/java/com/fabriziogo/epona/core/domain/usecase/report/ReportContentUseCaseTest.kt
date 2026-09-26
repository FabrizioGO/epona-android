package com.fabriziogo.epona.core.domain.usecase.report

import com.fabriziogo.epona.core.domain.model.ReportReason
import com.fabriziogo.epona.core.domain.repository.ReportRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportContentUseCaseTest {

    @Test
    fun `trims details and drops a blank one to null`() = runBlocking {
        val repo = FakeReportRepository()
        val useCase = ReportContentUseCase(repo)

        useCase("alert-1", ReportReason.SPAM, "  looks fake  ")
        assertEquals("looks fake", repo.lastDetails)

        useCase("alert-1", ReportReason.SPAM, "   ")
        assertNull(repo.lastDetails)
    }

    @Test
    fun `rejects details over the 500 character limit without calling the repository`() = runBlocking {
        val repo = FakeReportRepository()
        val useCase = ReportContentUseCase(repo)

        val tooLong = "x".repeat(501)
        val result = useCase("alert-1", ReportReason.OTHER, tooLong)

        assertTrue(result.isFailure)
        assertEquals(0, repo.callCount)
    }

    @Test
    fun `allows details right at the limit`() = runBlocking {
        val repo = FakeReportRepository()
        val useCase = ReportContentUseCase(repo)

        val exactly500 = "x".repeat(500)
        val result = useCase("alert-1", ReportReason.OTHER, exactly500)

        assertTrue(result.isSuccess)
        assertEquals(1, repo.callCount)
    }

    private class FakeReportRepository : ReportRepository {
        var callCount = 0
        var lastDetails: String? = null

        override suspend fun reportContent(
            alertId: String,
            reason: ReportReason,
            details: String?
        ): Result<Unit> {
            callCount++
            lastDetails = details
            return Result.success(Unit)
        }
    }
}
