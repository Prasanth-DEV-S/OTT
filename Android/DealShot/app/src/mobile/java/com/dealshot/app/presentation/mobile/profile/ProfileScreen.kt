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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
            Column(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Centered Top Header
                ProfileTopBar(onBack = { onIntent(ProfileIntent.BackClicked) })

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
                    profileError = state.error
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
    LaunchedEffect(isSaving) {
        if (wasSaving && !isSaving && saveError == null) isEditing = false
        wasSaving = isSaving
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
                    onCancel = { isEditing = false }
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

@Composable
private fun AccountDetailsSection(
    formattedMobile: String,
    email: String,
    gender: String,
    language: String,
    subscriptionLabel: String,
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

        AccountDetailRow(
            label = "Language",
            value = language.ifBlank { "English" },
            icon = {
                Icon(Icons.Default.Language, "Language", tint = GoldPrimary, modifier = Modifier.size(18.dp))
            }
        )
        HorizontalDivider(color = DividerDark, thickness = 1.dp)

        AccountDetailRow(
            label = "Subscription",
            value = subscriptionLabel,
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isDeleting,
            dismissOnClickOutside = !isDeleting
        )
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(CardSurface)
                .border(1.dp, DangerRed, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DangerRed.copy(alpha = 0.12f))
                        .border(2.dp, DangerRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Delete Account",
                    color = TextWhite,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This permanently deletes your DealShot account and its data. This can't be undone.",
                    color = TextMuted,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        color = DangerRed,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

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
                            .clickable(enabled = !isDeleting) { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Cancel",
                            color = if (isDeleting) TextMuted else TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDeleting) DangerRed.copy(alpha = 0.55f) else DangerRed)
                            .clickable(enabled = !isDeleting) { onConfirm() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Delete",
                                color = TextWhite,
                                fontSize = 15.sp,
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
