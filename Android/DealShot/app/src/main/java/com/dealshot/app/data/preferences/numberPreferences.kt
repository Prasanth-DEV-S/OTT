package com.dealshot.app.data.preferences
import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {

    // 1. Initialize SharedPreferences safely using Private Mode
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    /**
     * Put (Save) a String value asynchronously
     */
    fun saveString(key: String, value: String) {
        sharedPreferences.edit()
            .putString(key, value)
            .apply() // .apply() writes to disk asynchronously (recommended)
    }

    /**
     * Get (Retrieve) a String value with a default fallback
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Put (Save) a Boolean value
     */
    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit()
            .putBoolean(key, value)
            .apply()
    }

    /**
     * Get (Retrieve) a Boolean value
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    /**
     * Check if user has an active logged in session
     */
    fun isLoggedIn(): Boolean {
        val mobileNo = getString("Mobile_No")
        val token = getString("token")
        val mobile = getString("mobile")
        val userId = getString("user_id")
        val authToken = getString("auth_token")
        return mobileNo.isNotBlank() || token.isNotBlank() || mobile.isNotBlank() || userId.isNotBlank() || authToken.isNotBlank()
    }

    /**
     * Save recent search queries (max 6)
     */
    fun saveRecentSearches(searches: List<String>) {
        val jsonString = searches.take(6).joinToString("|||")
        saveString("recent_searches", jsonString)
    }

    /**
     * Get recent search queries (max 6)
     */
    fun getRecentSearches(): List<String> {
        val raw = getString("recent_searches")
        if (raw.isBlank()) return emptyList()
        return raw.split("|||").map { it.trim() }.filter { it.isNotBlank() }.take(6)
    }

    /**
     * Clear all saved preferences
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
