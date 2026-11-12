package com.dealshot.app.data.remote.repository.like

import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.remote.dto.like.LikeMutationResponseDto

interface LikedVideosRepository {

    suspend fun getLikedVideos(): List<VideoDto>

    suspend fun likeVideo(
        videoId: String
    ): LikeMutationResponseDto

    suspend fun getVideoLikeStatus(
        videoId: String
    ): Boolean

    suspend fun toggleVideoLike(
        videoId: String
    ): Result<LikeMutationResponseDto>
}
