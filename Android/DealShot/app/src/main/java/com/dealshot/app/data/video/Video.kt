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
)