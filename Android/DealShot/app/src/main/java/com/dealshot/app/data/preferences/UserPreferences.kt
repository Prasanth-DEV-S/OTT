package com.dealshot.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dealshot.app.data.subscription.SubscriptionState
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

         val MOBILE_NUMBER =
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
        private val AVATAR_URL = stringPreferencesKey("avatar_url")
        private val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")

        private val IS_MOBILE_VERIFIED =
            booleanPreferencesKey("is_mobile_verified")

        private val MAX_PROFILES =
            intPreferencesKey("max_profiles")

        private val SUBSCRIPTION_PLAN_ID =
            stringPreferencesKey("subscription_plan_id")

        private val SUBSCRIPTION_PLAN_NAME =
            stringPreferencesKey("subscription_plan_name")

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
        lastLoginAt: String,
        subscriptionPlanName: String? = null
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

            if (!subscriptionPlanId.isNullOrBlank()) {
                preferences[SUBSCRIPTION_PLAN_ID] = subscriptionPlanId
            } else {
                preferences.remove(SUBSCRIPTION_PLAN_ID)
            }

            if (!subscriptionPlanName.isNullOrBlank()) {
                preferences[SUBSCRIPTION_PLAN_NAME] = subscriptionPlanName
            } else {
                preferences.remove(SUBSCRIPTION_PLAN_NAME)
            }

            if (!subscriptionStartDate.isNullOrBlank()) {
                preferences[SUBSCRIPTION_START_DATE] = subscriptionStartDate
            } else {
                preferences.remove(SUBSCRIPTION_START_DATE)
            }

            if (!subscriptionEndDate.isNullOrBlank()) {
                preferences[SUBSCRIPTION_END_DATE] = subscriptionEndDate
            } else {
                preferences.remove(SUBSCRIPTION_END_DATE)
            }

            preferences[SUBSCRIPTION_IS_ACTIVE] = subscriptionIsActive
            preferences[SUBSCRIPTION_STATUS] = subscriptionStatus
            preferences[LAST_LOGIN_AT] = lastLoginAt
        }
        PreferenceManager(context).saveString("Mobile_No", mobile)
        PreferenceManager(context).saveString("token", token)
    }

    suspend fun saveSubscriptionState(
        planId: String?,
        planName: String? = null,
        startDate: String?,
        endDate: String?,
        isActive: Boolean,
        status: String
    ) {
        context.dataStore.edit { preferences ->
            if (!planId.isNullOrBlank()) {
                preferences[SUBSCRIPTION_PLAN_ID] = planId
            } else {
                preferences.remove(SUBSCRIPTION_PLAN_ID)
            }

            if (!planName.isNullOrBlank()) {
                preferences[SUBSCRIPTION_PLAN_NAME] = planName
            } else {
                preferences.remove(SUBSCRIPTION_PLAN_NAME)
            }

            if (!startDate.isNullOrBlank()) {
                preferences[SUBSCRIPTION_START_DATE] = startDate
            } else {
                preferences.remove(SUBSCRIPTION_START_DATE)
            }

            if (!endDate.isNullOrBlank()) {
                preferences[SUBSCRIPTION_END_DATE] = endDate
            } else {
                preferences.remove(SUBSCRIPTION_END_DATE)
            }

            preferences[SUBSCRIPTION_IS_ACTIVE] = isActive
            preferences[SUBSCRIPTION_STATUS] = status
        }
    }

    suspend fun saveMaxProfiles(maxProfiles: Int) {
        context.dataStore.edit { preferences ->
            preferences[MAX_PROFILES] = maxProfiles
        }
    }

    val maxProfiles: Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[MAX_PROFILES] ?: 5
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
     * Logged-in user name.
     */
    val userName: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[USER_NAME]
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
     * Logged-in profile avatar URL.
     */
    val avatarUrl: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[AVATAR_URL]
        }

    /**
     * Currently selected active profile ID.
     */
    val activeProfileId: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[ACTIVE_PROFILE_ID]
        }

    /**
     * Whether user has an active subscription.
     */
    val isSubscriptionActive: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[SUBSCRIPTION_IS_ACTIVE] ?: false
        }

    /**
     * Centralized subscription state.
     */
    val subscriptionState: Flow<SubscriptionState> =
        context.dataStore.data.map { preferences ->
            SubscriptionState(
                isActive = preferences[SUBSCRIPTION_IS_ACTIVE] ?: false,
                planId = preferences[SUBSCRIPTION_PLAN_ID],
                planName = preferences[SUBSCRIPTION_PLAN_NAME],
                startDate = preferences[SUBSCRIPTION_START_DATE],
                endDate = preferences[SUBSCRIPTION_END_DATE],
                status = preferences[SUBSCRIPTION_STATUS] ?: "inactive"
            )
        }

    /**
     * Store selected active profile ID.
     */
    suspend fun saveActiveProfileId(profileId: String) {
        context.dataStore.edit { preferences ->
            if (profileId.isNotBlank()) {
                preferences[ACTIVE_PROFILE_ID] = profileId
            } else {
                preferences.remove(ACTIVE_PROFILE_ID)
            }
        }
    }

    /**
     * Update/Cache profile details in DataStore preferences.
     */
    suspend fun saveProfileData(
        name: String,
        email: String,
        avatar: String
    ) {
        context.dataStore.edit { preferences ->
            if (name.isNotBlank()) preferences[USER_NAME] = name
            if (email.isNotBlank()) preferences[EMAIL] = email
            if (avatar.isNotBlank()) preferences[AVATAR_URL] = avatar
        }
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
            preferences.remove(AVATAR_URL)
            preferences.remove(ACTIVE_PROFILE_ID)
            preferences.remove(IS_MOBILE_VERIFIED)

            preferences.remove(MAX_PROFILES)
            preferences.remove(MAX_DEVICES)

            preferences.remove(SUBSCRIPTION_PLAN_ID)
            preferences.remove(SUBSCRIPTION_PLAN_NAME)
            preferences.remove(SUBSCRIPTION_START_DATE)
            preferences.remove(SUBSCRIPTION_END_DATE)
            preferences.remove(SUBSCRIPTION_IS_ACTIVE)
            preferences.remove(SUBSCRIPTION_STATUS)

            preferences.remove(LAST_LOGIN_AT)
        }
        PreferenceManager(context).saveString("Mobile_No", "")
    }
}
