package com.dealshot.app.data.preferences

import android.content.Context
import android.content.SharedPreferences

data class PendingResumeState(
    val videoId: String,
    val positionMs: Long,
    val screen: String,
)

interface GuestPreviewRepository {
    fun isPreviewExhausted(videoId: String): Boolean
    fun setPreviewExhausted(videoId: String)
    fun isGuest(): Boolean

    fun savePendingResumeState(videoId: String, positionMs: Long, screen: String)
    fun getPendingResumeState(): PendingResumeState?
    fun clearPendingResumeState()

    fun saveLocalWatchProgress(videoId: String, positionMs: Long)
    fun getLocalWatchProgress(videoId: String): Long

    companion object {
        const val GUEST_PREVIEW_LIMIT_MS = 30_000L
    }
}

class GuestPreviewRepositoryImpl(context: Context) : GuestPreviewRepository {
    private val previewPrefs: SharedPreferences =
        context.getSharedPreferences("guest_preview", Context.MODE_PRIVATE)

    private val authPrefs = PreferenceManager(context)

    override fun isPreviewExhausted(videoId: String): Boolean {
        return previewPrefs.getBoolean(videoId, false)
    }

    override fun setPreviewExhausted(videoId: String) {
        previewPrefs.edit().putBoolean(videoId, true).apply()
    }

    override fun isGuest(): Boolean {
        val mobile = authPrefs.getString("Mobile_No")
        val token = authPrefs.getString("token")
        return mobile.isBlank() && token.isBlank()
    }

    override fun savePendingResumeState(videoId: String, positionMs: Long, screen: String) {
        previewPrefs.edit()
            .putString("pending_video_id", videoId)
            .putLong("pending_position", positionMs)
            .putString("pending_screen", screen)
            .apply()
    }

    override fun getPendingResumeState(): PendingResumeState? {
        val videoId = previewPrefs.getString("pending_video_id", null) ?: return null
        val positionMs = previewPrefs.getLong("pending_position", 30_000L)
        val screen = previewPrefs.getString("pending_screen", "details") ?: "details"
        return PendingResumeState(videoId, positionMs, screen)
    }

    override fun clearPendingResumeState() {
        previewPrefs.edit()
            .remove("pending_video_id")
            .remove("pending_position")
            .remove("pending_screen")
            .apply()
    }

    override fun saveLocalWatchProgress(videoId: String, positionMs: Long) {
        if (videoId.isNotBlank()) {
            previewPrefs.edit().putLong("watch_pos_$videoId", positionMs).apply()
        }
    }

    override fun getLocalWatchProgress(videoId: String): Long {
        return if (videoId.isNotBlank()) {
            previewPrefs.getLong("watch_pos_$videoId", 0L)
        } else 0L
    }
}
