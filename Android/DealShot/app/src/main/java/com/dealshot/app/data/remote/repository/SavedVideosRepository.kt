package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.SavedVideosApiService
import com.dealshot.app.data.remote.dto.VideoDto
import retrofit2.HttpException

enum class SavedVideoCollection(val path: String) {
    MY_LIST("my-list"),
    WATCH_LATER("watch-later")
}

interface SavedVideosRepository {
    suspend fun getVideos(
        collection: SavedVideoCollection
    ): List<VideoDto>

    suspend fun addVideo(
        collection: SavedVideoCollection,
        videoId: String
    ): VideoDto?

    suspend fun removeVideo(
        collection: SavedVideoCollection,
        videoId: String
    ): String
}

class SavedVideosRepositoryImpl(
    private val api: SavedVideosApiService
) : SavedVideosRepository {

    override suspend fun getVideos(
        collection: SavedVideoCollection
    ): List<VideoDto> {
        if (collection == SavedVideoCollection.MY_LIST) {
            val response = api.getLikedVideos()

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val body = response.body()
                ?: throw IllegalStateException("Unable to load liked videos.")

            if (!body.success) {
                throw IllegalStateException("Unable to load liked videos.")
            }

            return body.videos
        } else {
            val response = api.getSavedVideos(collection.path)

            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val body = response.body()
                ?: throw IllegalStateException("Unable to load saved videos.")

            if (!body.success) {
                throw IllegalStateException("Unable to load saved videos.")
            }

            return body.data.map { item ->
                item.videoId
            }
        }
    }

    override suspend fun addVideo(
        collection: SavedVideoCollection,
        videoId: String
    ): VideoDto? {
        val response = if (collection == SavedVideoCollection.MY_LIST) {
            api.likeVideo(videoId)
        } else {
            api.addVideo(collection.path, videoId)
        }

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

    override suspend fun removeVideo(
        collection: SavedVideoCollection,
        videoId: String
    ): String {
        val response = if (collection == SavedVideoCollection.MY_LIST) {
            api.unlikeVideo(videoId)
        } else {
            api.removeVideo(collection.path, videoId)
        }

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