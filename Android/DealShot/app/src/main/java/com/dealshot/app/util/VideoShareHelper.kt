package com.dealshot.app.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.repository.VideoRepository
import com.dealshot.app.data.remote.repository.VideoRepositoryImpl

/**
 * Helper responsible for fetching backend video share URLs, preparing share content,
 * invoking Android system Sharesheet, and copying share links to clipboard.
 */
object VideoShareHelper {

    /**
     * Generates standard fallback share URL for a given video ID.
     */
    fun generateShareUrl(videoId: String): String {
        val cleanId = videoId.trim()
        return if (cleanId.isNotBlank()) "https://dealshot.in/video/$cleanId" else "https://dealshot.in/video"
    }

    /**
     * Builds standard user-facing share text according to DealShot specs:
     * "[VIDEO_TITLE]
     * Watch this video on DealShot: [VIDEO_SHARE_URL]"
     */
    fun buildShareMessage(videoTitle: String, shareUrl: String): String {
        val titlePrefix = if (videoTitle.isNotBlank()) "$videoTitle\n\n" else ""
        return "${titlePrefix}Watch this video on DealShot: $shareUrl"
    }

    /**
     * Fetches real share URL from backend API GET /videos/{videoId}/share and opens native Sharesheet.
     */
    suspend fun fetchAndShareVideo(
        context: Context,
        videoId: String,
        videoTitle: String,
        videoRepository: VideoRepository = VideoRepositoryImpl(ApiClient.videoApi),
        chooserTitle: String = "Share video via"
    ) {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) {
            Toast.makeText(context, "Invalid video ID for sharing", Toast.LENGTH_SHORT).show()
            return
        }

        val result = videoRepository.generateShareLink(cleanId)

        result.fold(
            onSuccess = { shareDto ->
                val shareUrl = shareDto.shareUrl.ifBlank { shareDto.deepLink.orEmpty() }
                if (shareUrl.isNotBlank()) {
                    openNativeShareSheet(context, videoTitle.ifBlank { shareDto.title }, shareUrl, chooserTitle)
                } else {
                    Toast.makeText(context, "Share link is currently unavailable for this video", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { error ->
                val msg = error.localizedMessage ?: "Failed to generate share link"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Fetches real share URL from backend API GET /videos/{videoId}/share and copies to clipboard.
     */
    suspend fun fetchAndCopyShareLink(
        context: Context,
        videoId: String,
        videoRepository: VideoRepository = VideoRepositoryImpl(ApiClient.videoApi),
        onCopied: (() -> Unit)? = null
    ) {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) {
            Toast.makeText(context, "Invalid video ID", Toast.LENGTH_SHORT).show()
            return
        }

        val result = videoRepository.generateShareLink(cleanId)

        result.fold(
            onSuccess = { shareDto ->
                val shareUrl = shareDto.shareUrl.ifBlank { shareDto.deepLink.orEmpty() }
                if (shareUrl.isNotBlank()) {
                    copyShareLink(context, shareUrl, onCopied)
                } else {
                    Toast.makeText(context, "Share link is currently unavailable for this video", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { error ->
                val msg = error.localizedMessage ?: "Failed to generate share link"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Opens Android's native system Sharesheet using Intent.ACTION_SEND.
     */
    fun openNativeShareSheet(
        context: Context,
        videoTitle: String,
        shareUrl: String,
        chooserTitle: String = "Share video via"
    ) {
        val shareMessage = buildShareMessage(videoTitle, shareUrl)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, videoTitle.ifBlank { "DealShot Video" })
            putExtra(Intent.EXTRA_TEXT, shareMessage)
        }

        try {
            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (_: Exception) {
            Toast.makeText(context, "Could not open share options", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies video link to clipboard and provides feedback toast.
     */
    fun copyShareLink(
        context: Context,
        shareUrl: String,
        onCopied: (() -> Unit)? = null
    ) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("DealShot Video Link", shareUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
            onCopied?.invoke()
        } catch (_: Exception) {
            Toast.makeText(context, "Failed to copy link", Toast.LENGTH_SHORT).show()
        }
    }
}
