package com.dealshot.app.presentation.shared.profile

import android.content.Context
import android.net.Uri
import com.dealshot.app.data.remote.dto.AvatarDto
import com.dealshot.app.data.remote.dto.MaturityLevel
import com.dealshot.app.data.remote.dto.ProfileDto
import com.dealshot.app.data.subscription.SubscriptionState

enum class ProfileFormMode {
    CREATE,
    EDIT
}

data class ProfileState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeletingProfile: Boolean = false,
    val deletingProfileId: String? = null,
    val error: String? = null,
    val profiles: List<ProfileDto> = emptyList(),
    val selectedProfile: ProfileDto? = null,
    val showCreateProfileDialog: Boolean = false,
    val presetAvatars: List<AvatarDto> = emptyList(),
    val isAvatarsLoading: Boolean = false,

    // Centralized Subscription & Profile Limits State
    val subscriptionState: SubscriptionState = SubscriptionState(),
    val maxProfiles: Int = 5,

    // Main Account Level Info (does not change when switching sub-profiles)
    val accountEmail: String = "",
    val accountGender: String = "",
    val accountName: String = "",

    // Form fields for create/edit
    val username: String = "",
    val avatar: String = "",
    val selectedAvatarId: String? = null,
    val gender: String = "prefer_not_to_say",
    val isKidsProfile: Boolean = false,
    val language: String = "en",
    val maturityLevel: String = MaturityLevel.ADULT,
    val autoplayNext: Boolean = true,
    val autoplayPreviews: Boolean = true,

    // Existing UI state properties
    val phoneNumber: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val isEditingProfile: Boolean = false,
    val isLoadingProfileDetails: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val deleteAccountError: String? = null
)

sealed interface ProfileIntent {
    data object LoadProfile : ProfileIntent
    data object LoadProfiles : ProfileIntent
    data class LoadProfileDetails(val profileId: String) : ProfileIntent
    data class SelectProfile(val profile: ProfileDto) : ProfileIntent

    data object BackClicked : ProfileIntent
    data object EditProfileClicked : ProfileIntent
    data object SaveProfileClicked : ProfileIntent
    data object RetryClicked : ProfileIntent

    data object OpenCreateProfileDialog : ProfileIntent
    data object CloseCreateProfileDialog : ProfileIntent
    data object AddProfileClicked : ProfileIntent
    data object CreateProfileClicked : ProfileIntent
    data class DeleteProfile(val profileId: String) : ProfileIntent
    data object LoadPresetAvatars : ProfileIntent
    data class SelectPresetAvatar(val avatarId: String) : ProfileIntent
    data class UploadCustomAvatar(val context: Context, val uri: Uri) : ProfileIntent

    // Form field intents
    data class UsernameChanged(val value: String) : ProfileIntent
    data class EmailChanged(val value: String) : ProfileIntent
    data class GenderChanged(val value: String) : ProfileIntent
    data class AvatarChanged(val value: String) : ProfileIntent
    data class SelectedAvatarIdChanged(val value: String?) : ProfileIntent
    data class IsKidsProfileChanged(val value: Boolean) : ProfileIntent
    data class LanguageChanged(val value: String) : ProfileIntent
    data class MaturityLevelChanged(val value: String) : ProfileIntent
    data class AutoplayNextChanged(val value: Boolean) : ProfileIntent
    data class AutoplayPreviewsChanged(val value: Boolean) : ProfileIntent

    // Existing UI intents
    data object ChangeProfilePictureClicked : ProfileIntent
    data class ProfilePictureSelected(val uri: String) : ProfileIntent
    data object DownloadQualityClicked : ProfileIntent
    data object DownloadWifiOnlyClicked : ProfileIntent
    data object DeviceManagementClicked : ProfileIntent
    data object HelpCenterClicked : ProfileIntent
    data object LogoutClicked : ProfileIntent
    data object DeleteAccountClicked : ProfileIntent
    data object ClearError : ProfileIntent
    data object CancelEditClicked : ProfileIntent
}

sealed interface ProfileEffect {
    data object NavigateBack : ProfileEffect
    data object NavigateToCreateProfile : ProfileEffect
    data object OpenImagePicker : ProfileEffect
    data object OpenDownloadQuality : ProfileEffect
    data object OpenDeviceManagement : ProfileEffect
    data object OpenHelpCenter : ProfileEffect
    data object Logout : ProfileEffect
    data object DeleteAccount : ProfileEffect
    data object ProfileCreated : ProfileEffect
    data object ProfileUpdated : ProfileEffect
    data class ProfileDeleted(val profileId: String) : ProfileEffect
    data class ShowMessage(val message: String) : ProfileEffect
}
