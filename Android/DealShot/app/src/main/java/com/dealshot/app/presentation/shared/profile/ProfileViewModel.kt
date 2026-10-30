package com.dealshot.app.presentation.shared.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.dto.CreateProfileRequestDto
import com.dealshot.app.data.remote.dto.ProfileDto
import com.dealshot.app.data.remote.dto.UpdateProfileRequestDto
import com.dealshot.app.data.remote.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileViewModel(
    private val userPreferences: UserPreferences,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<ProfileEffect>(Channel.BUFFERED)
    private var usernameBeforeEdit = _uiState.value.username
    private var emailBeforeEdit = _uiState.value.email
    private var imageBeforeEdit = _uiState.value.profileImageUrl
    val effect = _effect.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                userPreferences.mobileNumber,
                userPreferences.userName,
                userPreferences.email,
                userPreferences.avatarUrl
            ) { mobile, name, email, avatar ->
                val finalMobile = mobile.orEmpty()
                val finalName = when {
                    !name.isNullOrBlank() && name != "User Name" && name != "Guest" -> name
                    finalMobile.length >= 4 -> "User ${finalMobile.takeLast(4)}"
                    else -> ""
                }

                _uiState.update { state ->
                    state.copy(
                        phoneNumber = finalMobile,
                        username = if (state.username.isBlank() || state.username == "Guest" || state.username == "User Name") finalName else state.username,
                        email = state.email.ifBlank { email.orEmpty() },
                        avatar = state.avatar.ifBlank { avatar.orEmpty() },
                        profileImageUrl = state.profileImageUrl ?: avatar?.takeIf { it.isNotBlank() }
                    )
                }
            }.collect {}
        }
    }

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LoadProfile,
            ProfileIntent.LoadProfiles,
            ProfileIntent.RetryClicked -> loadProfiles()

            is ProfileIntent.LoadProfileDetails -> loadProfileDetails(intent.profileId)

            is ProfileIntent.SelectProfile -> selectProfile(intent.profile)

            ProfileIntent.BackClicked -> sendEffect(ProfileEffect.NavigateBack)

            is ProfileIntent.EditProfileClicked -> {
                usernameBeforeEdit = _uiState.value.username
                emailBeforeEdit = _uiState.value.email
                imageBeforeEdit = _uiState.value.profileImageUrl
                _uiState.update { it.copy(isEditingProfile = true, error = null) }
            }

            ProfileIntent.CancelEditClicked -> {
                _uiState.update {
                    it.copy(
                        isEditingProfile = false,
                        username = usernameBeforeEdit,
                        email = emailBeforeEdit,
                        profileImageUrl = imageBeforeEdit,
                        error = null
                    )
                }
            }

            ProfileIntent.OpenCreateProfileDialog -> {
                _uiState.update {
                    it.copy(
                        showCreateProfileDialog = true,
                        username = "",
                        isKidsProfile = false,
                        language = "en",
                        maturityLevel = "kids",
                        autoplayNext = true,
                        autoplayPreviews = true,
                        error = null
                    )
                }
            }

            ProfileIntent.CloseCreateProfileDialog -> {
                _uiState.update { it.copy(showCreateProfileDialog = false, error = null) }
            }

            ProfileIntent.CreateProfileClicked -> createProfile()

            ProfileIntent.SaveProfileClicked ->
                saveProfile()

            is ProfileIntent.UsernameChanged -> {
                _uiState.update { it.copy(username = intent.value, error = null) }
            }

            is ProfileIntent.EmailChanged -> {
                _uiState.update { it.copy(email = intent.value, error = null) }
            }

            is ProfileIntent.AvatarChanged -> {
                _uiState.update { it.copy(avatar = intent.value, error = null) }
            }

            is ProfileIntent.IsKidsProfileChanged -> {
                _uiState.update {
                    it.copy(
                        isKidsProfile = intent.value,
                        maturityLevel = if (intent.value) "kids" else it.maturityLevel,
                        error = null
                    )
                }
            }

            is ProfileIntent.LanguageChanged -> {
                _uiState.update { it.copy(language = intent.value, error = null) }
            }

            is ProfileIntent.MaturityLevelChanged -> {
                _uiState.update { it.copy(maturityLevel = intent.value, error = null) }
            }

            is ProfileIntent.AutoplayNextChanged -> {
                _uiState.update { it.copy(autoplayNext = intent.value, error = null) }
            }

            is ProfileIntent.AutoplayPreviewsChanged -> {
                _uiState.update { it.copy(autoplayPreviews = intent.value, error = null) }
            }

            ProfileIntent.ChangeProfilePictureClicked ->
                sendEffect(ProfileEffect.OpenImagePicker)

            is ProfileIntent.ProfilePictureSelected -> {
                _uiState.update { it.copy(profileImageUrl = intent.uri, error = null) }
            }

            ProfileIntent.DownloadQualityClicked ->
                sendEffect(ProfileEffect.OpenDownloadQuality)

            ProfileIntent.DownloadWifiOnlyClicked -> {
                // DataStore setting
            }

            ProfileIntent.DeviceManagementClicked ->
                sendEffect(ProfileEffect.OpenDeviceManagement)

            ProfileIntent.HelpCenterClicked ->
                sendEffect(ProfileEffect.OpenHelpCenter)

            ProfileIntent.LogoutClicked -> {
                viewModelScope.launch {
                    userPreferences.clearUser()
                    _uiState.update {
                        ProfileState()
                    }
                    _effect.send(ProfileEffect.Logout)
                }
            }

            ProfileIntent.DeleteAccountClicked -> {
                viewModelScope.launch {
                    userPreferences.clearUser()
                    _uiState.update {
                        ProfileState()
                    }
                    _effect.send(ProfileEffect.DeleteAccount)
                }
            }

            ProfileIntent.ClearError -> {
                _uiState.update { it.copy(error = null) }
            }
        }
    }

    private fun sendEffect(effect: ProfileEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            // Step 1: Immediately load cached session data from DataStore
            val mobileNumber = userPreferences.mobileNumber.first().orEmpty()
            val userEmail = userPreferences.email.first().orEmpty()
            val userName = userPreferences.userName.first().orEmpty()
            val cachedAvatar = userPreferences.avatarUrl.first().orEmpty()

            // Update UI state instantly with cached static session data
            _uiState.update { state ->
                state.copy(
                    phoneNumber = mobileNumber,
                    email = state.email.ifBlank { userEmail },
                    username = state.username.ifBlank { userName },
                    avatar = state.avatar.ifBlank { cachedAvatar },
                    profileImageUrl = state.profileImageUrl ?: cachedAvatar.takeIf { it.isNotBlank() }
                )
            }

            // If user is not logged in / no mobile number in session, finish loading without making API calls
            if (mobileNumber.isBlank()) {
                _uiState.update { it.copy(isLoading = false, error = null) }
                return@launch
            }

            // Step 2: Fetch profiles from backend API if not already loaded
            if (_uiState.value.profiles.isNotEmpty()) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val profilesList = profileRepository.getProfiles()
                val selected = profilesList.firstOrNull { it.isDefault }
                    ?: profilesList.firstOrNull()

                val finalName = selected?.name?.takeIf { it.isNotBlank() } ?: userName
                val finalEmail = selected?.email?.takeIf { it.isNotBlank() } ?: userEmail
                val finalAvatar = selected?.avatar.orEmpty().ifBlank { cachedAvatar }

                // Cache fresh profile details back to DataStore
                userPreferences.saveProfileData(
                    name = finalName,
                    email = finalEmail,
                    avatar = finalAvatar
                )

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        phoneNumber = mobileNumber,
                        email = finalEmail,
                        profiles = profilesList,
                        selectedProfile = selected,
                        username = finalName,
                        avatar = finalAvatar,
                        profileImageUrl = state.profileImageUrl ?: finalAvatar.takeIf { it.isNotBlank() },
                        isKidsProfile = selected?.isKidsProfile ?: false,
                        language = selected?.language ?: "en",
                        maturityLevel = selected?.maturityLevel ?: "adult",
                        autoplayNext = selected?.autoplayNext ?: true,
                        autoplayPreviews = selected?.autoplayPreviews ?: true,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // If API fails (e.g. offline), retain the cached local session without locking out the user
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadProfileDetails(profileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val details = profileRepository.getProfileDetails(profileId)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        selectedProfile = details,
                        email = details.email?.takeIf { it.isNotBlank() } ?: state.email,
                        username = details.name,
                        avatar = details.avatar.orEmpty(),
                        isKidsProfile = details.isKidsProfile,
                        language = details.language ?: "en",
                        maturityLevel = details.maturityLevel,
                        autoplayNext = details.autoplayNext,
                        autoplayPreviews = details.autoplayPreviews,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val isNotFound = (e as? HttpException)?.code() == 404
                if (isNotFound) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Profile not found"
                        )
                    }
                    sendEffect(ProfileEffect.NavigateBack)
                } else {
                    handleError(e)
                }
            }
        }
    }

    private fun selectProfile(profile: ProfileDto) {
        _uiState.update { state ->
            state.copy(
                selectedProfile = profile,
                email = profile.email?.takeIf { it.isNotBlank() } ?: state.email,
                username = profile.name,
                avatar = profile.avatar.orEmpty(),
                isKidsProfile = profile.isKidsProfile,
                language = profile.language ?: "en",
                maturityLevel = profile.maturityLevel,
                autoplayNext = profile.autoplayNext,
                autoplayPreviews = profile.autoplayPreviews,
                error = null
            )
        }
    }

    private fun createProfile() {
        val state = _uiState.value
        if (state.isSaving) return

        val name = state.username.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Profile name cannot be empty") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val request = CreateProfileRequestDto(
                    name = name,
                    avatar = state.avatar,
                    isKidsProfile = state.isKidsProfile,
                    language = state.language,
                    maturityLevel = state.maturityLevel,
                    autoplayNext = state.autoplayNext,
                    autoplayPreviews = state.autoplayPreviews
                )

                val created = profileRepository.createProfile(request)

                _uiState.update { current ->
                    val updatedList = current.profiles + created
                    current.copy(
                        isSaving = false,
                        showCreateProfileDialog = false,
                        profiles = updatedList,
                        selectedProfile = created,
                        error = null
                    )
                }

                sendEffect(ProfileEffect.ProfileCreated)
                sendEffect(ProfileEffect.ShowMessage("Profile created successfully"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }


    private fun saveProfile() {
        val state = _uiState.value

        if (state.isSaving) return

        val current = state.selectedProfile
        val profileId = current?.profileId.orEmpty()

        val name = state.username.trim()
        val email = state.email.trim()

        if (name.isBlank()) {
            _uiState.update {
                it.copy(error = "Username cannot be empty")
            }
            return
        }

        // If no profile is loaded yet or profile ID is empty, create a new profile via POST /profiles
        if (current == null || profileId.isBlank()) {
            createProfile()
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, error = null)
            }

            try {
                val request = UpdateProfileRequestDto(
                    name = name.takeIf { it != current.name },
                    email = email.takeIf {
                        it != current.email.orEmpty()
                    },
                    avatar = state.avatar.takeIf {
                        it != current.avatar
                    },
                    isKidsProfile = state.isKidsProfile.takeIf {
                        it != current.isKidsProfile
                    },
                    language = state.language.takeIf {
                        it != current.language
                    },
                    maturityLevel = state.maturityLevel.takeIf {
                        it != current.maturityLevel
                    },
                    autoplayNext = state.autoplayNext.takeIf {
                        it != current.autoplayNext
                    },
                    autoplayPreviews = state.autoplayPreviews.takeIf {
                        it != current.autoplayPreviews
                    }
                )

                val updated =
                    profileRepository.updateProfile(profileId, request)

                _uiState.update { curr ->
                    val updatedProfiles = curr.profiles.map { profile ->
                        if (profile.profileId == updated.profileId) updated else profile
                    }

                    curr.copy(
                        isSaving = false,
                        isEditingProfile = false,
                        profiles = if (updatedProfiles.none { it.profileId == updated.profileId }) {
                            updatedProfiles + updated
                        } else {
                            updatedProfiles
                        },
                        selectedProfile = updated,
                        username = updated.name,
                        email = updated.email?.ifBlank { curr.email } ?: curr.email,
                        error = null
                    )
                }

                sendEffect(ProfileEffect.ProfileUpdated)
                sendEffect(
                    ProfileEffect.ShowMessage("Profile updated successfully")
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _uiState.update {
                    it.copy(isSaving = false)
                }
            }
        }
    }


    private fun handleError(throwable: Throwable) {
        if (throwable is CancellationException) {
            throw throwable
        }

        val message = when {
            throwable is IllegalStateException && !throwable.message.isNullOrBlank() ->
                throwable.message!!
            else -> AppErrorHandler.getUserMessage(throwable)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                error = message
            )
        }
    }
}
