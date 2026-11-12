package com.dealshot.app.presentation.shared.report

import com.dealshot.app.data.remote.repository.report.ReportReason

object ReportValidator {

    const val MAX_COMMENT_LENGTH = 500

    data class ValidationResult(
        val reasonError: String? = null,
        val commentError: String? = null
    ) {
        val isValid: Boolean get() = reasonError == null && commentError == null
    }

    fun validate(reason: ReportReason?, comment: String): ValidationResult {
        val reasonError = if (reason == null) "Please select a reason for your report." else null

        val commentError = when {
            comment.length > MAX_COMMENT_LENGTH ->
                "Comment can't be longer than $MAX_COMMENT_LENGTH characters."
            reason?.commentRequired == true && comment.isBlank() ->
                "Please tell us more when you select \"Other\"."
            else -> null
        }
        return ValidationResult(reasonError, commentError)
    }
}