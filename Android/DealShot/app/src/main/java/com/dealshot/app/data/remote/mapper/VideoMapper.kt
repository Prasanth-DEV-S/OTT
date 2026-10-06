package com.dealshot.app.data.remote.mapper

import com.dealshot.app.data.remote.dto.VideoDto
import com.dealshot.app.data.video.Video

fun VideoDto.toDomain(localId: String): Video {
    return Video(
        id = localId,
        title = title.orEmpty(),
        description = description.orEmpty(),
        duration = duration.orEmpty(),
        thumbnail = "0",
        url = videoUrl.orEmpty(),
        category = category.orEmpty(),

        apiId = id,
        thumbnailUrl = thumbnailUrl.orEmpty(),
        isPremium = isPremium,
        featured = featured
    )
}