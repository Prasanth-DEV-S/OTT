package com.dealshot.app.presentation.shared.like


import com.dealshot.app.data.remote.dto.VideoDto

sealed interface LikedVideosEffect {

    data class ShowMessage(
        val message: String
    ) : LikedVideosEffect
    data class NavigateBack(
        val message: String
    ) : LikedVideosEffect

    data class OpenPlayer(
        val video: VideoDto
    ) : LikedVideosEffect
}