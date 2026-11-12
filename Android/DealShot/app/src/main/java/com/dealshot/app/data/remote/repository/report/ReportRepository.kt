package com.dealshot.app.data.remote.repository.report

import kotlinx.coroutines.delay
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/** The single integration point for submitting a report. */
interface ReportRepository {
    suspend fun submitReport(request: ReportRequest): Result<ReportReceipt>
}

// ---------------- Real implementation (not wired yet) ----------------

object ReportEndpoints {
    const val REPORT_VIDEO = "reports"   // placeholder: change when the backend contract is final
}

interface ReportApiService {
    @POST(ReportEndpoints.REPORT_VIDEO)
    suspend fun reportVideo(@Body body: ReportRequest): Response<Unit>
}

class ReportRepositoryImpl(
    private val api: ReportApiService
) : ReportRepository {
    override suspend fun submitReport(request: ReportRequest): Result<ReportReceipt> {
        return try {
            val response = api.reportVideo(request)
            if (response.isSuccessful) {
                Result.success(ReportReceipt(isSimulated = false))
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ---------------- Mock for UI testing only ----------------

enum class MockReportOutcome {
    NOT_CONNECTED,       // fails with "service unavailable": never pretends to succeed
    SIMULATED_SUCCESS,   // success UI, clearly labelled as test mode (nothing is sent)
    SIMULATED_FAILURE    // generic failure, to test the error + Retry flow
}

class MockReportRepository(
    private val outcome: MockReportOutcome = MockReportOutcome.NOT_CONNECTED
) : ReportRepository {
    override suspend fun submitReport(request: ReportRequest): Result<ReportReceipt> {
        delay(1_200L) // lets you see the loading state
        return when (outcome) {
            MockReportOutcome.NOT_CONNECTED -> Result.failure(ReportUnavailableException())
            MockReportOutcome.SIMULATED_SUCCESS -> Result.success(ReportReceipt(isSimulated = true))
            MockReportOutcome.SIMULATED_FAILURE -> Result.failure(IOException("Simulated failure"))
        }
    }
}