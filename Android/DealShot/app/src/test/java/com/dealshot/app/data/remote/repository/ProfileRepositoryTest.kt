package com.dealshot.app.data.remote.repository

import com.dealshot.app.data.remote.api.ProfileApiService
import com.dealshot.app.data.remote.api.UserApiService
import com.dealshot.app.data.remote.dto.CreateProfileRequestDto
import com.dealshot.app.data.remote.dto.DeleteProfileResponseDto
import com.dealshot.app.data.remote.dto.ProfileDto
import com.dealshot.app.data.remote.dto.ProfileResponseDto
import com.dealshot.app.data.remote.dto.ProfilesListResponseDto
import com.dealshot.app.data.remote.dto.SelectProfileResponseDto
import com.dealshot.app.data.remote.dto.UpdateAvatarRequestDto
import com.dealshot.app.data.remote.dto.UpdateProfileRequestDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class ProfileRepositoryTest {

    private lateinit var mockApi: FakeProfileApiService
    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setUp() {
        mockApi = FakeProfileApiService()
        repository = ProfileRepositoryImpl(mockApi, FakeUserApiService())
    }

    @Test
    fun testGetProfiles_returnsProfileList() = runBlocking {
        val profiles = repository.getProfiles()
        assertEquals(1, profiles.size)
        assertEquals("Casey", profiles[0].name)
        assertEquals("6af2b7cf078edef2a470ae79", profiles[0].profileId)
    }

    @Test
    fun testCreateProfile_returnsCreatedProfile() = runBlocking {
        val request = CreateProfileRequestDto(
            name = "Casey1",
            avatarId = "6aec5c98194650def7b18b14",
            language = "en"
        )
        val created = repository.createProfile(request)
        assertEquals("Casey1", created.name)
        assertEquals("6af3f4ddb2f5d381bf5b3a89", created.profileId)
    }

    @Test
    fun testGetProfileDetails_returnsProfile() = runBlocking {
        val profile = repository.getProfileDetails("6af3f4ddb2f5d381bf5b3a89")
        assertEquals("Casey1", profile.name)
        assertEquals("6af3f4ddb2f5d381bf5b3a89", profile.profileId)
    }

    @Test
    fun testUpdateProfile_returnsUpdatedProfile() = runBlocking {
        val request = UpdateProfileRequestDto(
            name = "UpdatedCasey"
        )
        val updated = repository.updateProfile("6af3f4ddb2f5d381bf5b3a89", request)
        assertEquals("UpdatedCasey", updated.name)
    }

    @Test
    fun testUpdateProfileAvatar_returnsUpdatedProfile() = runBlocking {
        val request = UpdateAvatarRequestDto(
            avatarId = "6aec5c98194650def7b18b14"
        )
        val result = repository.updateProfileAvatar("6af3f4ddb2f5d381bf5b3a89", request)
        assertTrue(result.isSuccess)
        assertEquals("6aec5c98194650def7b18b14", result.getOrNull()?.avatarId)
    }

    @Test
    fun testSelectProfile_returnsSelectedProfile() = runBlocking {
        val result = repository.selectProfile("6af3f4ddb2f5d381bf5b3a89")
        assertTrue(result.isSuccess)
        assertEquals("6af3f4ddb2f5d381bf5b3a89", result.getOrNull()?.profileId)
    }

    @Test
    fun testDeleteUserAccount_returnsSuccess() = runBlocking {
        val result = repository.deleteUserAccount()
        assertTrue(result.isSuccess)
    }
}

private class FakeProfileApiService : ProfileApiService {
    override suspend fun getPresetAvatars() = Response.success(com.dealshot.app.data.remote.dto.AvatarsResponseDto(success = true))

    override suspend fun createProfile(request: CreateProfileRequestDto): Response<ProfileResponseDto> {
        val dto = ProfileDto(
            id = "6af3f4ddb2f5d381bf5b3a89",
            name = request.name,
            avatarId = request.avatarId,
            language = request.language
        )
        return Response.success(ProfileResponseDto(success = true, message = "Profile created successfully", data = dto))
    }

    override suspend fun getProfiles(): Response<ProfilesListResponseDto> {
        val dto = ProfileDto(
            id = "6af2b7cf078edef2a470ae79",
            name = "Casey",
            avatarId = "6aec5c98194650def7b18b14",
            isDefault = true
        )
        return Response.success(ProfilesListResponseDto(success = true, count = 1, data = listOf(dto)))
    }

    override suspend fun getProfileDetails(profileId: String): Response<ProfileResponseDto> {
        val dto = ProfileDto(
            id = profileId,
            name = "Casey1",
            avatarId = "6aec5c98194650def7b18b14"
        )
        return Response.success(ProfileResponseDto(success = true, data = dto))
    }

    override suspend fun updateProfile(profileId: String, request: UpdateProfileRequestDto): Response<ProfileResponseDto> {
        val dto = ProfileDto(
            id = profileId,
            name = request.name ?: "Casey1",
            avatarId = request.avatarId
        )
        return Response.success(ProfileResponseDto(success = true, message = "Profile updated successfully", data = dto))
    }

    override suspend fun updateProfileAvatar(profileId: String, request: UpdateAvatarRequestDto): Response<ProfileResponseDto> {
        val dto = ProfileDto(
            id = profileId,
            name = "Casey1",
            avatarId = request.avatarId
        )
        return Response.success(ProfileResponseDto(success = true, message = "Profile avatar updated successfully", data = dto))
    }

    override suspend fun deleteProfile(profileId: String): Response<DeleteProfileResponseDto> {
        return Response.success(DeleteProfileResponseDto(success = true, message = "Profile deleted successfully"))
    }

    override suspend fun selectProfile(profileId: String): Response<SelectProfileResponseDto> {
        val dto = ProfileDto(
            id = profileId,
            name = "Casey1"
        )
        return Response.success(SelectProfileResponseDto(success = true, message = "Profile selected successfully", data = dto))
    }
}

private class FakeUserApiService : UserApiService {
    override suspend fun getUserAccountProfile() = Response.success(com.dealshot.app.data.remote.dto.UserAccountResponseDto(success = true))
    override suspend fun updateUserAccountProfile(request: com.dealshot.app.data.remote.dto.UpdateUserAccountRequestDto) = Response.success(com.dealshot.app.data.remote.dto.UserAccountResponseDto(success = true))
    override suspend fun selectAccountPresetAvatar(request: com.dealshot.app.data.remote.dto.SelectPresetAvatarRequestDto) = Response.success(com.dealshot.app.data.remote.dto.UserAccountResponseDto(success = true))
    override suspend fun uploadCustomAvatar(avatar: okhttp3.MultipartBody.Part) = Response.success(com.dealshot.app.data.remote.dto.UserAccountResponseDto(success = true))
    override suspend fun deleteUserAccount() = Response.success(com.dealshot.app.data.remote.dto.UserAccountResponseDto(success = true))
}
