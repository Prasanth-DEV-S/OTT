package com.dealshot.app.core.error

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.serialization.SerializationException

object AppErrorHandler {

    fun getUserMessage(throwable: Throwable): String {
        val explicitMessage = throwable.localizedMessage ?: throwable.message
        if (!explicitMessage.isNullOrBlank() &&
            !explicitMessage.startsWith("java.") &&
            !explicitMessage.startsWith("kotlin.") &&
            !explicitMessage.startsWith("retrofit2.")
        ) {
            return explicitMessage
        }

        return when (throwable) {
            is java.net.UnknownHostException ->
                "Unable to connect to the server. Please check your internet connection."

            is java.net.ConnectException ->
                "Unable to reach the server. Please try again later."

            is java.net.SocketTimeoutException ->
                "The server is taking too long to respond. Please try again."

            is java.io.IOException ->
                "A network error occurred. Please check your connection and try again."

            is SerializationException ->
                "Failed to process data from server. Please try again later."

            is retrofit2.HttpException -> when (throwable.code()) {
                400 -> "Invalid request. Please check your details and try again."
                401 -> "Unable to find your session or may have expired. Please sign in again."
                403 -> "You don't have permission to perform this action."
                404 -> "The requested content could not be found."
                409 -> "Email is already in use by another account."
                in 500..599 ->
                    "Our server is temporarily unavailable. Please try again later."
                else ->
                    "We couldn't complete your request. Please try again."
            }

            else ->
                explicitMessage?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
        }
    }
}
