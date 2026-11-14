package com.dealshot.app.data.remote.repository.report

import kotlinx.serialization.Serializable

data class ReportReason(
    val id: String,
    val label: String
)

@Serializable
data class ReportReasonItemDto(
    val id: String,
    val label: String
)

@Serializable
data class ReportMasterDataDto(
    val reasons: List<ReportReasonItemDto> = emptyList()
)

@Serializable
data class ReportMasterDataResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: ReportMasterDataDto? = null
)

@Serializable
data class SubmitReportRequestDto(
    val videoId: String,
    val reason: String
)

@Serializable
data class SubmitReportResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: SubmitReportDataDto? = null
)

@Serializable
data class SubmitReportDataDto(
    val id: String = "",
    val userId: String = "",
    val videoId: String = "",
    val videoTitle: String? = null,
    val reason: String = "",
    val comment: String? = null,
    val description: String? = null,
    val status: String = "pending",
    val createdAt: String? = null,
    val updatedAt: String? = null
)
