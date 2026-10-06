package com.dealshot.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "streambox_preferences"
)

class UserPreferences(
    private val context: Context
) {
    private val secureTokenStorage =
        SecureTokenStorage(context)
    companion object {

        private val AUTH_TOKEN =
            stringPreferencesKey("auth_token")

        private val USER_ID =
            stringPreferencesKey("user_id")

        private val MOBILE_NUMBER =
            stringPreferencesKey("mobile_number")

        private val USER_ROLE =
            stringPreferencesKey("user_role")

        private val IS_NEW_USER =
            booleanPreferencesKey("is_new_user")

        private val LAST_ACTIVITY =
            longPreferencesKey("last_activity")
    }

    /**
     * Save complete authenticated session.
     */
    suspend fun saveLoginSession(
        token: String,
        userId: String,
        mobile: String,
        role: String,
        isNewUser: Boolean
    ) {
        val encryptedToken =
            secureTokenStorage.encrypt(token)
        context.dataStore.edit { preferences ->

            preferences[AUTH_TOKEN] = encryptedToken
            preferences[USER_ID] = userId
            preferences[MOBILE_NUMBER] = mobile
            preferences[USER_ROLE] = role
            preferences[IS_NEW_USER] = isNewUser
            preferences[LAST_ACTIVITY] = System.currentTimeMillis()
        }
    }

    suspend fun updateLastActivity() {
        context.dataStore.edit { preferences ->
            preferences[LAST_ACTIVITY] = System.currentTimeMillis()
        }
    }

    /**
     * Authentication token.
     */
    val authToken: Flow<String?> =
        context.dataStore.data.map { preferences ->

            val encryptedToken =
                preferences[AUTH_TOKEN]

            encryptedToken?.let {
                secureTokenStorage.decrypt(it)
            }
        }

    /**
     * Logged-in user ID.
     */
    val userId: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[USER_ID]
        }

    /**
     * Logged-in mobile number.
     */
    val mobileNumber: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[MOBILE_NUMBER]
        }

    /**
     * Logged-in user role.
     */
    val userRole: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[USER_ROLE]
        }

    /**
     * Whether this is a newly created user.
     */
    val isNewUser: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[IS_NEW_USER] ?: false
        }

    val lastActivity: Flow<Long?> =
        context.dataStore.data.map { preferences ->
            preferences[LAST_ACTIVITY]
        }

    /**
     * Save only mobile number.
     *
     * Kept because existing code may already use this method.
     */
    suspend fun saveMobileNumber(number: String) {
        context.dataStore.edit { preferences ->
            preferences[MOBILE_NUMBER] = number
        }
    }

    /**
     * Clear authenticated user data.
     */
    suspend fun clearUser() {
        context.dataStore.edit { preferences ->

            preferences.remove(AUTH_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(MOBILE_NUMBER)
            preferences.remove(USER_ROLE)
            preferences.remove(IS_NEW_USER)
            preferences.remove(LAST_ACTIVITY)
        }
    }
}