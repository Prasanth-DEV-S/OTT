package com.dealshot.app.data.remote.repository.report

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import kotlin.coroutines.cancellation.CancellationException

interface ReportApiService {
    @GET("reports/master-data")
    suspend fun getMasterData(): Response<ReportMasterDataResponseDto>

    @POST("reports")
    suspend fun submitReport(@Body body: SubmitReportRequestDto): Response<SubmitReportResponseDto>

    @GET("reports/status/{videoId}")
    suspend fun getReportStatus(@Path("videoId") videoId: String): Response<ReportStatusResponseDto>
}

interface ReportRepository {
    suspend fun getMasterData(): Result<List<ReportReason>>
    suspend fun submitReport(request: SubmitReportRequestDto): Result<SubmitReportResponseDto>
    suspend fun getReportStatus(videoId: String): Result<Boolean>
}

class ReportRepositoryImpl(
    private val api: ReportApiService
) : ReportRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getMasterData(): Result<List<ReportReason>> {
        return try {
            val response = api.getMasterData()
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                val reasonsList = body.data?.reasons.orEmpty().map { dto ->
                    ReportReason(
                        id = dto.id,
                        label = dto.label
                    )
                }
                Result.success(reasonsList)
            } else {
                val msg = parseErrorMessage(response)
                    ?: body?.message?.ifBlank { null }
                    ?: "Failed to load report reasons (${response.code()})"
                Result.failure(Exception(msg))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitReport(request: SubmitReportRequestDto): Result<SubmitReportResponseDto> {
        return try {
            val response = api.submitReport(request)
            val body = response.body()

            if ((response.isSuccessful || response.code() == 201) && body != null && body.success) {
                Result.success(body)
            } else {
                val msg = parseErrorMessage(response)
                    ?: body?.message?.ifBlank { null }
                    ?: "Failed to submit video report (${response.code()})"
                Result.failure(Exception(msg))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReportStatus(videoId: String): Result<Boolean> {
        return try {
            val response = api.getReportStatus(videoId)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                Result.success(body.data?.myReportStatus ?: false)
            } else {
                Result.success(false)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            Result.success(false)
        }
    }

    private fun parseErrorMessage(response: Response<*>): String? {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val element = json.parseToJsonElement(errorBody)
                element.jsonObject["message"]?.jsonPrimitive?.contentOrNull
                    ?: element.jsonObject["error"]?.jsonPrimitive?.contentOrNull
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
