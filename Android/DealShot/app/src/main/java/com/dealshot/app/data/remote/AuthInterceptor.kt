
package com.dealshot.app.data.remote

import android.content.Context
import android.util.Log
import com.dealshot.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    context: Context
) : Interceptor {

    private val userPreferences = UserPreferences(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Read the currently saved access token.
        val token = runBlocking {
            userPreferences.authToken.first()
        }?.trim()
        Log.d(
            "LoginSession",
            "Token saved locally: ${!token.isNullOrBlank()}"
        )
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.header(
                "Authorization",
                "Bearer $token"
            )
        }

        return chain.proceed(requestBuilder.build())
    }
}
