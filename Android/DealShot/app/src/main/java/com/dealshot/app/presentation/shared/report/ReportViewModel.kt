package com.dealshot.app.presentation.shared.report

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.remote.repository.report.ReportReason
import com.dealshot.app.data.remote.repository.report.ReportRepository
import com.dealshot.app.data.remote.repository.report.SubmitReportRequestDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReportUiState(
    val reasons: List<ReportReason> = emptyList(),
    val isLoadingReasons: Boolean = false,
    val reasonsError: String? = null,
    val selectedReason: ReportReason? = null,
    val reasonError: String? = null,
    val submitError: String? = null,
    val submitAttempted: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String = "Video report submitted successfully"
)

class ReportViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: ReportRepository,
    private val videoId: String,
    private val isLoggedIn: () -> Boolean
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadMasterData()
    }

    fun loadMasterData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingReasons = true, reasonsError = null) }

            val result = try {
                repository.getMasterData()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }

            result.fold(
                onSuccess = { list ->
                    val savedReasonId = savedStateHandle.get<String>(KEY_REASON)
                    val restoredReason = list.firstOrNull { it.id == savedReasonId }
                    _uiState.update { current ->
                        current.copy(
                            reasons = list,
                            selectedReason = current.selectedReason ?: restoredReason,
                            isLoadingReasons = false,
                            reasonsError = null
                        )
                    }
                },
                onFailure = { error ->
                    runCatching { Log.e(TAG, "Failed to load report master data", error) }
                    _uiState.update {
                        it.copy(
                            isLoadingReasons = false,
                            reasonsError = userMessageFor(error)
                        )
                    }
                }
            )
        }
    }

    fun onReasonSelected(reason: ReportReason) {
        if (_uiState.value.isSubmitting) return
        savedStateHandle[KEY_REASON] = reason.id
        _uiState.update { revalidate(it.copy(selectedReason = reason, submitError = null)) }
    }

    fun submit() {
        val current = _uiState.value

        // Duplicate-request guard: a second tap while one is running does nothing.
        if (current.isSubmitting || current.isSuccess) return

        if (!isLoggedIn()) {
            _uiState.update { it.copy(submitError = "Please log in to report this video.") }
            return
        }

        val validation = ReportValidator.validate(current.selectedReason)
        val reason = current.selectedReason
        if (!validation.isValid || reason == null) {
            _uiState.update {
                it.copy(
                    submitAttempted = true,
                    reasonError = validation.reasonError,
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
                submitError = null
            )
        }

        viewModelScope.launch {
            val request = SubmitReportRequestDto(
                videoId = videoId,
                reason = reason.id
            )

            val result = try {
                repository.submitReport(request)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }

            result.fold(
                onSuccess = { response ->
                    val msg = response.message.ifBlank { "Video report submitted successfully" }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isSuccess = true,
                            successMessage = msg
                        )
                    }
                },
                onFailure = { error ->
                    runCatching { Log.e(TAG, "Report submission failed", error) }
                    _uiState.update {
                        it.copy(isSubmitting = false, submitError = userMessageFor(error))
                    }
                }
            )
        }
    }

    private fun revalidate(state: ReportUiState): ReportUiState {
        if (!state.submitAttempted) return state
        val v = ReportValidator.validate(state.selectedReason)
        return state.copy(reasonError = v.reasonError)
    }

    private fun userMessageFor(error: Throwable): String {
        return AppErrorHandler.getUserMessage(error)
    }

    private companion object {
        const val TAG = "ReportViewModel"
        const val KEY_REASON = "report_reason"
    }
}
