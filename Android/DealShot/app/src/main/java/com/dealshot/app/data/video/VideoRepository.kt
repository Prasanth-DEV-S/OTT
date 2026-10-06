package com.dealshot.app.data.video

import androidx.media3.common.util.UnstableApi


@UnstableApi
class VideoRepository {

    private val videos =
        listOf(
            Video(
            id ="1",
                title = "The Color Journey",
                description = "A one-minute stream demo",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/kalyan_jewelry.mp4",
                "premium"
            ),

            Video(
                id = "2",
                title = "The Color Journey",
                description = "A one-minute stream demo",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/jewelry.mp4",
                "premium"
            ),

            Video(
                id = "3",
                title = "Chuttamalle",
                description = "CloudFront streaming demo",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/gold-video.mp4",
                "sports"
            ),

            Video(
                id = "4",
                title = "Video 4",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/Hightech farming.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Why the Future of Farming is in Cities - The Big Money in Vertical Farming.mp4",
                "home"
            ),

            Video(
                id = "5",
                title = "Video 5",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/MIG-29s.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/When MiG-29s Ambushed F-15 Eagles.mp4",
                "home"
            ),

            Video(
                id = "6",
                title = "Video 6",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/Monaco.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Monaco  Where the Rich Come to Live 4k video_1440p.mp4",

                "home"
            ),

            Video(
                id = "7",
                title = "Video 7",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/Monaco.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Monaco  Where the Rich Come to Live 4k video_1440p.mp4",
                "home"
            ),

            Video(
                id =" 8",
                title = "Video 8",
                description = "StreamBox demo video",
                duration = "1 min",
                thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/GOLD.png",
                url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Inside the 24K Gold Factory From Raw Ore to Pure Gold Bars.mp4",

                "movies"
            )
        )


    fun getVideosByCategory(
        videos: List<Video>,
        category: String
    ): List<Video> {
        return videos.filter {
            it.category.equals(category, ignoreCase = true)
        }
    }

    companion object {
        private val videos =
            listOf(
                Video(
                    id = "1",
                    title = "The Color Journey",
                    description = "A one-minute stream demo",
                    duration = "1 min",
                    thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                    url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/kalyan_jewelry.mp4",
                    "premium"
                ),

                Video(
                    id = "2",
                    title = "The Color Journey",
                    description = "A one-minute stream demo",
                    duration = "1 min",
                    thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                    url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/jewelry.mp4",
                    "premium"
                ),

                Video(
                    id = "3",
                    title = "Chuttamalle",
                    description = "CloudFront streaming demo",
                    duration = "1 min",
                    thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/jewelry_thumbnail.jpg",
                    url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/gold-video.mp4",
                    "sports"
                ),

                Video(
                    id = "4",
                    title = "Video 4",
                    description = "StreamBox demo video",
                    duration = "1 min",
                    thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/Hightech farming.png",
                    url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Why the Future of Farming is in Cities - The Big Money in Vertical Farming.mp4",
                    "home"
                ),

                Video(
                    id = "5",
                    title = "Video 5",
                    description = "StreamBox demo video",
                    duration = "1 min",
                    thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/MIG-29s.png",
                    url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/When MiG-29s Ambushed F-15 Eagles.mp4",
                    "home"
                ),

                Video(
                    id = "6",
                    title = "Video 6",
                    description = "StreamBox demo video",
                    duration = "1 min",
                    thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/Monaco.png",
                    url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Monaco  Where the Rich Come to Live 4k video_1440p.mp4",

                    "home"
                ),

                Video(
                    id = "7",
                    title = "Video 7",
                    description = "StreamBox demo video",
                    duration = "1 min",
                    thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/Monaco.png",
                    url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Monaco  Where the Rich Come to Live 4k video_1440p.mp4",
                    "home"
                ),

                Video(
                    id = "8",
                    title = "Video 8",
                    description = "StreamBox demo video",
                    duration = "1 min",
                    thumbnail = "https://dealshot-video-bucket.s3.ap-south-2.amazonaws.com/thumbnails/GOLD.png",
                    url = "https://d2mt5pdqyed9m3.cloudfront.net/videos/Inside the 24K Gold Factory From Raw Ore to Pure Gold Bars.mp4",

                    "movies"
                )
            )


        fun getVideos(): List<Video> {
            return videos
        }
    }
}

