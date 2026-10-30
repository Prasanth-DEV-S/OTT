package com.dealshot.app.presentation.shared.like

import com.dealshot.app.data.remote.dto.VideoDto

sealed interface LikedVideosIntent {

    data object Load : LikedVideosIntent

    data object Retry : LikedVideosIntent

    data class LikeVideo(
        val videoId: String
    ) : LikedVideosIntent

    data class CheckLikeStatus(
        val videoId: String
    ) : LikedVideosIntent

    data object ClearError : LikedVideosIntent

    data class VideoClicked(
        val video: VideoDto
    ) : LikedVideosIntent

    data object ClearMessage : LikedVideosIntent
}