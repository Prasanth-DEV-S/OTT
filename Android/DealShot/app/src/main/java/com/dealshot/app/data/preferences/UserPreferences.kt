package com.dealshot.app.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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

        private val TOKEN = stringPreferencesKey("token")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val MOBILE = stringPreferencesKey("mobile")
        private val EMAIL = stringPreferencesKey("email")
        private val ROLE = stringPreferencesKey("role")

        private val IS_MOBILE_VERIFIED =
            booleanPreferencesKey("is_mobile_verified")

        private val MAX_PROFILES =
            intPreferencesKey("max_profiles")

        private val SUBSCRIPTION_PLAN_ID =
            stringPreferencesKey("subscription_plan_id")

        private val SUBSCRIPTION_START_DATE =
            stringPreferencesKey("subscription_start_date")

        private val SUBSCRIPTION_END_DATE =
            stringPreferencesKey("subscription_end_date")

        private val SUBSCRIPTION_IS_ACTIVE =
            booleanPreferencesKey("subscription_is_active")

        private val SUBSCRIPTION_STATUS =
            stringPreferencesKey("subscription_status")

        private val LAST_LOGIN_AT =
            stringPreferencesKey("last_login_at")

        private val MAX_DEVICES =
            intPreferencesKey("max_devices")
    }

    /**
     * Save complete authenticated session.
     */
    suspend fun saveLoginSession(
        token: String,
        userId: String,
        name: String,
        mobile: String,
        email: String,
        role: String,
        isMobileVerified: Boolean,
        isNewUser: Boolean,
        maxProfiles: Int,
        maxDevices: Int,
        subscriptionPlanId: String?,
        subscriptionStartDate: String?,
        subscriptionEndDate: String?,
        subscriptionIsActive: Boolean,
        subscriptionStatus: String,
        lastLoginAt: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN] = token
            preferences[USER_ID] = userId
            preferences[USER_NAME] = name
            preferences[MOBILE] = mobile
            preferences[EMAIL] = email
            preferences[ROLE] = role
            preferences[IS_MOBILE_VERIFIED] = isMobileVerified
            preferences[IS_NEW_USER] = isNewUser
            preferences[MAX_PROFILES] = maxProfiles
            preferences[MAX_DEVICES] = maxDevices
            preferences[LAST_ACTIVITY] = System.currentTimeMillis()

            subscriptionPlanId?.let { preferences[SUBSCRIPTION_PLAN_ID] = it }
            subscriptionStartDate?.let { preferences[SUBSCRIPTION_START_DATE] = it }
            subscriptionEndDate?.let { preferences[SUBSCRIPTION_END_DATE] = it }

            preferences[SUBSCRIPTION_IS_ACTIVE] = subscriptionIsActive
            preferences[SUBSCRIPTION_STATUS] = subscriptionStatus
            preferences[LAST_LOGIN_AT] = lastLoginAt
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
            preferences[TOKEN]
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
            preferences[MOBILE]
        }

    /**
     * Logged-in email.
     */
    val email: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[EMAIL]
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
            preferences.remove(TOKEN)

            preferences.remove(USER_ID)
            preferences.remove(MOBILE_NUMBER)
            preferences.remove(USER_ROLE)
            preferences.remove(IS_NEW_USER)
            preferences.remove(LAST_ACTIVITY)

            preferences.remove(USER_NAME)
            preferences.remove(MOBILE)
            preferences.remove(EMAIL)
            preferences.remove(ROLE)
            preferences.remove(IS_MOBILE_VERIFIED)

            preferences.remove(MAX_PROFILES)
            preferences.remove(MAX_DEVICES)

            preferences.remove(SUBSCRIPTION_PLAN_ID)
            preferences.remove(SUBSCRIPTION_START_DATE)
            preferences.remove(SUBSCRIPTION_END_DATE)
            preferences.remove(SUBSCRIPTION_IS_ACTIVE)
            preferences.remove(SUBSCRIPTION_STATUS)

            preferences.remove(LAST_LOGIN_AT)
        }
    }

}