package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.SelectPresetAvatarRequestDto
import com.dealshot.app.data.remote.dto.UpdateUserAccountRequestDto
import com.dealshot.app.data.remote.dto.UserAccountResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

interface UserApiService {

    @GET("users/me/profile")
    suspend fun getUserAccountProfile(): Response<UserAccountResponseDto>

    @PATCH("users/me/profile")
    suspend fun updateUserAccountProfile(
        @Body request: UpdateUserAccountRequestDto
    ): Response<UserAccountResponseDto>

    @PUT("users/me/avatar/select")
    suspend fun selectAccountPresetAvatar(
        @Body request: SelectPresetAvatarRequestDto
    ): Response<UserAccountResponseDto>

    @Multipart
    @POST("users/me/avatar")
    suspend fun uploadCustomAvatar(
        @Part avatar: MultipartBody.Part
    ): Response<UserAccountResponseDto>

    @DELETE("users/me")
    suspend fun deleteUserAccount(): Response<UserAccountResponseDto>
}
