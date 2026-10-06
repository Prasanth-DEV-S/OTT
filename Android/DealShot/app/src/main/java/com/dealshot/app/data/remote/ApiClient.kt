package com.dealshot.app.data.remote

import com.dealshot.app.BuildConfig
import com.dealshot.app.data.remote.api.AuthApiService
import com.dealshot.app.data.remote.api.VideoApiService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .build()

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
}