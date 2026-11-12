package com.dealshot.app.util

import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Helper responsible for parsing, validating, and handling DealShot deep links.
 *
 * Supported formats (configurable):
 * - dealshot://video/{videoId}
 * - dealshot://details/{videoId}
 * - https://dealshot.in/video/{videoId}
 * - https://dealshot.in/details/{videoId}
 * - Subdomains: https://api.dealshot.in/video/{videoId}
 * - URLs with query parameters: ?videoId={videoId} or ?video={videoId} or ?id={videoId}
 */
object DeepLinkHelper {

    private const val TAG = "DeepLinkHelper"

    // Configurable allowed schemes and hosts for deep link resolution
    var allowedSchemes = setOf("dealshot", "https", "http")
    var allowedHosts = setOf("dealshot.in", "www.dealshot.in", "stg-api.dealshot.in", "video", "details")

    sealed interface DeepLinkTarget {
        data class VideoDetails(val videoId: String) : DeepLinkTarget
        data object Invalid : DeepLinkTarget
    }

    /**
     * Extracts and validates video ID from raw input string (can be full URL or direct video ID).
     */
    fun extractVideoId(rawInput: String?): String? {
        if (rawInput.isNullOrBlank()) return null

        val trimmed = rawInput.trim()

        // 1. Direct ID check (alphanumeric / mongo / uuid format, no scheme/slashes)
        if (!trimmed.contains("://") && !trimmed.contains("/") && !trimmed.contains("?")) {
            return if (trimmed.length >= 3) trimmed else null
        }

        return try {
            val uri = Uri.parse(trimmed)
            extractVideoId(uri)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse URI: $rawInput", e)
            null
        }
    }

    /**
     * Extracts and validates video ID from an Android Uri.
     */
    fun extractVideoId(uri: Uri?): String? {
        if (uri == null) return null

        val scheme = uri.scheme?.lowercase()
        val host = uri.host?.lowercase()

        // Validate scheme if present
        if (scheme != null && scheme !in allowedSchemes) {
            Log.d(TAG, "Unsupported scheme: $scheme")
            return null
        }

        // 1. Check query parameters (?videoId=xxx, ?video=xxx, ?id=xxx)
        val queryId = uri.getQueryParameter("videoId")
            ?: uri.getQueryParameter("video")
            ?: uri.getQueryParameter("id")

        if (!queryId.isNullOrBlank()) {
            return queryId.trim()
        }

        // 2. Check path segments (e.g., /video/{id} or /details/{id})
        val pathSegments = uri.pathSegments
        if (pathSegments.isNotEmpty()) {
            // Check for /video/{id} or /details/{id}
            val keyIndex = pathSegments.indexOfFirst { it.equals("video", ignoreCase = true) || it.equals("details", ignoreCase = true) }
            if (keyIndex != -1 && keyIndex + 1 < pathSegments.size) {
                val candidateId = pathSegments[keyIndex + 1].trim()
                if (candidateId.isNotBlank()) {
                    return candidateId
                }
            }

            // Custom scheme: dealshot://video/{videoId} where host == "video"
            if (host != null && (host == "video" || host == "details")) {
                val candidateId = pathSegments.firstOrNull()?.trim()
                if (!candidateId.isNullOrBlank()) {
                    return candidateId
                }
            }

            // Fallback: Last path segment if it looks like an identifier
            val last = pathSegments.lastOrNull()?.trim()
            if (!last.isNullOrBlank() && last.length >= 3 && last != "video" && last != "details") {
                return last
            }
        }

        return null
    }

    /**
     * Parses an incoming Android Intent for deep links.
     */
    fun parseIntent(intent: Intent?): DeepLinkTarget {
        val uri = intent?.data ?: return DeepLinkTarget.Invalid
        val videoId = extractVideoId(uri)
        return if (!videoId.isNullOrBlank()) {
            DeepLinkTarget.VideoDetails(videoId)
        } else {
            DeepLinkTarget.Invalid
        }
    }
}
