
package com.dealshot.app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WatchProgressDto(
    val positionSeconds: Int = 0,
    val durationSeconds: Int = 0,
    val completed: Boolean = false,
    val lastWatchedAt: String? = null,

    @SerialName("_id")
    val id: String? = null,

    val userId: String? = null
)

@Serializable
data class WatchProgressResponseDto(
    val success: Boolean = false,
    val data: WatchProgressDto
)

@Serializable
data class SaveWatchProgressResponseDto(
    val success: Boolean = false,
    val message: String = "",
    val data: WatchProgressDto
)

@Serializable
data class SaveWatchProgressRequestDto(
    val positionSeconds: Int,
    val durationSeconds: Int
)