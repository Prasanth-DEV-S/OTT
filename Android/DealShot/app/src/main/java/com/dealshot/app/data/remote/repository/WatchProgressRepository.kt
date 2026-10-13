
package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.WatchProgressApiService
import com.dealshot.app.data.remote.dto.SaveWatchProgressRequestDto
import com.dealshot.app.data.remote.dto.WatchProgressDto
import retrofit2.HttpException

interface WatchProgressRepository {
    suspend fun getProgress(videoId: String): WatchProgressDto

    suspend fun saveProgress(
        videoId: String,
        positionSeconds: Int,
        durationSeconds: Int
    ): WatchProgressDto
}

class WatchProgressRepositoryImpl(
    private val api: WatchProgressApiService
) : WatchProgressRepository {

    override suspend fun getProgress(
        videoId: String
    ): WatchProgressDto {
        val response = api.getWatchProgress(videoId)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: throw IllegalStateException("Unable to load watch progress.")

        if (!body.success) {
            throw IllegalStateException("Unable to load watch progress.")
        }

        return body.data
    }

    override suspend fun saveProgress(
        videoId: String,
        positionSeconds: Int,
        durationSeconds: Int
    ): WatchProgressDto {
        val response = api.saveWatchProgress(
            videoId = videoId,
            request = SaveWatchProgressRequestDto(
                positionSeconds = positionSeconds.coerceAtLeast(0),
                durationSeconds = durationSeconds.coerceAtLeast(0)
            )
        )

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: throw IllegalStateException("Unable to save watch progress.")

        if (!body.success) {
            throw IllegalStateException("Unable to save watch progress.")
        }

        return body.data
    }
}