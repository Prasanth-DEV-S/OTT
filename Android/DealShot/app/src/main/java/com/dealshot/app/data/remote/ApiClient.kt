package com.dealshot.app.data.remote

import android.content.Context
import android.util.Log
import com.dealshot.app.BuildConfig
import com.dealshot.app.data.remote.api.AuthApiService
import com.dealshot.app.data.remote.api.SavedVideosApiService
import com.dealshot.app.data.remote.api.SubscriptionApiService
import com.dealshot.app.data.remote.api.VideoApiService
import com.dealshot.app.data.remote.api.VideoDetailsApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.dealshot.app.DealShotApplication
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.api.WatchProgressApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor


import com.dealshot.app.data.remote.api.ProfileApiService

object ApiClient {

    @Volatile
    private var applicationContext: Context? = null
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val authInterceptor = Interceptor { chain ->
        val context = DealShotApplication.instance.applicationContext
        val preferences = UserPreferences(context)

        val token = runBlocking {
            preferences.authToken.first()
        }?.trim()

        val request = chain.request()
            .newBuilder()
            .apply {
                if (!token.isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                }
            }
            .build()
        Log.d(
            "LoginSession",
            "Received token is blank: ${token?.isBlank()}"
        )
        chain.proceed(request)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().addInterceptor(authInterceptor).build()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(
            json.asConverterFactory(
                "application/json".toMediaType()
            )
        )
        .build()

    val videoApi: VideoApiService =
        retrofit.create(VideoApiService::class.java)
    val authApi: AuthApiService =
        retrofit.create(AuthApiService::class.java)

    val subscriptionApi: SubscriptionApiService =
        retrofit.create(SubscriptionApiService::class.java)

    val videoDetailsApiService =
        retrofit.create(VideoDetailsApiService::class.java)

    val savedVideosApiService =
        retrofit.create(SavedVideosApiService::class.java)

    val watchProgressApiService: WatchProgressApiService =
        retrofit.create(WatchProgressApiService::class.java)

    val profileApiService: ProfileApiService =
        retrofit.create(ProfileApiService::class.java)
}