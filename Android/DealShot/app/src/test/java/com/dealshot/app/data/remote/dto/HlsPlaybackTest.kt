package com.dealshot.app.data.remote.dto

import com.dealshot.app.data.video.Video
import com.dealshot.app.data.video.isHlsCategory
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HlsPlaybackTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        isLenient = true
    }

    @Test
    fun parseHlsQualitiesResponse() {
        val sampleJson = """
            {
              "success": true,
              "message": "HLS qualities fetched successfully",
              "data": {
                "videoId": "6abbb384f582d90fbf73376f",
                "streamingType": "hls",
                "autoAvailable": true,
                "qualities": [
                  {
                    "quality": "2160p",
                    "label": "4K",
                    "resolution": "4096x2160",
                    "bandwidthBps": 23874276,
                    "averageBandwidthBps": 13062829
                  },
                  {
                    "quality": "1080p",
                    "label": "1080p",
                    "resolution": "1920x1080",
                    "bandwidthBps": 6742261,
                    "averageBandwidthBps": 3602031
                  },
                  {
                    "quality": "720p",
                    "label": "720p",
                    "resolution": "1280x720",
                    "bandwidthBps": 3747445,
                    "averageBandwidthBps": 1733795
                  }
                ]
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<HlsQualitiesResponseDto>(sampleJson)

        assertTrue(response.success)
        assertEquals("HLS qualities fetched successfully", response.message)
        assertNotNull(response.data)

        val data = response.data!!
        assertEquals("6abbb384f582d90fbf73376f", data.videoId)
        assertTrue(data.autoAvailable)
        assertEquals(3, data.qualities.size)
        assertEquals("2160p", data.qualities[0].quality)
        assertEquals("4K", data.qualities[0].label)
        assertEquals("4096x2160", data.qualities[0].resolution)
        assertEquals(23874276L, data.qualities[0].bandwidthBps)
    }

    @Test
    fun parseHlsPlaybackResponse_manualMode() {
        val sampleJson = """
            {
              "success": true,
              "message": "HLS playback selected",
              "data": {
                "mode": "manual",
                "selectedQuality": "720p",
                "resolution": "1280x720",
                "bandwidthBps": 2778636,
                "averageBandwidthBps": 1237503,
                "availableQualities": [
                  "2160p",
                  "1080p",
                  "720p",
                  "480p",
                  "360p"
                ],
                "streamingType": "hls",
                "playbackUrl": "/api/videos/6abbb384f582d90fbf73376d/playback/hls/assets/eyJ2aWRlb0lkIjoiNmFiYmIzODRmNTgyZDkwZmJmNzMzNzZkIiwidXNlcklkIjoiNmFmNTk5YzVlZGFiN2U2MjIzYzdkNmE2IiwicHJvZmlsZUlkIjoiNmFmNTk5YzVlZGFiN2U2MjIzYzdkNmE3IiwibWFzdGVyS2V5IjoidmlkZW9zL0hvdyBEYXRhIENlbnRlcnMgaW4gU3BhY2UgQ291bGQgQ2hhbmdlIEFJXzIxNjBwL2hscy9Ib3cgRGF0YSBDZW50ZXJzIGluIFNwYWNlIENvdWxkIENoYW5nZSBBSV8yMTYwcC5tM3U4Iiwib2JqZWN0S2V5IjoidmlkZW9zL0hvdyBEYXRhIENlbnRlcnMgaW4gU3BhY2UgQ291bGQgQ2hhbmdlIEFJXzIxNjBwL2hscy9Ib3cgRGF0YSBDZW50ZXJzIGluIFNwYWNlIENvdWxkIENoYW5nZSBBSV8yMTYwcF83MjBwLm0zdTgiLCJleHBpcmVzQXQiOjE3OTQ1NTIwMjd9.WyqVLjn_a_zgwJFv0gKfgQvcEF8ngEMFffAHUS6T59o",
                "expiresIn": 300
              }
            }
        """.trimIndent()

        val response = json.decodeFromString<HlsPlaybackResponseDto>(sampleJson)

        assertTrue(response.success)
        assertEquals("HLS playback selected", response.message)
        assertNotNull(response.data)

        val data = response.data!!
        assertEquals("manual", data.mode)
        assertEquals("720p", data.selectedQuality)
        assertEquals(300L, data.expiresIn)
        assertTrue(data.playbackUrl.startsWith("/api/videos/"))

        val absoluteUrl = data.playbackUrl.toAbsolutePlaybackUrl()
        assertTrue(absoluteUrl.startsWith("https://stg-api.dealshot.in/api/videos/"))
    }

    @Test
    fun categoryIsolation_onlyInternationalIsHls() {
        val internationalVideo = Video(
            id = "1",
            title = "Test International",
            description = "Desc",
            duration = "10:00",
            thumbnail = "",
            url = "http://example.com/video.mp4",
            category = "International"
        )

        val nationalVideo = Video(
            id = "2",
            title = "Test National",
            description = "Desc",
            duration = "10:00",
            thumbnail = "",
            url = "http://example.com/video.mp4",
            category = "National"
        )

        assertTrue(internationalVideo.isHlsCategory())
        assertTrue(isHlsCategory("International"))
        assertTrue(isHlsCategory("international"))
        assertFalse(nationalVideo.isHlsCategory())
        assertFalse(isHlsCategory("National"))
        assertFalse(isHlsCategory("Motivational"))
        assertFalse(isHlsCategory("Academic"))
    }
}
