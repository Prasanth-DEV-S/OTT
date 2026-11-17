package com.dealshot.app.presentation.mobile.profile

import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.dealshot.app.R
import com.dealshot.app.data.remote.dto.AvatarDto
import com.dealshot.app.presentation.shared.profile.ProfileFormMode
import com.dealshot.app.presentation.shared.profile.ProfileIntent
import com.dealshot.app.presentation.shared.profile.ProfileState
import com.dealshot.app.ui.AnimatedGoldenBackground

// Target Design System Colors
private val ScreenBackground = Color(0xFF080807)
private val CardSurface = Color(0xFF151411)
private val SecondarySurface = Color(0xFF211E17)
private val GoldPrimary = Color(0xFFD8AD55)
private val GoldLight = Color(0xFFF2DDA6)
private val TextWhite = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFFA6A29A)
private val DividerDark = Color(0xFF302B20)
private val DangerRed = Color(0xFFD65C52)
private val SuccessGreen = Color(0xFF36C96A)
private val GoldGradient = listOf(Color(0xFFE8C575), Color(0xFFD8AD55), Color(0xFF9E6B15))

// =====================================================================
// SCREEN (route-facing; parameters and callbacks unchanged)
// =====================================================================

@Composable
fun ProfileScreen(
    state: ProfileState,
    onIntent: (ProfileIntent) -> Unit,
    email: String = "",
    gender: String = "",
    language: String = "",
    subscriptionLabel: String = "Free",
    subscriptionEndDate: String? = null,
    profiles: List<ProfileListItem> = emptyList(),
    selectedProfileId: String? = null,
    isSaving: Boolean = false,
    saveError: String? = null,
    isDeleting: Boolean = false,
    deleteError: String? = null,
    onSaveProfile: (userName: String, email: String, gender: String, avatarId: String?, avatarUrl: String?) -> Unit = { _, _, _, _, _ -> },
    onDeleteAccount: () -> Unit = {},
    onProfileSelected: (String) -> Unit = {},
    onRemoveProfile: (String) -> Unit = {},
    onAddProfile: () -> Unit = {},
    onViewSubscription: () -> Unit = {},
    onUnlockPremium: () -> Unit = {},
    onLogin: () -> Unit = {}
) {
    BackHandler {
        onIntent(ProfileIntent.BackClicked)
    }

    val effectiveProfiles = remember(profiles, state.profiles) {
        if (profiles.isNotEmpty()) {
            profiles
        } else {
            state.profiles.map { dto ->
                ProfileListItem(
                    id = dto.profileId,
                    name = dto.name,
                    avatarUrl = dto.avatarUrl.takeIf { !it.isNullOrBlank() } ?: dto.avatar,
                    isPrimary = dto.isDefault
                )
            }
        }
    }

    val effectiveSelectedProfileId = selectedProfileId
        ?: state.selectedProfile?.profileId
        ?: state.profiles.firstOrNull { it.isActive }?.profileId

    val activeProfile = effectiveProfiles.firstOrNull { it.id == effectiveSelectedProfileId }

    Box(Modifier.fillMaxSize().background(ScreenBackground)) {
        AnimatedGoldenBackground(Modifier.matchParentSize())
        Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.82f)))

        // Single primary vertical scroll container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var lastEditExitTime by remember { mutableLongStateOf(0L) }
            val isEditingMode = state.isEditingProfile

            BackHandler(enabled = isEditingMode || (System.currentTimeMillis() - lastEditExitTime < 600L)) {
                if (isEditingMode) {
                    lastEditExitTime = System.currentTimeMillis()
                    onIntent(ProfileIntent.CancelEditClicked)
                }
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered Top Header
                ProfileTopBar(
                    onBack = {
                        val now = System.currentTimeMillis()
                        if (isEditingMode) {
                            lastEditExitTime = now
                            onIntent(ProfileIntent.CancelEditClicked)
                        } else if (now - lastEditExitTime >= 600L) {
                            onIntent(ProfileIntent.BackClicked)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Main Cohesive Card Container
                ProfilePopupCard(
                    userName = if (activeProfile?.isPrimary == true || activeProfile == null) {
                        state.username.ifBlank { activeProfile?.name.orEmpty() }
                    } else {
                        activeProfile.name
                    },
                    mobileNumber = state.phoneNumber,
                    profileImageUrl = state.profileImageUrl?.ifBlank { null } ?: state.avatar.ifBlank { null } ?: activeProfile?.avatarUrl,
                    onLogout = { onIntent(ProfileIntent.LogoutClicked) },
                    onLogin = onLogin,
                    email = email.ifBlank { state.email },
                    isSaving = isSaving,
                    saveError = saveError,
                    isDeleting = isDeleting,
                    deleteError = deleteError,
                    onSaveProfile = onSaveProfile,
                    onDeleteAccount = onDeleteAccount,
                    onEditProfileClicked = { onIntent(ProfileIntent.EditProfileClicked) },
                    gender = gender.ifBlank { state.gender },
                    language = language.ifBlank { state.language },
                    subscriptionLabel = subscriptionLabel,
                    subscriptionEndDate = subscriptionEndDate ?: state.subscriptionState.endDate,
                    profiles = effectiveProfiles,
                    maxProfiles = state.maxProfiles,
                    selectedProfileId = effectiveSelectedProfileId,
                    presetAvatars = state.presetAvatars,
                    onProfileSelected = { id ->
                        onProfileSelected(id)
                        state.profiles.firstOrNull { it.profileId == id }?.let { dto ->
                            onIntent(ProfileIntent.SelectProfile(dto))
                        }
                    },
                    onRemoveProfile = onRemoveProfile,
                    onAddProfile = onAddProfile,
                    onViewSubscription = onViewSubscription,
                    isLoggedIn = state.phoneNumber.isNotBlank() || state.email.isNotBlank() || (state.username.isNotBlank() && state.username != "Guest"),
                    onUnlockPremium = onUnlockPremium,
                    isLoadingProfileDetails = state.isLoadingProfileDetails,
                    isDeletingProfile = state.isDeletingProfile,
                    deletingProfileId = state.deletingProfileId,
                    profileError = state.error,
                    isEditingProfile = state.isEditingProfile,
                    onCancelEdit = { onIntent(ProfileIntent.CancelEditClicked) }
                )

                if (state.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = GoldPrimary,
                        strokeWidth = 2.dp
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/** Round back button on the left, centered ACCOUNT / My Profile / subtitle block. */
@Composable
internal fun ProfileTopBar(onBack: () -> Unit, enabled: Boolean = true) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(40.dp)
                .clip(CircleShape)
                .background(SecondarySurface)
                .border(1.dp, DividerDark, CircleShape)
                .clickable(enabled = enabled, onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = TextWhite,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp)
                .padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ACCOUNT",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "My Profile",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage your profile and account details",
                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// =====================================================================
// PROFILE CONTENT (Target Screenshot Layout)
// =====================================================================

@Composable
fun ProfilePopupCard(
    userName: String,
    mobileNumber: String,
    profileImageUrl: String? = null,
    onLogout: () -> Unit,
    onLogin: () -> Unit = {},
    email: String = "",
    isSaving: Boolean = false,
    saveError: String? = null,
    isDeleting: Boolean = false,
    deleteError: String? = null,
    onSaveProfile: (userName: String, email: String, gender: String, avatarId: String?, avatarUrl: String?) -> Unit = { _, _, _, _, _ -> },
    onDeleteAccount: () -> Unit = {},
    onEditProfileClicked: () -> Unit = {},
    gender: String = "",
    language: String = "",
    subscriptionLabel: String = "Free",
    subscriptionEndDate: String? = null,
    profiles: List<ProfileListItem> = emptyList(),
    maxProfiles: Int = 5,
    selectedProfileId: String? = null,
    presetAvatars: List<AvatarDto> = emptyList(),
    onProfileSelected: (String) -> Unit = {},
    onRemoveProfile: (String) -> Unit = {},
    onAddProfile: () -> Unit = {},
    onViewSubscription: () -> Unit = {},
    onUnlockPremium: () -> Unit = {},
    isLoadingProfileDetails: Boolean = false,
    isDeletingProfile: Boolean = false,
    deletingProfileId: String? = null,
    profileError: String? = null,
    isLoggedIn: Boolean,
    isEditingProfile: Boolean = false,
    onCancelEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayName = when {
        userName.isNotBlank() && userName != "User Name" && userName != "Guest" -> userName
        mobileNumber.length >= 4 -> "User ${mobileNumber.takeLast(4)}"
        else -> "Guest User"
    }

    val formattedMobile = if (mobileNumber.isNotBlank()) {
        if (mobileNumber.startsWith("+")) mobileNumber else "+91 $mobileNumber"
    } else {
        ""
    }

    var showLogoutConfirm by remember { mutableStateOf(false) }
    if (showLogoutConfirm) {
        LogoutConfirmDialog(
            onConfirm = {
                showLogoutConfirm = false
                onLogout()
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }

    var profileToRemove by remember { mutableStateOf<ProfileListItem?>(null) }
    var wasDeletingProfile by remember { mutableStateOf(false) }
    LaunchedEffect(isDeletingProfile) {
        if (wasDeletingProfile && !isDeletingProfile && profileError == null) {
            profileToRemove = null
        }
        wasDeletingProfile = isDeletingProfile
    }

    profileToRemove?.let { target ->
        RemoveProfileConfirmDialog(
            profileName = target.name,
            isDeleting = isDeletingProfile && deletingProfileId == target.id,
            errorMessage = if (deletingProfileId == target.id) profileError else null,
            onConfirm = {
                onRemoveProfile(target.id)
            },
            onDismiss = { if (!isDeletingProfile) profileToRemove = null }
        )
    }

    var isEditing by rememberSaveable { mutableStateOf(false) }
    var wasSaving by remember { mutableStateOf(false) }
    var lastEditExitTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isEditingProfile) {
        if (!isEditingProfile) {
            isEditing = false
        }
    }

    LaunchedEffect(isSaving) {
        if (wasSaving && !isSaving && saveError == null) {
            lastEditExitTime = System.currentTimeMillis()
            isEditing = false
            onCancelEdit()
        }
        wasSaving = isSaving
    }

    BackHandler(enabled = isEditing || isEditingProfile || (System.currentTimeMillis() - lastEditExitTime < 600L)) {
        if (isEditing || isEditingProfile) {
            lastEditExitTime = System.currentTimeMillis()
            isEditing = false
            onCancelEdit()
        }
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    if (showDeleteConfirm) {
        DeleteAccountConfirmDialog(
            isDeleting = isDeleting,
            errorMessage = deleteError,
            onConfirm = onDeleteAccount,
            onDismiss = { if (!isDeleting) showDeleteConfirm = false }
        )
    }

    val cardShape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Main Container Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = cardShape,
                    spotColor = GoldPrimary.copy(alpha = 0.2f),
                    ambientColor = Color.Black
                )
                .clip(cardShape)
                .background(CardSurface)
                .border(
                    width = 1.dp,
                    color = GoldPrimary.copy(alpha = 0.35f),
                    shape = cardShape
                )
                .padding(20.dp)
        ) {
            val activeProfile = profiles.firstOrNull { it.id == selectedProfileId }
            val isDefaultProfile = activeProfile?.isPrimary == true

            if (isLoggedIn && isEditing) {
                // Reuses the exact same Create Profile UI form architecture in EDIT mode
                EditProfileForm(
                    isDefaultProfile = isDefaultProfile,
                    presetAvatars = presetAvatars,
                    savedUserName = userName.takeIf {
                        it.isNotBlank() && it != "User Name" && it != "Guest"
                    }.orEmpty(),
                    savedEmail = email,
                    gender = gender,
                    formattedMobile = formattedMobile,
                    profileImageUrl = profileImageUrl,
                    isSaving = isSaving,
                    saveError = saveError,
                    isLoadingProfileDetails = isLoadingProfileDetails,
                    onSave = onSaveProfile,
                    onCancel = {
                        lastEditExitTime = System.currentTimeMillis()
                        isEditing = false
                        onCancelEdit()
                    }
                )
            } else {
                // Account Header Block
                ProfileHeader(
                    displayName = displayName,
                    formattedMobile = formattedMobile,
                    isLoggedIn = isLoggedIn,
                    profileImageUrl = profileImageUrl
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DividerDark, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoggedIn) {
                    // Profiles Section
                    ProfileSwitcherSection(
                        profiles = profiles,
                        maxProfiles = maxProfiles,
                        selectedProfileId = selectedProfileId,
                        onProfileSelected = onProfileSelected,
                        onRequestRemove = { profileToRemove = it },
                        onAddProfile = onAddProfile
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Edit Profile Button
                    EditProfileSection(onToggle = {
                        onEditProfileClicked()
                        isEditing = true
                    })

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DividerDark, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Details Section
                    AccountDetailsSection(
                        formattedMobile = formattedMobile,
                        email = email,
                        gender = gender,
                        language = language,
                        subscriptionLabel = subscriptionLabel,
                        subscriptionEndDate = subscriptionEndDate,
                        onViewSubscription = onViewSubscription
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Unlock Premium Section
                    SubscriptionSection(
                        subscriptionLabel = subscriptionLabel,
                        onUnlockPremium = onUnlockPremium
                    )
                } else {
                    GuestSection(onLogin = onLogin)
                }
            }
        }

        if (isLoggedIn && !isEditing) {
            Spacer(modifier = Modifier.height(16.dp))

            // Action rows below main card (Log out & Delete account)
            ProfileActionSection(
                onLogoutClick = { showLogoutConfirm = true },
                onDeleteClick = { showDeleteConfirm = true }
            )
        }
    }
}

// =====================================================================
// SECTIONS
// =====================================================================

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = GoldPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = modifier
    )
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(), content = content)
}

@Composable
private fun ProfileHeader(
    displayName: String,
    formattedMobile: String,
    isLoggedIn: Boolean,
    profileImageUrl: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(68.dp)) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .border(2.dp, GoldPrimary, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2417)),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatar(url = profileImageUrl, size = 60.dp)
            }

            if (isLoggedIn) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                        .border(2.dp, CardSurface, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isLoggedIn && formattedMobile.isNotBlank()) {
                Text(
                    text = formattedMobile,
                    color = TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "Sign in to watch premium stories",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileSwitcherSection(
    profiles: List<ProfileListItem>,
    maxProfiles: Int = 5,
    selectedProfileId: String?,
    onProfileSelected: (String) -> Unit,
    onRequestRemove: (ProfileListItem) -> Unit,
    onAddProfile: () -> Unit
) {
    val context = LocalContext.current
    SectionCard {
        SectionTitle("PROFILES (${profiles.size}/$maxProfiles)")
        Spacer(modifier = Modifier.height(10.dp))
        ProfilesRow(
            profiles = profiles,
            selectedProfileId = selectedProfileId,
            onSelect = { onProfileSelected(it.id) },
            onRemoveRequest = onRequestRemove,
            onAddProfile = {
                if (profiles.size >= maxProfiles) {
                    Toast.makeText(context, "Maximum limit of $maxProfiles profiles reached", Toast.LENGTH_SHORT).show()
                } else {
                    onAddProfile()
                }
            }
        )
    }
}

@Composable
private fun EditProfileSection(onToggle: () -> Unit) {
    GoldOutlineButton(
        label = "Edit Profile",
        onClick = onToggle,
        leading = {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

private fun formatSubscriptionEndDate(dateIso: String?): String? {
    if (dateIso.isNullOrBlank()) return null
    return try {
        val clean = dateIso.take(10)
        val parts = clean.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val monthInt = parts[1].toIntOrNull() ?: 1
            val day = parts[2]
            val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthStr = monthNames.getOrElse(monthInt - 1) { "Jan" }
            "$day $monthStr $year"
        } else {
            clean
        }
    } catch (_: Exception) {
        dateIso.take(10)
    }
}

@Composable
private fun AccountDetailsSection(
    formattedMobile: String,
    email: String,
    gender: String,
    language: String,
    subscriptionLabel: String,
    subscriptionEndDate: String? = null,
    onViewSubscription: () -> Unit
) {
    SectionCard {
        SectionTitle("ACCOUNT DETAILS")
        Spacer(modifier = Modifier.height(4.dp))

        AccountDetailRow(
            label = "Mobile Number",
            value = formattedMobile.ifBlank { "Not added" },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.iphone),
                    contentDescription = "Mobile Number",
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailing = if (formattedMobile.isNotBlank()) {
                {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16301C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.checkmark),
                            contentDescription = "Verified",
                            tint = SuccessGreen,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else null
        )
        HorizontalDivider(color = DividerDark, thickness = 1.dp)

        AccountDetailRow(
            label = "Email",
            value = email.ifBlank { "Not added" },
            icon = {
                Icon(Icons.Default.Email, "Email", tint = GoldPrimary, modifier = Modifier.size(18.dp))
            }
        )
        HorizontalDivider(color = DividerDark, thickness = 1.dp)

        AccountDetailRow(
            label = "Gender",
            value = gender.ifBlank { "Not set" },
            icon = {
                Icon(Icons.Default.Person, "Gender", tint = GoldPrimary, modifier = Modifier.size(18.dp))
            }
        )
        HorizontalDivider(color = DividerDark, thickness = 1.dp)

//        AccountDetailRow(
//            label = "Language",
//            value = language.ifBlank { "English" },
//            icon = {
//                Icon(Icons.Default.Language, "Language", tint = GoldPrimary, modifier = Modifier.size(18.dp))
//            }
//        )
//        HorizontalDivider(color = DividerDark, thickness = 1.dp)

        val formattedEndDate = formatSubscriptionEndDate(subscriptionEndDate)

        AccountDetailRow(
            label = "Subscription",
            value = subscriptionLabel,
            subValue = if (!formattedEndDate.isNullOrBlank()) "End Date: $formattedEndDate" else null,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.sparkles),
                    contentDescription = "Subscription",
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailing = {
                Text(
                    text = "View",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondarySurface)
                        .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewSubscription)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        )
    }
}

@Composable
private fun SubscriptionSection(
    subscriptionLabel: String,
    onUnlockPremium: () -> Unit
) {
    if (subscriptionLabel.equals("Free", ignoreCase = true)) {
        PrimaryGoldButton(
            label = "Unlock Premium",
            onClick = onUnlockPremium,
            leading = {
                Icon(
                    painter = painterResource(id = R.drawable.sparkles),
                    contentDescription = null,
                    tint = Color(0xFF140E02),
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
private fun ProfileActionSection(
    onLogoutClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ActionRow(
            title = "Log out",
            subtitle = "Sign out of your DealShot account",
            titleColor = TextWhite,
            borderColor = DividerDark,
            iconBg = SecondarySurface,
            showChevron = true,
            onClick = onLogoutClick,
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.rectangle_portrait_and_arrow_right),
                    contentDescription = "Log out",
                    tint = GoldPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        ActionRow(
            title = "Delete account",
            subtitle = "Permanently remove your DealShot account",
            titleColor = DangerRed,
            borderColor = DangerRed.copy(alpha = 0.5f),
            iconBg = Color(0xFF381F19),
            onClick = onDeleteClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete account",
                    tint = DangerRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    titleColor: Color,
    borderColor: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showChevron: Boolean = false,
    icon: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardSurface)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) { icon() }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = titleColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 12.sp)
        }

        if (showChevron) {
            Icon(
                painter = painterResource(id = R.drawable.chevron_right),
                contentDescription = "Go",
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PrimaryGoldButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape(14.dp)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(Brush.horizontalGradient(GoldGradient)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leading != null) {
                    leading()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    color = Color(0xFF140E02),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null,
                    tint = Color(0xFF140E02),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun GuestSection(onLogin: () -> Unit) {
    SectionCard {
        Text(
            text = "GET STARTED",
            color = GoldPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SecondarySurface)
                .border(1.dp, DividerDark, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Sign in or register now to unlock unlimited premium videos, save watch history, and sync across devices.",
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        PrimaryGoldButton(
            label = "Log In / Register",
            onClick = onLogin
        )
    }
}

// =====================================================================
// REUSABLE EDIT FORM (Reuses CreateProfileScreen in EDIT mode)
// =====================================================================

@Composable
private fun EditProfileForm(
    isDefaultProfile: Boolean,
    presetAvatars: List<AvatarDto>,
    savedUserName: String,
    savedEmail: String,
    gender: String,
    formattedMobile: String,
    profileImageUrl: String?,
    isSaving: Boolean,
    saveError: String?,
    isLoadingProfileDetails: Boolean = false,
    onSave: (userName: String, email: String, gender: String, avatarId: String?, avatarUrl: String?) -> Unit,
    onCancel: () -> Unit
) {
    if (isLoadingProfileDetails) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = GoldPrimary,
                strokeWidth = 2.5.dp
            )
        }
    } else {
        val avatarUrls = remember(presetAvatars) { presetAvatars.map { it.imageUrl } }
        CreateProfileScreen(
            mode = ProfileFormMode.EDIT,
            isDefaultProfile = isDefaultProfile,
            isEmbedded = true,
            initialName = savedUserName,
            initialEmail = savedEmail,
            initialGender = gender,
            initialAvatarUrl = profileImageUrl,
            avatarUrls = avatarUrls,
            accountEmail = savedEmail,
            mobileNumber = formattedMobile,
            isSubmitting = isSaving,
            submitError = saveError,
            onSubmit = { avatarUrl, name, email, newGender ->
                val matchedAvatar = presetAvatars.firstOrNull { it.imageUrl == avatarUrl }
                val avatarId = matchedAvatar?.id?.ifBlank { null }
                onSave(name, email, newGender, avatarId, avatarUrl)
            },
            onBack = onCancel
        )
    }
}

// =====================================================================
// DIALOGS
// =====================================================================

@Composable
fun DeleteAccountConfirmDialog(
    isDeleting: Boolean,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var isAgreed by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isDeleting,
            dismissOnClickOutside = !isDeleting,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .fillMaxWidth(0.96f)
                .heightIn(max = 660.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF12100C))
                .border(1.dp, Color(0xFF2E271D), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ACCOUNT SECURITY",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Delete Account Policy",
                            color = TextWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Please review this policy before deleting your account.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF262118))
                            .clickable(enabled = !isDeleting) { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFF262118), thickness = 1.dp)

                // Scrollable Policy Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "This Delete Account Policy explains how users can request deletion of their DealShot account and what happens to their personal information, subscriptions, viewing history, and other account-related data when an account is deleted.\n\nDealShot is committed to handling account deletion requests in a transparent and secure manner and in accordance with applicable laws and regulations.",
                            color = Color(0xFFC4B8A5),
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp
                        )

                        PolicySectionItem(
                            number = "01",
                            title = "What Happens When You Request Deletion",
                            bullets = listOf(
                                "Your DealShot account will be deactivated or permanently deleted, as applicable.",
                                "You may lose access to your profile and account.",
                                "Your viewing history and watchlist may be deleted.",
                                "Your preferences and personalized recommendations may be deleted or disconnected.",
                                "Saved profiles associated with the account may be deleted.",
                                "Personal information will be deleted or anonymized where applicable.",
                                "You may lose access to previously purchased or subscribed content.",
                                "You may need to create a new account to use DealShot again."
                            )
                        )

                        PolicySectionItem(
                            number = "02",
                            title = "Active Subscriptions",
                            content = "Deleting your DealShot account does not necessarily cancel an active subscription automatically.\n\nBefore deleting your account, cancel any active subscription to prevent future recurring charges.\n\nIf purchased through a third-party provider, you may need to cancel directly through:\n• Google Play\n• Apple App Store\n• Another app store\n• A third-party payment or subscription provider\n\nAccount deletion does not automatically entitle you to a refund. Applicable refunds are handled under the DealShot Refund & Cancellation Policy."
                        )

                        PolicySectionItem(
                            number = "03",
                            title = "Pending Payments and Refunds",
                            content = "If a payment, refund, chargeback, dispute, or other financial transaction is pending, DealShot may retain information necessary to complete or resolve it.\n\nAccount deletion does not cancel:\n• A payment already being processed;\n• A valid refund request;\n• A payment dispute or chargeback;\n• A tax or accounting obligation; or\n• Any financial obligation that arose before deletion."
                        )

                        PolicySectionItem(
                            number = "04",
                            title = "Information That May Be Retained",
                            bullets = listOf(
                                "Compliance with applicable laws and regulations;",
                                "Tax, accounting, and financial record-keeping;",
                                "Fraud prevention and security;",
                                "Prevention or investigation of misuse or illegal activity;",
                                "Resolution of disputes and complaints;",
                                "Enforcement of agreements and legal rights;",
                                "Lawful requests from government or regulatory authorities; or",
                                "Establishing, exercising, or defending legal claims."
                            ),
                            headerPrefixText = "DealShot will take reasonable steps to delete or anonymize personal information that is no longer required. Some information may need to be retained for:",
                            footerSuffixText = "Where retention is necessary, information will be retained only for as long as reasonably required for the applicable purpose, subject to applicable law."
                        )

                        PolicySectionItem(
                            number = "05",
                            title = "Backups",
                            content = "Some information may remain temporarily in secure backup systems after deletion. It will not ordinarily be used for normal account operations and will be removed or overwritten under DealShot's backup and data-retention procedures, subject to legal or security requirements."
                        )

                        PolicySectionItem(
                            number = "06",
                            title = "Anonymized Information",
                            bullets = listOf(
                                "Service analytics;",
                                "Platform improvement;",
                                "Business reporting;",
                                "Audience and content analysis; and",
                                "Security and performance analysis."
                            ),
                            headerPrefixText = "DealShot may retain aggregated or irreversibly anonymized information that can no longer reasonably be associated with you. This may be used for:",
                            footerSuffixText = "Properly anonymized information may not be deleted as part of an individual deletion request where permitted by applicable law."
                        )

                        PolicySectionItem(
                            number = "07",
                            title = "Content and User Activity",
                            bullets = listOf(
                                "Watch history;",
                                "Watchlist;",
                                "Likes and preferences;",
                                "Ratings or reviews, where applicable;",
                                "Profiles;",
                                "Device associations; and",
                                "Personalized recommendations."
                            ),
                            headerPrefixText = "Following account deletion, DealShot may remove or disassociate:",
                            footerSuffixText = "Activity records required for legal, security, fraud-prevention, or dispute-resolution purposes may be retained for the applicable period."
                        )

                        PolicySectionItem(
                            number = "08",
                            title = "User-Generated Content",
                            bullets = listOf(
                                "Delete the content;",
                                "Anonymize the content;",
                                "Remove its association with your account; or",
                                "Retain it where necessary for legal, security, dispute-resolution, or other legitimate purposes."
                            ),
                            headerPrefixText = "Account deletion may not immediately delete every review, comment, rating, feedback item, or other content you previously submitted. Depending on the content and applicable law, DealShot may:"
                        )

                        PolicySectionItem(
                            number = "09",
                            title = "Verification and Security",
                            bullets = listOf(
                                "The request was not made by the account holder;",
                                "The account has been compromised;",
                                "Additional verification is required;",
                                "The request involves fraud or unlawful activity; or",
                                "Retention is required by applicable law."
                            ),
                            headerPrefixText = "DealShot may require reasonable verification before processing a deletion request. A request may be rejected or temporarily delayed if we reasonably believe:"
                        )

                        PolicySectionItem(
                            number = "10",
                            title = "Processing Time",
                            content = "DealShot will process verified deletion requests within a reasonable period, subject to the nature of the request, the amount of information involved, technical requirements, applicable laws, and pending financial, security, or legal matters.\n\nWe may notify you when processing is complete or if additional information is required."
                        )

                        PolicySectionItem(
                            number = "11",
                            title = "Recreating Your Account",
                            content = "Once your account is permanently deleted, DealShot may not be able to restore your previous viewing history, watchlist, preferences, profiles, recommendations, or other account-specific information. You may need to create a new account if you return."
                        )

                        PolicySectionItem(
                            number = "12",
                            title = "Children's Accounts",
                            content = "If DealShot becomes aware that a child's personal information has been collected or processed in violation of applicable requirements, it may take appropriate steps to delete or restrict that information in accordance with applicable law.\n\nParents or legal guardians who believe a child's information has been collected improperly may contact us using the details below."
                        )

                        PolicySectionItem(
                            number = "13",
                            title = "Relationship With Our Privacy Policy",
                            bullets = listOf(
                                "What information DealShot collects;",
                                "How information is used;",
                                "When information may be shared;",
                                "How information is protected;",
                                "Data-retention practices; and",
                                "Your privacy rights and choices."
                            ),
                            headerPrefixText = "This Delete Account Policy should be read together with the DealShot Privacy Policy, which explains:"
                        )

                        PolicySectionItem(
                            number = "14",
                            title = "Withdrawal of Consent",
                            content = "Where personal-data processing is based on consent, you may have the right to withdraw that consent under applicable law. Withdrawal does not affect the lawfulness of processing carried out before withdrawal.\n\nWhere applicable law permits or requires continued processing without consent, it may continue for the relevant purpose."
                        )

                        PolicySectionItem(
                            number = "15",
                            title = "Contact Us",
                            content = "DealShot Support\nEmail: nebulamedia90@gmail.com\n\nPrivacy Team\nEmail: nebulamedia90@gmail.com\n\nGrievance Officer\nEmail: nebulamedia90@gmail.com\nPhone: +91 9989874992\n\nRegistered Address: Plot 94, Trimurthy Colony, Mahendra Hill, Nehrunagar (Hyderabad), Secunderabad, Hyderabad – 500026, Telangana\nCorporate Address: 2nd Floor, H.No.1-10-60 to 62, Ashoka Raghupathi Chambers, S.P. Road, Begumpet Hyderabad, Telangana-500016, India"
                        )

                        PolicySectionItem(
                            number = "16",
                            title = "Changes to This Policy",
                            content = "DealShot may update this Delete Account Policy to reflect changes in its services, technology, legal requirements, or data practices. Updated versions will be published on this page with a revised Last Updated date."
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFF262118), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Agreement Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = !isDeleting) { isAgreed = !isAgreed }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAgreed,
                        onCheckedChange = { isAgreed = it },
                        enabled = !isDeleting,
                        colors = CheckboxDefaults.colors(
                            checkedColor = GoldPrimary,
                            uncheckedColor = TextMuted,
                            checkmarkColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "I have read and agree to the Delete Account Policy.",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorMessage,
                        color = DangerRed,
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF222222))
                            .border(1.dp, Color(0xFF333333), RoundedCornerShape(10.dp))
                            .clickable(enabled = !isDeleting) { onDismiss() }
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = if (isDeleting) TextMuted else TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    val canDelete = isAgreed && !isDeleting
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (canDelete) DangerRed else DangerRed.copy(alpha = 0.35f)
                            )
                            .clickable(enabled = canDelete) { onConfirm() }
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = TextWhite,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = if (canDelete) TextWhite else TextWhite.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "Delete Account",
                                color = if (canDelete) TextWhite else TextWhite.copy(alpha = 0.5f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicySectionItem(
    number: String,
    title: String,
    content: String? = null,
    bullets: List<String> = emptyList(),
    headerPrefixText: String? = null,
    footerSuffixText: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = number,
                color = GoldPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                color = GoldPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (!headerPrefixText.isNullOrBlank()) {
            Text(
                text = headerPrefixText,
                color = Color(0xFFC4B8A5),
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (!content.isNullOrBlank()) {
            Text(
                text = content,
                color = Color(0xFFC4B8A5),
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )
        }

        if (bullets.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                bullets.forEach { bullet ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            color = GoldPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = bullet,
                            color = Color(0xFFC4B8A5),
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        if (!footerSuffixText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = footerSuffixText,
                color = Color(0xFFC4B8A5),
                fontSize = 13.5.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun LogoutConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = GoldPrimary.copy(alpha = 0.25f),
                    ambientColor = Color.Black
                )
                .clip(RoundedCornerShape(24.dp))
                .background(CardSurface)
                .border(1.dp, GoldPrimary, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.12f))
                        .border(2.dp, GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rectangle_portrait_and_arrow_right),
                        contentDescription = "Log out",
                        tint = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Log Out",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Are you sure you want to log out of your DealShot account?",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecondarySurface)
                            .border(1.dp, DividerDark, RoundedCornerShape(12.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(GoldGradient))
                            .clickable { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Log Out",
                            color = Color(0xFF140E02),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Preview to verify UI in Android Studio editor
@Preview(showBackground = true, backgroundColor = 0xFF080807)
@Composable
fun ProfileScreenPreview() {
    val dummyProfiles = listOf(
        ProfileListItem("1", "Casey", null, isPrimary = true),
        ProfileListItem("2", "Kids", null, isPrimary = false)
    )
    ProfileScreen(
        state = ProfileState(
            username = "Casey",
            phoneNumber = "+91 9876543210",
            email = "casey@example.com"
        ),
        onIntent = {},
        profiles = dummyProfiles,
        selectedProfileId = "1",
        email = "casey@example.com",
        subscriptionLabel = "Free"
    )
}
