package com.dealshot.app.data.remote.repository.report

import androidx.lifecycle.SavedStateHandle
import com.dealshot.app.presentation.shared.report.ReportViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReportFeatureTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun parseReportMasterDataResponse() {
        val jsonString = """
            {
              "success": true,
              "message": "Report master data fetched successfully",
              "data": {
                "reasons": [
                  { "id": "inappropriate_content", "label": "Inappropriate content" },
                  { "id": "violence_or_harmful_content", "label": "Violence or harmful content" },
                  { "id": "other", "label": "Other" }
                ]
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<ReportMasterDataResponseDto>(jsonString)
        assertTrue(response.success)
        assertNotNull(response.data)
        val data = response.data!!
        assertEquals(3, data.reasons.size)
        assertEquals("inappropriate_content", data.reasons[0].id)
        assertEquals("Inappropriate content", data.reasons[0].label)
    }

    @Test
    fun parseSubmitReportResponse() {
        val jsonString = """
            {
              "success": true,
              "message": "Video report submitted successfully",
              "data": {
                "id": "6af579b215c6205841b34c85",
                "userId": "6ae597ed7c61f1d1da15c233",
                "videoId": "6abbb384f582d90fbf73375b",
                "videoTitle": "Gold is Cash",
                "reason": "inappropriate_content",
                "comment": "This video contains inappropriate material.",
                "description": "This video contains inappropriate material.",
                "status": "pending",
                "createdAt": "2026-11-12T08:32:50.821Z",
                "updatedAt": "2026-11-12T08:32:50.821Z"
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<SubmitReportResponseDto>(jsonString)
        assertTrue(response.success)
        assertEquals("Video report submitted successfully", response.message)
        assertNotNull(response.data)
        val data = response.data!!
        assertEquals("6af579b215c6205841b34c85", data.id)
        assertEquals("6abbb384f582d90fbf73375b", data.videoId)
    }

    @Test
    fun viewModel_loadsMasterDataAndSubmitsReportSuccessfully() {
        val fakeRepository = FakeReportRepository()
        val viewModel = ReportViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = fakeRepository,
            videoId = "6abbb384f582d90fbf73375b",
            isLoggedIn = { true }
        )

        // Verify reasons loaded dynamically
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingReasons)
        assertEquals(2, state.reasons.size)
        assertEquals("Inappropriate content", state.reasons[0].label)

        // Select first reason
        viewModel.onReasonSelected(state.reasons[0])

        // Submit report
        viewModel.submit()

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isSuccess)
        assertEquals("Video report submitted successfully", finalState.successMessage)
        assertNotNull(fakeRepository.lastSubmittedRequest)
        assertEquals("6abbb384f582d90fbf73375b", fakeRepository.lastSubmittedRequest?.videoId)
        assertEquals("inappropriate_content", fakeRepository.lastSubmittedRequest?.reason)
    }

    @Test
    fun viewModel_handlesHttp409DuplicateReportError() {
        val fakeRepository = FakeReportRepository(shouldFailWith409 = true)
        val viewModel = ReportViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = fakeRepository,
            videoId = "6abbb384f582d90fbf73375b",
            isLoggedIn = { true }
        )

        val state = viewModel.uiState.value
        viewModel.onReasonSelected(state.reasons[0])
        viewModel.submit()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isSuccess)
        assertFalse(finalState.isSubmitting)
        assertEquals("You already have an unresolved report for this video", finalState.submitError)
    }

    private class FakeReportRepository(
        private val shouldFailWith409: Boolean = false
    ) : ReportRepository {
        var lastSubmittedRequest: SubmitReportRequestDto? = null

        override suspend fun getMasterData(): Result<List<ReportReason>> {
            return Result.success(
                listOf(
                    ReportReason(id = "inappropriate_content", label = "Inappropriate content"),
                    ReportReason(id = "other", label = "Other")
                )
            )
        }

        override suspend fun submitReport(request: SubmitReportRequestDto): Result<SubmitReportResponseDto> {
            lastSubmittedRequest = request
            return if (shouldFailWith409) {
                Result.failure(Exception("You already have an unresolved report for this video"))
            } else {
                Result.success(
                    SubmitReportResponseDto(
                        success = true,
                        message = "Video report submitted successfully",
                        data = SubmitReportDataDto(
                            id = "report_123",
                            videoId = request.videoId,
                            reason = request.reason
                        )
                    )
                )
            }
        }
    }
}
