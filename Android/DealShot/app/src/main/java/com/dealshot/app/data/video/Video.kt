package com.dealshot.app.data.video

data class Video(
    val id: String,
    val title: String,
    val description: String,
    val duration: String,
    val thumbnail: String,
    val url: String,
    val category: String,

    // API fields
    val apiId: String = "",
    val thumbnailUrl: String = "",
    val isPremium: Boolean = false,
    val featured: Boolean = false,
    val promoVideoUrl: String = ""
) {
    constructor(
        id: String,
        title: String,
        description: String,
        duration: String,
        thumbnail: String?,
        url: String,
        category: String,
        apiId: String,
        thumbnailUrl: String?,
        isPremium: Boolean,
        promoVideoUrl: String,
        featured: Boolean
    ) : this(
        id = id,
        title = title,
        description = description,
        duration = duration,
        thumbnail = thumbnail.orEmpty(),
        url = url,
        category = category,
        apiId = apiId,
        thumbnailUrl = thumbnailUrl.orEmpty(),
        isPremium = isPremium,
        featured = featured,
        promoVideoUrl = promoVideoUrl
    )

    fun isHlsCategory(): Boolean {
        return category.trim().equals("International", ignoreCase = true)
    }

    val effectiveThumbnail: String
        get() = listOf(thumbnailUrl, thumbnail).firstOrNull { it.isNotBlank() }.orEmpty()
}

fun isHlsCategory(category: String?): Boolean {
    return category?.trim()?.equals("International", ignoreCase = true) == true
}
