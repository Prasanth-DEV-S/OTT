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
    onDeleteAccount: () -> Unit
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
                ProfileEffect.OpenImagePicker -> imagePickerLauncher.launch("image/*")
                ProfileEffect.OpenDownloadQuality -> onOpenDownloadQuality()
                ProfileEffect.OpenDeviceManagement -> onOpenDeviceManagement()
                ProfileEffect.OpenHelpCenter -> onOpenHelpCenter()
                ProfileEffect.Logout -> onLogout()
                ProfileEffect.DeleteAccount -> onDeleteAccount()
                ProfileEffect.ProfileCreated,
                ProfileEffect.ProfileUpdated -> Unit
                is ProfileEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    ProfileScreen(
        state = state,
        onIntent = viewModel::onIntent
    )
}
