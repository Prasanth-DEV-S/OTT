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
     * Clear all saved preferences
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
