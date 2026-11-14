package com.dealshot.app.presentation.shared.report

import com.dealshot.app.data.remote.repository.report.ReportReason

object ReportValidator {

    data class ValidationResult(
        val reasonError: String? = null
    ) {
        val isValid: Boolean get() = reasonError == null
    }

    fun validate(reason: ReportReason?): ValidationResult {
        val reasonError = if (reason == null) "Please select a reason for your report." else null
        return ValidationResult(reasonError)
    }
}
