package com.dealshot.app.presentation.mobile.profile

import android.widget.Toast
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
fun CreateProfileRoute(
    userPreferences: UserPreferences,
    onBack: () -> Unit
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

    LaunchedEffect(Unit) {
        viewModel.onIntent(ProfileIntent.LoadPresetAvatars)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.ProfileCreated -> {
                    onBack()
                }
                is ProfileEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    val avatarUrls = remember(state.presetAvatars) {
//        if (state.presetAvatars.isNotEmpty()) {
            state.presetAvatars.map { it.imageUrl }
//        } else {
//            (1..9).map { "file:///android_asset/avatars/avatar$it.svg" }
//        }
    }

    CreateProfileScreen(
        avatarUrls = avatarUrls,
        accountEmail = state.email,
        mobileNumber = state.phoneNumber,
        isSubmitting = state.isSaving,
        submitError = state.error,
        onSubmit = { avatarUrl, name, email, _ ->
            viewModel.onIntent(ProfileIntent.UsernameChanged(name))
            viewModel.onIntent(ProfileIntent.EmailChanged(email))
            if (!avatarUrl.isNullOrBlank()) {
                val matchedAvatar = state.presetAvatars.firstOrNull { it.imageUrl == avatarUrl }
                val avatarValue = matchedAvatar?.id?.ifBlank { null } ?: avatarUrl
                viewModel.onIntent(ProfileIntent.AvatarChanged(avatarValue))
            }
            viewModel.onIntent(ProfileIntent.CreateProfileClicked)
        },
        onBack = onBack
    )
}
