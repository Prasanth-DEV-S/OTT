package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.SavedVideosApiService
import com.dealshot.app.data.remote.dto.VideoDto
import retrofit2.HttpException

enum class SavedVideoCollection(val path: String) {
    MY_LIST("my-list"),
    LIKED("like")
}

interface SavedVideosRepository {
    suspend fun getVideos(collection: SavedVideoCollection = SavedVideoCollection.MY_LIST): List<VideoDto>

    suspend fun addVideo(videoId: String, collection: SavedVideoCollection = SavedVideoCollection.MY_LIST): VideoDto?

    suspend fun removeVideo(videoId: String, collection: SavedVideoCollection = SavedVideoCollection.MY_LIST): String
}

class SavedVideosRepositoryImpl(
    private val api: SavedVideosApiService
) : SavedVideosRepository {

    override suspend fun getVideos(collection: SavedVideoCollection): List<VideoDto> {
        val response = api.getSavedVideos(collection.path)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
        if (body == null || !body.success) {
            throw IllegalStateException("Unable to load videos.")
        }

        return body.data.map { it.videoId }
    }

    override suspend fun addVideo(videoId: String, collection: SavedVideoCollection): VideoDto? {
        val response = api.addVideo(collection.path, videoId)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: throw IllegalStateException("Unable to add video.")

        if (!body.success) {
            throw IllegalStateException("Unable to add video.")
        }

        return body.data
    }

    override suspend fun removeVideo(videoId: String, collection: SavedVideoCollection): String {
        val response = api.addVideo(collection.path, videoId)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val body = response.body()
            ?: throw IllegalStateException("Unable to remove video.")

        if (!body.success) {
            throw IllegalStateException("Unable to remove video.")
        }

        return body.message
    }
}
