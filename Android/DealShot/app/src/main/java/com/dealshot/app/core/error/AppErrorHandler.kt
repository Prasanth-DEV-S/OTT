package com.dealshot.app.core.error

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

object AppErrorHandler {

    fun getUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is java.net.UnknownHostException ->
                "Unable to connect to the server. Please check your internet connection."

            is java.net.ConnectException ->
                "Unable to reach the server. Please try again later."

            is java.net.SocketTimeoutException ->
                "The server is taking too long to respond. Please try again."

            is java.io.IOException ->
                "A network error occurred. Please check your connection and try again."

            is retrofit2.HttpException -> when (throwable.code()) {
                401 -> "Your session may have expired. Please sign in again."
                403 -> "You don't have permission to perform this action."
                404 -> "The requested content could not be found."
                in 500..599 ->
                    "Our server is temporarily unavailable. Please try again later."
                else ->
                    "We couldn't complete your request. Please try again."
            }

            else ->
                "Something went wrong. Please try again."
        }
    }
}