package com.dealshot.app.data.remote.repository.report

enum class ReportReason(
    val label: String,
    val apiValue: String,          // value sent to the backend; change when the contract is final
    val commentRequired: Boolean = false
) {
    INAPPROPRIATE_CONTENT("Inappropriate content", "inappropriate_content"),
    VIOLENCE_HARMFUL("Violence or harmful content", "violence_harmful"),
    HATE_HARASSMENT("Hate speech or harassment", "hate_harassment"),
    COPYRIGHT("Copyright violation", "copyright_violation"),
    MISLEADING("Misleading information", "misleading_information"),
    SPAM_SCAM("Spam or scam", "spam_scam"),
    OTHER("Other", "other", commentRequired = true)
}

/** Request body. Field names match the suggested contract: videoId, reason, comment. */
data class ReportRequest(
    val videoId: String,
    val reason: String,
    val comment: String
)

/** isSimulated = true means nothing was sent to a server (mock only). */
data class ReportReceipt(val isSimulated: Boolean = false)

class ReportUnavailableException : Exception("Report service is not connected yet")