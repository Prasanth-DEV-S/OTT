package com.dealshot.app.presentation.shared.report

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.repository.report.ReportReason
import com.dealshot.app.data.remote.repository.report.ReportRepository
import com.dealshot.app.data.remote.repository.report.ReportRequest
import com.dealshot.app.data.remote.repository.report.ReportUnavailableException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class ReportUiState(
    val selectedReason: ReportReason? = null,
    val comment: String = "",
    val reasonError: String? = null,
    val commentError: String? = null,
    val submitError: String? = null,
    val submitAttempted: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val isSimulatedSuccess: Boolean = false
)

class ReportViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ReportRepository,
    private val videoId: String,
    private val isLoggedIn: () -> Boolean
) : ViewModel() {

    // Reason + comment survive process death through SavedStateHandle.
    private val _uiState = MutableStateFlow(
        ReportUiState(
            selectedReason = savedStateHandle.get<String>(KEY_REASON)
                ?.let { saved -> ReportReason.values().firstOrNull { it.name == saved } },
            comment = savedStateHandle.get<String>(KEY_COMMENT).orEmpty()
        )
    )
    val uiState = _uiState.asStateFlow()

    fun onReasonSelected(reason: ReportReason) {
        if (_uiState.value.isSubmitting) return
        savedStateHandle[KEY_REASON] = reason.name
        _uiState.update { revalidate(it.copy(selectedReason = reason, submitError = null)) }
    }

    fun onCommentChanged(text: String) {
        if (_uiState.value.isSubmitting) return
        // Hard cap: typing or pasting can never exceed the limit.
        val limited = text.take(ReportValidator.MAX_COMMENT_LENGTH)
        savedStateHandle[KEY_COMMENT] = limited
        _uiState.update { revalidate(it.copy(comment = limited, submitError = null)) }
    }

    fun submit() {
        val current = _uiState.value

        // Duplicate-request guard: a second tap while one is running does nothing.
        if (current.isSubmitting || current.isSuccess) return

        if (!isLoggedIn()) {
            _uiState.update { it.copy(submitError = "Please log in to report this video.") }
            return
        }

        val validation = ReportValidator.validate(current.selectedReason, current.comment)
        val reason = current.selectedReason
        if (!validation.isValid || reason == null) {
            _uiState.update {
                it.copy(
                    submitAttempted = true,
                    reasonError = validation.reasonError,
                    commentError = validation.commentError,
                    submitError = null
                )
            }
            return
        }

        // Set synchronously on the main thread, before launching, so a second tap sees it.
        _uiState.update {
            it.copy(
                isSubmitting = true,
                submitAttempted = true,
                reasonError = null,
                commentError = null,
                submitError = null
            )
        }

        viewModelScope.launch {
            val request = ReportRequest(
                videoId = videoId,
                reason = reason.apiValue,
                comment = current.comment.trim()
            )

            val result = try {
                repository.submitReport(request)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }

            result.fold(
                onSuccess = { receipt ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            isSimulatedSuccess = receipt.isSimulated
                        )
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Report submission failed", error)   // technical detail stays in Logcat
                    // reason and comment stay in state, so nothing the user typed is lost
                    _uiState.update {
                        it.copy(isSubmitting = false, submitError = userMessageFor(error))
                    }
                }
            )
        }
    }

    private fun revalidate(state: ReportUiState): ReportUiState {
        if (!state.submitAttempted) return state
        val v = ReportValidator.validate(state.selectedReason, state.comment)
        return state.copy(reasonError = v.reasonError, commentError = v.commentError)
    }

    private fun userMessageFor(error: Throwable): String = when (error) {
        is ReportUnavailableException ->
            "Reporting isn't available right now. Please try again later."
        else -> AppErrorHandler.getUserMessage(error)
    }

    private companion object {
        const val TAG = "ReportViewModel"
        const val KEY_REASON = "report_reason"
        const val KEY_COMMENT = "report_comment"
    }
}