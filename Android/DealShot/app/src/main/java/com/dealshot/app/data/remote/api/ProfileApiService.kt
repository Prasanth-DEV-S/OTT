package com.dealshot.app.data.remote.api

import com.dealshot.app.data.remote.dto.AvatarsResponseDto
import com.dealshot.app.data.remote.dto.CreateProfileRequestDto
import com.dealshot.app.data.remote.dto.DeleteProfileResponseDto
import com.dealshot.app.data.remote.dto.ProfileResponseDto
import com.dealshot.app.data.remote.dto.ProfilesListResponseDto
import com.dealshot.app.data.remote.dto.SelectProfileResponseDto
import com.dealshot.app.data.remote.dto.UpdateAvatarRequestDto
import com.dealshot.app.data.remote.dto.UpdateProfileRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProfileApiService {

    @GET("avatars")
    suspend fun getPresetAvatars(): Response<AvatarsResponseDto>

    @POST("profiles")
    suspend fun createProfile(
        @Body request: CreateProfileRequestDto
    ): Response<ProfileResponseDto>

    @GET("profiles")
    suspend fun getProfiles(): Response<ProfilesListResponseDto>

    @GET("profiles/{profileId}")
    suspend fun getProfileDetails(
        @Path("profileId") profileId: String
    ): Response<ProfileResponseDto>

    @PATCH("profiles/{profileId}")
    suspend fun updateProfile(
        @Path("profileId") profileId: String,
        @Body request: UpdateProfileRequestDto
    ): Response<ProfileResponseDto>

    @PUT("profiles/{profileId}/avatar")
    suspend fun updateProfileAvatar(
        @Path("profileId") profileId: String,
        @Body request: UpdateAvatarRequestDto
    ): Response<ProfileResponseDto>

    @DELETE("profiles/{profileId}")
    suspend fun deleteProfile(
        @Path("profileId") profileId: String
    ): Response<DeleteProfileResponseDto>

    @POST("profiles/{profileId}/select")
    suspend fun selectProfile(
        @Path("profileId") profileId: String
    ): Response<SelectProfileResponseDto>
}
