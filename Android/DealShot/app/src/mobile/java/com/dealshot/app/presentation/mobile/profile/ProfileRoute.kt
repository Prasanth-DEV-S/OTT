package com.dealshot.app.presentation.mobile.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.data.preferences.UserPreferences
import com.dealshot.app.data.remote.ApiClient
import com.dealshot.app.data.remote.repository.ProfileRepositoryImpl
import com.dealshot.app.presentation.shared.profile.ProfileEffect
import com.dealshot.app.presentation.shared.profile.ProfileIntent
import com.dealshot.app.presentation.shared.profile.ProfileViewModel

@Composable
fun ProfileRoute(
    userPreferences: UserPreferences,
    onBack: () -> Unit,
    onOpenDownloadQuality: () -> Unit,
    onOpenDeviceManagement: () -> Unit,
    onOpenHelpCenter: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onAddProfile: () -> Unit = {},
    onOpenSubscription: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember {
        ProfileRepositoryImpl(ApiClient.profileApiService)
    }

    val viewModel: ProfileViewModel = viewModel(
        factory = remember(userPreferences, repository) {
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(userPreferences, repository) as T
                }
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onIntent(ProfileIntent.ProfilePictureSelected(it.toString()))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.onIntent(ProfileIntent.LoadProfiles)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateBack -> onBack()
                ProfileEffect.NavigateToCreateProfile -> onAddProfile()
                ProfileEffect.OpenImagePicker -> imagePickerLauncher.launch("image/*")
                ProfileEffect.OpenDownloadQuality -> onOpenDownloadQuality()
                ProfileEffect.OpenDeviceManagement -> onOpenDeviceManagement()
                ProfileEffect.OpenHelpCenter -> onOpenHelpCenter()
                ProfileEffect.Logout -> onLogout()
                ProfileEffect.DeleteAccount -> onDeleteAccount()
                ProfileEffect.ProfileCreated,
                ProfileEffect.ProfileUpdated -> {
                    viewModel.onIntent(ProfileIntent.LoadProfiles)
                }
                is ProfileEffect.ProfileDeleted -> Unit
                is ProfileEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val displayProfiles = remember(state.profiles, state.username) {
        state.profiles.map { dto ->
            ProfileListItem(
                id = dto.profileId,
                name = if (dto.isDefault && state.username.isNotBlank()) state.username else dto.name,
                avatarUrl = dto.avatarUrl.takeIf { !it.isNullOrBlank() } ?: dto.avatar,
                isPrimary = dto.isDefault
            )
        }
    }

    val subscriptionLabel = if (com.dealshot.app.data.subscription.hasPremiumAccess(state.subscriptionState)) {
        state.subscriptionState.planName?.ifBlank { "Premium" } ?: "Premium"
    } else {
        "Free"
    }

    val accountEmail = state.accountEmail.ifBlank { state.email }
    val accountGender = state.accountGender.ifBlank { state.gender }

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent,
        email = accountEmail,
        gender = accountGender,
        subscriptionLabel = subscriptionLabel,
        profiles = displayProfiles,
        selectedProfileId = state.selectedProfile?.profileId ?: state.profiles.firstOrNull { it.isActive }?.profileId,
        isSaving = state.isSaving,
        saveError = state.error,
        isDeleting = state.isDeletingAccount,
        deleteError = state.deleteAccountError,
        onSaveProfile = { name, email, gender, avatarId, avatarUrl ->
            viewModel.onIntent(ProfileIntent.UsernameChanged(name))
            viewModel.onIntent(ProfileIntent.EmailChanged(email))
            if (gender.isNotBlank()) {
                viewModel.onIntent(ProfileIntent.GenderChanged(gender))
            }
            if (!avatarId.isNullOrBlank()) {
                viewModel.onIntent(ProfileIntent.SelectedAvatarIdChanged(avatarId))
                viewModel.onIntent(ProfileIntent.AvatarChanged(avatarId))
            } else if (!avatarUrl.isNullOrBlank()) {
                viewModel.onIntent(ProfileIntent.AvatarChanged(avatarUrl))
            }
            viewModel.onIntent(ProfileIntent.SaveProfileClicked)
        },
        onProfileSelected = { id ->
            state.profiles.firstOrNull { it.profileId == id }?.let { dto ->
                viewModel.onIntent(ProfileIntent.SelectProfile(dto))
            }
        },
        onRemoveProfile = { profileId ->
            viewModel.onIntent(ProfileIntent.DeleteProfile(profileId))
        },
        onDeleteAccount = onDeleteAccount,
        onAddProfile = onAddProfile,
        onViewSubscription = onOpenSubscription,
        onUnlockPremium = onOpenSubscription,
        onLogin = onNavigateToLogin
    )
}
