package com.dealshot.app.presentation.shared.like

import com.dealshot.app.data.remote.dto.VideoDto

data class LikedVideosState(
    val videos: List<VideoDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val likingIds: Set<String> = emptySet()
)