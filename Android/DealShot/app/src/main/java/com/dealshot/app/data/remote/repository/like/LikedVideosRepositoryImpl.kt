package com.dealshot.app.data.remote.repository.like

import com.dealshot.app.data.remote.dto.like.LikeVideoRequestDto
import com.dealshot.app.data.remote.api.LikedVideosApiService
import com.dealshot.app.data.remote.dto.like.LikeMutationResponseDto
import retrofit2.HttpException

class LikedVideosRepositoryImpl(
    private val api: LikedVideosApiService
) : LikedVideosRepository {

    override suspend fun getLikedVideos() =
        api.getLikedVideos().let { response ->
            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val body = response.body()
                ?: error("Unable to load liked videos.")

            if (!body.success) {
                error("Unable to load liked videos.")
            }

            body.videos
        }

    override suspend fun likeVideo(
        videoId: String
    ): LikeMutationResponseDto {
        require(videoId.isNotBlank()) {
            "Video ID cannot be empty."
        }

        val response = try {
            val res = api.likeVideo(LikeVideoRequestDto(videoId = videoId))
            if (res.isSuccessful) res else api.likeVideoPath(videoId)
        } catch (e: Exception) {
            try {
                api.likeVideoPath(videoId)
            } catch (ex: Exception) {
                throw e
            }
        }

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: error("Unable to like video.")

        if (!body.success) {
            error("Unable to like video.")
        }

        return body
    }

    override suspend fun getVideoLikeStatus(
        videoId: String
    ): Boolean {
        val response = api.getVideoLikeStatus(videoId)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: error("Unable to load video like status.")

        if (!body.success) {
            error("Unable to load video like status.")
        }

        return body.data.myLikeStatus
    }
}
