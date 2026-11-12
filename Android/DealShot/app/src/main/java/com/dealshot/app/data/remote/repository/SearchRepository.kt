package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.VideoApiService
import com.dealshot.app.data.remote.dto.PaginationDto
import com.dealshot.app.data.remote.dto.VideoDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

data class SearchResult(
    val items: List<VideoDto>,
    val pagination: PaginationDto? = null
)

/**
 * Repository interface for video search functionality.
 */
interface SearchRepository {

    /**
     * Searches videos matching the query with support for pagination.
     *
     * @param query The search term entered by the user (2-100 characters).
     * @param page The 1-based page index for pagination.
     * @param limit The number of items per page.
     * @return Result wrapping SearchResult containing matching VideoDto items and pagination metadata.
     */
    suspend fun searchVideos(
        query: String,
        page: Int = 1,
        limit: Int = 20
    ): Result<SearchResult>
}

/**
 * Real API implementation of SearchRepository calling GET /search.
 */
class SearchRepositoryImpl(
    private val videoApiService: VideoApiService
) : SearchRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun searchVideos(
        query: String,
        page: Int,
        limit: Int
    ): Result<SearchResult> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank() || cleanQuery.length < 2) {
            return Result.success(SearchResult(emptyList(), null))
        }

        val clampedQuery = if (cleanQuery.length > 100) cleanQuery.take(100) else cleanQuery
        val clampedLimit = limit.coerceIn(1, 50)

        return try {
            val response = videoApiService.searchVideos(
                query = clampedQuery,
                page = page,
                limit = clampedLimit
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val videos = body.items.map { it.toVideoDto() }
                    Result.success(SearchResult(videos, body.pagination))
                } else {
                    Result.success(SearchResult(emptyList(), null))
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                    ?: when (response.code()) {
                        400 -> "Invalid search query (2–100 characters required)"
                        401 -> "Unauthorized. Please log in again."
                        else -> "Search failed (${response.code()})"
                    }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>): String? {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val element = json.parseToJsonElement(errorBody)
                if (element is JsonObject) {
                    element["message"]?.let {
                        if (it is JsonPrimitive) it.content else null
                    }
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
