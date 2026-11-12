package com.dealshot.app.presentation.shared.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dealshot.app.core.error.AppErrorHandler
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.dto.CreateProfileRequestDto
import com.dealshot.app.data.remote.dto.ProfileDto
import com.dealshot.app.data.remote.dto.UpdateAvatarRequestDto
import com.dealshot.app.data.remote.dto.UpdateProfileRequestDto
import com.dealshot.app.data.remote.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        loadPresetAvatars()
        loadUserAccountProfile()
        viewModelScope.launch {
            userPreferences.subscriptionState.collect { subState ->
                var planName = subState.planName
                if (subState.isActive && planName.isNullOrBlank() && !subState.planId.isNullOrBlank()) {
                    try {
                        val subRepo = com.dealshot.app.data.subscription.SubscriptionRepositoryImpl(com.dealshot.app.data.remote.ApiClient.subscriptionApi)
                        val details = subRepo.getSubscriptionPlanDetails(subState.planId)
                        if (details != null) {
                            planName = details.name
                            userPreferences.saveSubscriptionState(
                                planId = subState.planId,
                                planName = planName,
                                startDate = subState.startDate,
                                endDate = subState.endDate,
                                isActive = subState.isActive,
                                status = subState.status
                            )
                        }
                    } catch (e: Exception) {
                        Log.w("ProfileViewModel", "Failed to resolve plan details", e)
                    }
                }

                _uiState.update { current ->
                    current.copy(
                        subscriptionState = subState.copy(planName = planName ?: subState.planName)
                    )
                }
            }
        }
        viewModelScope.launch {
            userPreferences.maxProfiles.collect { max ->
                _uiState.update { it.copy(maxProfiles = max) }
            }
        }
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
            ProfileIntent.RetryClicked -> {
                loadProfiles()
                loadPresetAvatars()
                loadUserAccountProfile()
            }

            ProfileIntent.LoadPresetAvatars -> loadPresetAvatars()

            is ProfileIntent.LoadProfileDetails -> loadProfileDetails(intent.profileId)

            is ProfileIntent.SelectProfile -> selectProfile(intent.profile)

            is ProfileIntent.DeleteProfile -> deleteProfile(intent.profileId)

            is ProfileIntent.SelectPresetAvatar -> selectPresetAvatar(intent.avatarId)

            is ProfileIntent.UploadCustomAvatar -> uploadCustomAvatar(intent.context, intent.uri)

            ProfileIntent.BackClicked -> sendEffect(ProfileEffect.NavigateBack)

            is ProfileIntent.EditProfileClicked -> {
                usernameBeforeEdit = _uiState.value.username
                emailBeforeEdit = _uiState.value.email
                imageBeforeEdit = _uiState.value.profileImageUrl
                _uiState.update { it.copy(isEditingProfile = true, error = null) }
                val targetProfileId = _uiState.value.selectedProfile?.profileId
                    ?: _uiState.value.profiles.firstOrNull { it.isActive }?.profileId
                    ?: _uiState.value.profiles.firstOrNull()?.profileId
                if (!targetProfileId.isNullOrBlank()) {
                    loadProfileDetails(targetProfileId)
                } else {
                    loadUserAccountProfile()
                }
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
                val current = _uiState.value
                if (current.profiles.size >= current.maxProfiles) {
                    val msg = "Maximum limit of ${current.maxProfiles} profiles reached"
                    _uiState.update { it.copy(error = msg) }
                    sendEffect(ProfileEffect.ShowMessage(msg))
                } else {
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
            }

            ProfileIntent.CloseCreateProfileDialog -> {
                _uiState.update { it.copy(showCreateProfileDialog = false, error = null) }
            }

            ProfileIntent.AddProfileClicked -> {
                val current = _uiState.value
                if (current.profiles.size >= current.maxProfiles) {
                    val msg = "Maximum limit of ${current.maxProfiles} profiles reached"
                    _uiState.update { it.copy(error = msg) }
                    sendEffect(ProfileEffect.ShowMessage(msg))
                } else {
                    sendEffect(ProfileEffect.NavigateToCreateProfile)
                }
            }

            ProfileIntent.CreateProfileClicked -> createProfile()

            ProfileIntent.SaveProfileClicked -> saveProfile()

            is ProfileIntent.UsernameChanged -> {
                _uiState.update { it.copy(username = intent.value, error = null) }
            }

            is ProfileIntent.EmailChanged -> {
                _uiState.update { it.copy(email = intent.value, error = null) }
            }

            is ProfileIntent.GenderChanged -> {
                _uiState.update { it.copy(gender = intent.value, error = null) }
            }

            is ProfileIntent.AvatarChanged -> {
                _uiState.update { it.copy(avatar = intent.value, error = null) }
            }

            is ProfileIntent.SelectedAvatarIdChanged -> {
                _uiState.update { it.copy(selectedAvatarId = intent.value, error = null) }
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
                deleteAccount()
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

    private fun loadUserAccountProfile() {
        viewModelScope.launch {
            try {
                val authRepo = com.dealshot.app.data.remote.repository.AuthRepositoryImpl(
                    authApiService = com.dealshot.app.data.remote.ApiClient.authApi,
                    userPreferences = userPreferences
                )
                val userDto = authRepo.getCurrentUser()
                _uiState.update { current ->
                    current.copy(
                        maxProfiles = userDto.maxProfiles,
                        accountEmail = userDto.email?.takeIf { it.isNotBlank() } ?: current.accountEmail,
                        accountGender = userDto.gender?.takeIf { it.isNotBlank() } ?: current.accountGender,
                        accountName = userDto.name.ifBlank { current.accountName },
                        phoneNumber = userDto.mobile.ifBlank { current.phoneNumber }
                    )
                }
            } catch (e: Exception) {
                Log.w("ProfileViewModel", "Failed to refresh /auth/me user info", e)
            }

            val result = profileRepository.getUserAccountProfile()
            result.fold(
                onSuccess = { accountData ->
                    _uiState.update { current ->
                        val mainEmail = accountData.email?.takeIf { it.isNotBlank() } ?: current.accountEmail
                        val mainGender = accountData.gender?.takeIf { it.isNotBlank() } ?: current.accountGender
                        val mainName = accountData.name?.takeIf { it.isNotBlank() } ?: current.accountName
                        current.copy(
                            accountEmail = mainEmail,
                            accountGender = mainGender,
                            accountName = mainName,
                            username = accountData.name?.takeIf { it.isNotBlank() } ?: current.username,
                            email = current.email.ifBlank { mainEmail },
                            gender = current.gender.ifBlank { mainGender },
                            avatar = accountData.avatar?.takeIf { it.isNotBlank() } ?: current.avatar,
                            profileImageUrl = accountData.avatar?.takeIf { it.isNotBlank() } ?: current.profileImageUrl
                        )
                    }
                },
                onFailure = { error ->
                    Log.w("ProfileViewModel", "Failed to load user account profile", error)
                }
            )
        }
    }

    private fun loadPresetAvatars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isAvatarsLoading = true) }
            val result = profileRepository.getPresetAvatars()
            result.fold(
                onSuccess = { avatars ->
                    _uiState.update { it.copy(presetAvatars = avatars, isAvatarsLoading = false) }
                },
                onFailure = { error ->
                    Log.w("ProfileViewModel", "Failed to load preset avatars", error)
                    _uiState.update { it.copy(isAvatarsLoading = false) }
                }
            )
        }
    }

    private fun selectPresetAvatar(avatarId: String) {
        val cleanId = avatarId.trim()
        if (cleanId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val activeSubProfileId = _uiState.value.selectedProfile?.profileId.orEmpty()
            if (activeSubProfileId.isNotBlank()) {
                val avatarResult = profileRepository.updateProfileAvatar(
                    activeSubProfileId,
                    UpdateAvatarRequestDto(avatarId = cleanId)
                )
                avatarResult.onSuccess { updatedProfile ->
                    _uiState.update { current ->
                        val updatedList = current.profiles.map { if (it.profileId == activeSubProfileId) updatedProfile else it }
                        current.copy(
                            selectedProfile = updatedProfile,
                            profiles = updatedList,
                            avatar = updatedProfile.avatarUrl ?: updatedProfile.avatar.orEmpty()
                        )
                    }
                }
            }

            val result = profileRepository.selectAccountPresetAvatar(cleanId)
            result.fold(
                onSuccess = { accountData ->
                    val newAvatarUrl = accountData.avatar.orEmpty()
                    _uiState.update { current ->
                        current.copy(
                            avatar = newAvatarUrl,
                            profileImageUrl = newAvatarUrl.ifBlank { current.profileImageUrl },
                            isSaving = false,
                            error = null
                        )
                    }
                    if (newAvatarUrl.isNotBlank()) {
                        userPreferences.saveProfileData(
                            name = _uiState.value.username,
                            email = _uiState.value.email,
                            avatar = newAvatarUrl
                        )
                    }
                    sendEffect(ProfileEffect.ShowMessage("Avatar updated successfully"))
                },
                onFailure = { error ->
                    val msg = error.localizedMessage ?: "Failed to select preset avatar"
                    _uiState.update { it.copy(isSaving = false, error = msg) }
                    sendEffect(ProfileEffect.ShowMessage(msg))
                }
            )
        }
    }

    fun uploadCustomAvatar(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = profileRepository.uploadCustomAvatar(context, uri)
            result.fold(
                onSuccess = { accountData ->
                    val newAvatarUrl = accountData.avatar.orEmpty()
                    _uiState.update { current ->
                        current.copy(
                            avatar = newAvatarUrl,
                            profileImageUrl = newAvatarUrl,
                            isSaving = false,
                            error = null
                        )
                    }
                    if (newAvatarUrl.isNotBlank()) {
                        userPreferences.saveProfileData(
                            name = _uiState.value.username,
                            email = _uiState.value.email,
                            avatar = newAvatarUrl
                        )
                    }
                    sendEffect(ProfileEffect.ShowMessage("Custom avatar uploaded successfully"))
                },
                onFailure = { error ->
                    val msg = error.localizedMessage ?: "Failed to upload custom avatar"
                    _uiState.update { it.copy(isSaving = false, error = msg) }
                    sendEffect(ProfileEffect.ShowMessage(msg))
                }
            )
        }
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            val mobileNumber = userPreferences.mobileNumber.first().orEmpty()
            val userEmail = userPreferences.email.first().orEmpty()
            val userName = userPreferences.userName.first().orEmpty()
            val cachedAvatar = userPreferences.avatarUrl.first().orEmpty()

            _uiState.update { state ->
                state.copy(
                    phoneNumber = mobileNumber,
                    email = state.email.ifBlank { userEmail },
                    username = state.username.ifBlank { userName },
                    avatar = state.avatar.ifBlank { cachedAvatar },
                    profileImageUrl = state.profileImageUrl ?: cachedAvatar.takeIf { it.isNotBlank() }
                )
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val profilesList = profileRepository.getProfiles()
                val selected = profilesList.firstOrNull { it.isDefault }
                    ?: profilesList.firstOrNull()

                selected?.let {
                    userPreferences.saveActiveProfileId(it.profileId)
                }

                val finalName = selected?.name?.takeIf { it.isNotBlank() } ?: userName
                val finalEmail = selected?.email?.takeIf { it.isNotBlank() } ?: userEmail
                val finalAvatar = (selected?.avatarUrl ?: selected?.avatar).orEmpty().ifBlank { cachedAvatar }

                if (finalName.isNotBlank() || finalEmail.isNotBlank()) {
                    userPreferences.saveProfileData(
                        name = finalName,
                        email = finalEmail,
                        avatar = finalAvatar
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        phoneNumber = mobileNumber,
                        email = finalEmail,
                        profiles = profilesList,
                        selectedProfile = selected ?: state.selectedProfile,
                        username = state.username.ifBlank { finalName },
                        avatar = state.avatar.ifBlank { finalAvatar },
                        profileImageUrl = state.profileImageUrl ?: finalAvatar.takeIf { it.isNotBlank() },
                        isKidsProfile = selected?.isKidsProfile ?: state.isKidsProfile,
                        language = selected?.language ?: state.language,
                        maturityLevel = selected?.maturityLevel ?: state.maturityLevel,
                        autoplayNext = selected?.autoplayNext ?: state.autoplayNext,
                        autoplayPreviews = selected?.autoplayPreviews ?: state.autoplayPreviews,
                        error = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to load profiles", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadProfileDetails(profileId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfileDetails = true, error = null) }

            try {
                val details = profileRepository.getProfileDetails(profileId)
                val accountRes = profileRepository.getUserAccountProfile()
                val accountData = accountRes.getOrNull()

                _uiState.update { state ->
                    val effectiveName = details.name.ifBlank { accountData?.name.orEmpty() }
                    val effectiveEmail = details.email?.takeIf { it.isNotBlank() } ?: accountData?.email.orEmpty()
                    val effectiveGender = accountData?.gender?.takeIf { it.isNotBlank() } ?: state.gender
                    val effectiveAvatar = details.effectiveAvatar.ifBlank { accountData?.avatar.orEmpty() }

                    state.copy(
                        isLoadingProfileDetails = false,
                        selectedProfile = details,
                        username = effectiveName,
                        email = effectiveEmail,
                        gender = effectiveGender,
                        avatar = effectiveAvatar,
                        selectedAvatarId = details.avatarId,
                        profileImageUrl = effectiveAvatar.takeIf { it.isNotBlank() },
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
                            isLoadingProfileDetails = false,
                            error = "Profile not found"
                        )
                    }
                    sendEffect(ProfileEffect.NavigateBack)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingProfileDetails = false,
                            error = AppErrorHandler.getUserMessage(e)
                        )
                    }
                }
            }
        }
    }

    private fun selectProfile(profile: ProfileDto) {
        val profileId = profile.profileId
        if (profileId.isBlank()) return

        _uiState.update { state ->
            state.copy(
                selectedProfile = profile,
                email = profile.email?.takeIf { it.isNotBlank() } ?: state.email,
                username = profile.name,
                avatar = profile.avatar.orEmpty(),
                profileImageUrl = profile.avatar?.takeIf { it.isNotBlank() },
                isKidsProfile = profile.isKidsProfile,
                language = profile.language ?: "en",
                maturityLevel = profile.maturityLevel,
                autoplayNext = profile.autoplayNext,
                autoplayPreviews = profile.autoplayPreviews,
                error = null
            )
        }

        viewModelScope.launch {
            userPreferences.saveActiveProfileId(profileId)

            val result = profileRepository.selectProfile(profileId)
            result.fold(
                onSuccess = { selectedDto ->
                    userPreferences.saveActiveProfileId(selectedDto.profileId)
                    userPreferences.saveProfileData(
                        name = selectedDto.name,
                        email = selectedDto.email.orEmpty(),
                        avatar = selectedDto.avatar.orEmpty()
                    )
                    _uiState.update { current ->
                        current.copy(
                            selectedProfile = selectedDto,
                            username = selectedDto.name,
                            email = selectedDto.email.orEmpty(),
                            avatar = selectedDto.avatar.orEmpty(),
                            profileImageUrl = selectedDto.avatar?.takeIf { it.isNotBlank() }
                        )
                    }
                    sendEffect(ProfileEffect.ShowMessage("Profile selected successfully"))
                },
                onFailure = { error ->
                    Log.w("ProfileViewModel", "Failed to select profile on backend", error)
                }
            )
        }
    }

    private fun deleteProfile(profileId: String) {
        val state = _uiState.value
        if (state.isDeletingProfile) return

        val cleanId = profileId.trim()
        if (cleanId.isBlank()) {
            _uiState.update { it.copy(error = "Invalid profile ID") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDeletingProfile = true,
                    deletingProfileId = cleanId,
                    error = null
                )
            }

            val result = profileRepository.deleteProfile(cleanId)

            result.fold(
                onSuccess = { response ->
                    try {
                        val freshProfiles = profileRepository.getProfiles()
                        val newSelected = freshProfiles.firstOrNull { it.isDefault }
                            ?: freshProfiles.firstOrNull()

                        _uiState.update { current ->
                            current.copy(
                                isDeletingProfile = false,
                                deletingProfileId = null,
                                profiles = freshProfiles,
                                selectedProfile = newSelected,
                                username = newSelected?.name ?: current.username,
                                email = newSelected?.email ?: current.email,
                                avatar = newSelected?.avatar.orEmpty(),
                                profileImageUrl = newSelected?.avatar?.takeIf { it.isNotBlank() },
                                error = null
                            )
                        }
                    } catch (e: Exception) {
                        _uiState.update { current ->
                            val updated = current.profiles.filterNot { it.profileId == cleanId }
                            val newSelected = if (current.selectedProfile?.profileId == cleanId) {
                                updated.firstOrNull { it.isDefault } ?: updated.firstOrNull()
                            } else {
                                current.selectedProfile
                            }
                            current.copy(
                                isDeletingProfile = false,
                                deletingProfileId = null,
                                profiles = updated,
                                selectedProfile = newSelected,
                                username = newSelected?.name ?: current.username,
                                email = newSelected?.email ?: current.email,
                                avatar = newSelected?.avatar.orEmpty(),
                                profileImageUrl = newSelected?.avatar?.takeIf { it.isNotBlank() },
                                error = null
                            )
                        }
                    }

                    sendEffect(ProfileEffect.ProfileDeleted(cleanId))
                    val msg = response.message?.takeIf { it.isNotBlank() } ?: "Profile deleted successfully"
                    sendEffect(ProfileEffect.ShowMessage(msg))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isDeletingProfile = false,
                            deletingProfileId = null,
                            error = error.message ?: "Failed to delete profile"
                        )
                    }
                    sendEffect(ProfileEffect.ShowMessage(error.message ?: "Failed to delete profile"))
                }
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
                val avatarIdToUse = state.selectedAvatarId.takeIf { !it.isNullOrBlank() }
                    ?: state.presetAvatars.firstOrNull { it.imageUrl == state.avatar || it.id == state.avatar }?.id

                val request = CreateProfileRequestDto(
                    name = name,
                    avatarId = avatarIdToUse,
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

    private fun deleteAccount() {
        if (_uiState.value.isDeletingAccount) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, deleteAccountError = null) }

            val result = profileRepository.deleteUserAccount()

            result.fold(
                onSuccess = {
                    userPreferences.clearUser()
                    _uiState.update { ProfileState() }
                    sendEffect(ProfileEffect.DeleteAccount)
                    sendEffect(ProfileEffect.ShowMessage("Account deleted successfully"))
                },
                onFailure = { error ->
                    val userMsg = error.message ?: "Failed to delete account"
                    _uiState.update {
                        it.copy(
                            isDeletingAccount = false,
                            deleteAccountError = userMsg,
                            error = userMsg
                        )
                    }
                    sendEffect(ProfileEffect.ShowMessage(userMsg))
                }
            )
        }
    }

    private fun saveProfile() {
        val state = _uiState.value

        if (state.isSaving) return

        val current = state.selectedProfile
        val profileId = current?.profileId.orEmpty()
        val isDefaultProfile = current?.isDefault == true
            || (current == null && state.profiles.firstOrNull { it.isDefault }?.profileId == profileId)
            || (current == null && state.profiles.firstOrNull()?.profileId == profileId)

        val name = state.username.trim()
        val email = state.email.trim()
        val gender = state.gender.trim()

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, error = null)
            }

            try {
                if (isDefaultProfile) {
                    if (name.isBlank()) {
                        _uiState.update {
                            it.copy(isSaving = false, error = "Profile name cannot be empty")
                        }
                        return@launch
                    }

                    if (name.length < 2 || name.length > 50) {
                        _uiState.update {
                            it.copy(isSaving = false, error = "Name must be between 2 and 50 characters.")
                        }
                        return@launch
                    }

                    if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        _uiState.update {
                            it.copy(isSaving = false, error = "Enter a valid email address.")
                        }
                        return@launch
                    }

                    val avatarIdToUse = state.selectedAvatarId.takeIf { !it.isNullOrBlank() }
                        ?: state.presetAvatars.firstOrNull { it.imageUrl == state.avatar || it.id == state.avatar }?.id

                    var updatedSubProfile: ProfileDto? = null
                    if (profileId.isNotBlank()) {
                        val request = UpdateProfileRequestDto(
                            name = name,
//                            email = email.ifBlank { null },
                            avatarId = avatarIdToUse,
                            isKidsProfile = state.isKidsProfile,
                            language = state.language,
                            maturityLevel = state.maturityLevel,
                            autoplayNext = state.autoplayNext,
                            autoplayPreviews = state.autoplayPreviews
                        )
                        try {
                            updatedSubProfile = profileRepository.updateProfile(profileId, request)
                        } catch (e: Exception) {
                            Log.w("ProfileViewModel", "Sub-profile update skipped or failed for $profileId", e)
                        }

                        if (!avatarIdToUse.isNullOrBlank()) {
                            try {
                                profileRepository.updateProfileAvatar(
                                    profileId,
                                    UpdateAvatarRequestDto(avatarId = avatarIdToUse)
                                )
                            } catch (e: Exception) {
                                Log.w("ProfileViewModel", "Sub-profile avatar update skipped or failed for $profileId", e)
                            }
                        }
                    }

                    val accountResult = profileRepository.updateUserAccountProfile(
                        name = name,
                        email = email.ifBlank { null },
                        gender = gender.ifBlank { null }
                    )
                    accountResult.getOrThrow()

                    if (!avatarIdToUse.isNullOrBlank()) {
                        profileRepository.selectAccountPresetAvatar(avatarIdToUse)
                    }

                    val freshProfiles = try { profileRepository.getProfiles() } catch (e: Exception) { state.profiles }
                    val refreshedSelected = if (profileId.isNotBlank()) {
                        try { profileRepository.getProfileDetails(profileId) } catch (e: Exception) { freshProfiles.firstOrNull { it.profileId == profileId } }
                    } else {
                        freshProfiles.firstOrNull { it.isDefault } ?: freshProfiles.firstOrNull()
                    }

                    val accountProfile = profileRepository.getUserAccountProfile().getOrNull()

                    val finalName = updatedSubProfile?.name?.takeIf { it.isNotBlank() }
                        ?: refreshedSelected?.name?.takeIf { it.isNotBlank() }
                        ?: accountProfile?.name?.takeIf { it.isNotBlank() }
                        ?: name

                    val finalEmail = updatedSubProfile?.email?.takeIf { it.isNotBlank() }
                        ?: refreshedSelected?.email?.takeIf { it.isNotBlank() }
                        ?: accountProfile?.email?.takeIf { it.isNotBlank() }
                        ?: email

                    val finalGender = accountProfile?.gender?.takeIf { it.isNotBlank() } ?: gender
                    val finalAvatar = (updatedSubProfile?.avatarUrl ?: updatedSubProfile?.avatar)?.takeIf { it.isNotBlank() }
                        ?: (refreshedSelected?.avatarUrl ?: refreshedSelected?.avatar)?.takeIf { it.isNotBlank() }
                        ?: accountProfile?.avatar.orEmpty()
                        .ifBlank { state.avatar }

                    userPreferences.saveProfileData(
                        name = finalName,
                        email = finalEmail,
                        avatar = finalAvatar
                    )

                    val updatedProfilesList = freshProfiles.map { dto ->
                        if (dto.profileId == profileId || (dto.isDefault && isDefaultProfile)) {
                            dto.copy(
                                name = finalName,
                                email = finalEmail,
                                avatar = finalAvatar,
                                avatarUrl = finalAvatar
                            )
                        } else dto
                    }

                    val updatedSelectedProfile = (refreshedSelected ?: updatedSubProfile ?: current)?.copy(
                        name = finalName,
                        email = finalEmail,
                        avatar = finalAvatar,
                        avatarUrl = finalAvatar
                    )

                    _uiState.update { current ->
                        current.copy(
                            username = finalName,
                            email = finalEmail,
                            gender = finalGender,
                            avatar = finalAvatar,
                            profileImageUrl = finalAvatar.takeIf { it.isNotBlank() },
                            selectedProfile = updatedSelectedProfile,
                            profiles = updatedProfilesList,
                            isSaving = false,
                            isEditingProfile = false,
                            error = null
                        )
                    }
                } else {
                    // ADDITIONAL PROFILE: ONLY Profile Name and Avatar ID are updated!
                    if (name.isBlank()) {
                        _uiState.update {
                            it.copy(isSaving = false, error = "Profile name cannot be empty")
                        }
                        return@launch
                    }

                    if (name.length < 2 || name.length > 50) {
                        _uiState.update {
                            it.copy(isSaving = false, error = "Name must be between 2 and 50 characters.")
                        }
                        return@launch
                    }

                    val avatarIdToUse = state.selectedAvatarId.takeIf { !it.isNullOrBlank() }
                        ?: state.presetAvatars.firstOrNull { it.imageUrl == state.avatar || it.id == state.avatar }?.id

                    var updatedSubProfile: ProfileDto? = null
                    if (profileId.isNotBlank()) {
                        val request = UpdateProfileRequestDto(
                            name = name,
                            avatarId = avatarIdToUse
                        )
                        try {
                            updatedSubProfile = profileRepository.updateProfile(profileId, request)
                        } catch (e: Exception) {
                            Log.w("ProfileViewModel", "Sub-profile update skipped or failed for $profileId", e)
                        }

                        if (!avatarIdToUse.isNullOrBlank()) {
                            try {
                                profileRepository.updateProfileAvatar(
                                    profileId,
                                    UpdateAvatarRequestDto(avatarId = avatarIdToUse)
                                )
                            } catch (e: Exception) {
                                Log.w("ProfileViewModel", "Sub-profile avatar update skipped or failed for $profileId", e)
                            }
                        }
                    }

                    val freshProfiles = try { profileRepository.getProfiles() } catch (e: Exception) { state.profiles }
                    val refreshedSelected = if (profileId.isNotBlank()) {
                        try { profileRepository.getProfileDetails(profileId) } catch (e: Exception) { freshProfiles.firstOrNull { it.profileId == profileId } }
                    } else {
                        freshProfiles.firstOrNull { it.profileId == profileId } ?: updatedSubProfile
                    }

                    val finalName = updatedSubProfile?.name?.takeIf { it.isNotBlank() }
                        ?: refreshedSelected?.name?.takeIf { it.isNotBlank() }
                        ?: name

                    val finalAvatar = (updatedSubProfile?.avatarUrl ?: updatedSubProfile?.avatar)?.takeIf { it.isNotBlank() }
                        ?: (refreshedSelected?.avatarUrl ?: refreshedSelected?.avatar)?.takeIf { it.isNotBlank() }
                        ?: state.avatar

                    val updatedProfilesList = freshProfiles.map { dto ->
                        if (dto.profileId == profileId) {
                            dto.copy(
                                name = finalName,
                                avatar = finalAvatar,
                                avatarUrl = finalAvatar
                            )
                        } else dto
                    }

                    val updatedSelectedProfile = (refreshedSelected ?: updatedSubProfile ?: current)?.copy(
                        name = finalName,
                        avatar = finalAvatar,
                        avatarUrl = finalAvatar
                    )

                    _uiState.update { current ->
                        current.copy(
                            username = finalName,
                            avatar = finalAvatar,
                            profileImageUrl = finalAvatar.takeIf { it.isNotBlank() },
                            selectedProfile = updatedSelectedProfile,
                            profiles = updatedProfilesList,
                            isSaving = false,
                            isEditingProfile = false,
                            error = null
                        )
                    }
                }

                sendEffect(ProfileEffect.ProfileUpdated)
                sendEffect(ProfileEffect.ShowMessage("Profile updated successfully"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error saving profile", e)
                val userMsg = AppErrorHandler.getUserMessage(e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = userMsg
                    )
                }
                sendEffect(ProfileEffect.ShowMessage(userMsg))
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        if (throwable is CancellationException) {
            throw throwable
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                error = AppErrorHandler.getUserMessage(throwable)
            )
        }
    }
}
