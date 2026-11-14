package com.dealshot.app.data.remote

import android.content.Context
import android.util.Log
import com.dealshot.app.BuildConfig
import com.dealshot.app.DealShotApplication
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.api.AuthApiService
import com.dealshot.app.data.remote.api.LikedVideosApiService
import com.dealshot.app.data.remote.api.ProfileApiService
import com.dealshot.app.data.remote.api.SavedVideosApiService
import com.dealshot.app.data.remote.api.SubscriptionApiService
import com.dealshot.app.data.remote.api.VideoApiService
import com.dealshot.app.data.remote.api.VideoDetailsApiService
import com.dealshot.app.data.remote.api.WatchNextApiService
import com.dealshot.app.data.remote.api.WatchProgressApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    @Volatile
    private var applicationContext: Context? = null

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Public endpoints must not attach Bearer token (Specifically: GET /videos/share/resolve/{videoId})
        val isPublicEndpoint = path.contains("videos/share/resolve") ||
                originalRequest.header("No-Authentication") != null

        if (isPublicEndpoint) {
            val cleanRequest = originalRequest.newBuilder()
                .removeHeader("No-Authentication")
                .removeHeader("Authorization")
                .build()
            return@Interceptor chain.proceed(cleanRequest)
        }

        val context = DealShotApplication.instance.applicationContext
        val preferences = UserPreferences(context)

        val token = runBlocking {
            preferences.authToken.first()
        }?.trim()

        val activeProfileId = runBlocking {
            preferences.activeProfileId.first()
        }?.trim()

        val requestBuilder = originalRequest.newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        if (!activeProfileId.isNullOrBlank() && originalRequest.header("X-Profile-Id") == null) {
            requestBuilder.header("X-Profile-Id", activeProfileId)
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            Log.w("ApiClient", "Unauthorized 401 received for endpoint: ${originalRequest.url}")
        }

        response
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = true
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

    val videoDetailsApiService: VideoDetailsApiService =
        retrofit.create(VideoDetailsApiService::class.java)

    val savedVideosApiService: SavedVideosApiService =
        retrofit.create(SavedVideosApiService::class.java)

    val watchProgressApiService: WatchProgressApiService =
        retrofit.create(WatchProgressApiService::class.java)

    val profileApiService: ProfileApiService =
        retrofit.create(ProfileApiService::class.java)

    val likedVideosApiService: LikedVideosApiService =
        retrofit.create(LikedVideosApiService::class.java)

    val watchNextApiService: WatchNextApiService =
        retrofit.create(WatchNextApiService::class.java)

    val userApi: com.dealshot.app.data.remote.api.UserApiService =
        retrofit.create(com.dealshot.app.data.remote.api.UserApiService::class.java)

    val reportApiService: com.dealshot.app.data.remote.repository.report.ReportApiService =
        retrofit.create(com.dealshot.app.data.remote.repository.report.ReportApiService::class.java)
}
