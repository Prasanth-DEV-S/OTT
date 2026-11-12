package com.dealshot.app.presentation.mobile.profile

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dealshot.app.R
import com.dealshot.app.presentation.shared.profile.ProfileFormMode
import com.dealshot.app.ui.AnimatedGoldenBackground
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.CardBorderBottom
import com.dealshot.app.ui.theme.CardBorderTop
import com.dealshot.app.ui.theme.DeepBlackBg
import com.dealshot.app.ui.theme.FieldPlaceholder
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.TextMuted

private val DangerRed = Color(0xFFFF5252)
private val FormBorder = Color(0xFF383025)
private val FormBg = Color(0xFF1B1711)
private val OnGold = Color(0xFF140E02)
private val GoldGradient = listOf(Color(0xFFE8C575), Color(0xFFC9942C), Color(0xFF9E6B15))
private val GenderOptions = listOf("Male", "Female", "Other")

/**
 * Reusable Profile Form screen supporting both CREATE and EDIT modes.
 */
@Composable
fun CreateProfileScreen(
    mode: ProfileFormMode = ProfileFormMode.CREATE,
    isDefaultProfile: Boolean = false,
    isEmbedded: Boolean = false,
    initialName: String = "",
    initialEmail: String = "",
    initialGender: String = "",
    initialAvatarUrl: String? = null,
    avatarUrls: List<String> = emptyList(),
    accountEmail: String = "",
    mobileNumber: String = "",
    isSubmitting: Boolean = false,
    submitError: String? = null,
    onSubmit: (avatarUrl: String?, name: String, email: String, gender: String) -> Unit,
    onBack: () -> Unit
) {
    if (!isEmbedded) {
        BackHandler(enabled = isSubmitting) { }
    }

    val focusManager = LocalFocusManager.current

    val initialAvatarIdx = remember(initialAvatarUrl, avatarUrls) {
        if (!initialAvatarUrl.isNullOrBlank() && avatarUrls.isNotEmpty()) {
            val idx = avatarUrls.indexOfFirst { it == initialAvatarUrl }
            if (idx >= 0) idx else 0
        } else 0
    }

    var avatarIndex by rememberSaveable(initialAvatarIdx) { mutableStateOf(initialAvatarIdx) }
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var email by rememberSaveable(initialEmail, accountEmail) {
        mutableStateOf(initialEmail.ifBlank { accountEmail })
    }
    var gender by rememberSaveable(initialGender) { mutableStateOf(initialGender) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val avatarCount = avatarUrls.size
    val safeIndex = if (avatarCount == 0) 0 else avatarIndex.mod(avatarCount)

    val trimmedName = name.trim()
    val trimmedEmail = email.trim()
    val nameError = when {
        trimmedName.isEmpty() -> "Please enter a name."
        trimmedName.length < 2 -> "Name must be at least 2 characters."
        else -> null
    }
    val emailError =
        if (trimmedEmail.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            "Enter a valid email address."
        } else null

    val submit: () -> Unit = {
        if (!isSubmitting) {
            val isEditMode = mode == ProfileFormMode.EDIT
            val requiresEmail = isEditMode && isDefaultProfile

            val hasNameError = nameError != null
            val hasEmailError = if (requiresEmail) (email.isEmpty() || emailError != null) else false

            if (hasNameError || hasEmailError) {
                showErrors = true
            } else {
                showErrors = false
                focusManager.clearFocus()
                onSubmit(avatarUrls.getOrNull(safeIndex), trimmedName, trimmedEmail, gender)
            }
        }
    }

    val formattedMobile = when {
        mobileNumber.isBlank() -> "Not available"
        mobileNumber.startsWith("+") -> mobileNumber
        else -> "+91 $mobileNumber"
    }

    val isEditMode = mode == ProfileFormMode.EDIT
    val badgeText = if (isEditMode) "EDIT PROFILE" else "NEW PROFILE"
    val titleText = if (isEditMode) "Edit Profile" else "Create Profile"
    val subtitleText = when {
        !isEditMode -> "Add a new profile to your account"
        isDefaultProfile -> "Update your account details"
        else -> "Update your profile details"
    }
    val buttonText = if (isEditMode) "Save Changes" else "Create Profile"

    if (isEmbedded) {
        ProfileFormCard(
            isEditMode = isEditMode,
            isDefaultProfile = isDefaultProfile,
            badgeText = badgeText,
            titleText = titleText,
            subtitleText = subtitleText,
            buttonText = buttonText,
            avatarUrls = avatarUrls,
            safeIndex = safeIndex,
            avatarCount = avatarCount,
            isSubmitting = isSubmitting,
            submitError = submitError,
            name = name,
            email = email,
            gender = gender,
            formattedMobile = formattedMobile,
            nameError = nameError,
            emailError = emailError,
            showErrors = showErrors,
            focusManager = focusManager,
            onNameChange = { name = it.take(30) },
            onEmailChange = { email = it.take(100) },
            onGenderSelected = { gender = it },
            onPreviousAvatar = { if (avatarCount > 0) avatarIndex = (safeIndex - 1 + avatarCount) % avatarCount },
            onNextAvatar = { if (avatarCount > 0) avatarIndex = (safeIndex + 1) % avatarCount },
            onSubmit = submit,
            onCancel = onBack
        )
    } else {
        Box(modifier = Modifier.fillMaxSize().background(DeepBlackBg)) {
            AnimatedGoldenBackground(modifier = Modifier.matchParentSize())
            Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.8f)))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileTopBar(onBack = onBack, enabled = !isSubmitting)

                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileFormCard(
                        isEditMode = isEditMode,
                        isDefaultProfile = isDefaultProfile,
                        badgeText = badgeText,
                        titleText = titleText,
                        subtitleText = subtitleText,
                        buttonText = buttonText,
                        avatarUrls = avatarUrls,
                        safeIndex = safeIndex,
                        avatarCount = avatarCount,
                        isSubmitting = isSubmitting,
                        submitError = submitError,
                        name = name,
                        email = email,
                        gender = gender,
                        formattedMobile = formattedMobile,
                        nameError = nameError,
                        emailError = emailError,
                        showErrors = showErrors,
                        focusManager = focusManager,
                        onNameChange = { name = it.take(30) },
                        onEmailChange = { email = it.take(100) },
                        onGenderSelected = { gender = it },
                        onPreviousAvatar = { if (avatarCount > 0) avatarIndex = (safeIndex - 1 + avatarCount) % avatarCount },
                        onNextAvatar = { if (avatarCount > 0) avatarIndex = (safeIndex + 1) % avatarCount },
                        onSubmit = submit,
                        onCancel = onBack
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ProfileFormCard(
    isEditMode: Boolean,
    isDefaultProfile: Boolean,
    badgeText: String,
    titleText: String,
    subtitleText: String,
    buttonText: String,
    avatarUrls: List<String>,
    safeIndex: Int,
    avatarCount: Int,
    isSubmitting: Boolean,
    submitError: String?,
    name: String,
    email: String,
    gender: String,
    formattedMobile: String,
    nameError: String?,
    emailError: String?,
    showErrors: Boolean,
    focusManager: FocusManager,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onGenderSelected: (String) -> Unit,
    onPreviousAvatar: () -> Unit,
    onNextAvatar: () -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val cardShape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 24.dp,
                shape = cardShape,
                spotColor = GoldPrimary.copy(alpha = 0.25f),
                ambientColor = GoldPrimary.copy(alpha = 0.15f)
            )
            .clip(cardShape)
            .background(CardBgDark.copy(alpha = 0.95f))
            .border(
                1.dp,
                Brush.verticalGradient(listOf(CardBorderTop, CardBorderBottom)),
                cardShape
            )
            .padding(20.dp)
    ) {
        Text(
            text = badgeText,
            color = FieldPlaceholder,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = titleText,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitleText,
            color = TextMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ALL PROFILES (Avatar Selection & Profile Name)
        AvatarPicker(
            avatarUrls = avatarUrls,
            index = safeIndex,
            enabled = !isSubmitting,
            onPrevious = onPreviousAvatar,
            onNext = onNextAvatar
        )

        Spacer(modifier = Modifier.height(20.dp))

        FieldLabel("Profile Name")
        CreateProfileTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = "Enter profile name",
            enabled = !isSubmitting,
            errorText = if (showErrors) nameError else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditMode && isDefaultProfile) {
            // DEFAULT PROFILE EDIT ONLY (Email Address & Gender)
            FieldLabel("Email Address")
            CreateProfileTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Enter your email address",
                enabled = !isSubmitting,
                errorText = if (showErrors) emailError else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Gender")
            GenderDropdown(
                selected = gender,
                enabled = !isSubmitting,
                onSelected = onGenderSelected
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2B2620)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = GoldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Mobile Number", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedMobile,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This number is linked to your account and cannot be changed.",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        SubmitErrorBanner(
            message = submitError,
            retryEnabled = !isSubmitting,
            onRetry = onSubmit
        )

        if (submitError != null) {
            Spacer(modifier = Modifier.height(16.dp))
        }

        CreateProfileButton(
            label = buttonText,
            enabled = !isSubmitting,
            isSubmitting = isSubmitting,
            onClick = onSubmit
        )

        Spacer(modifier = Modifier.height(12.dp))

        CancelButton(
            enabled = !isSubmitting,
            onClick = onCancel
        )
    }
}

@Composable
private fun AvatarPicker(
    avatarUrls: List<String>,
    index: Int,
    enabled: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val count = avatarUrls.size
    val currentUrl = avatarUrls.getOrNull(index)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CHOOSE AVATAR",
            color = GoldTextLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B2620))
                    .clickable(enabled = enabled && count > 1, onClick = onPrevious),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous avatar",
                    tint = if (count > 1) GoldPrimary else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Box(
                modifier = Modifier
                    .size(104.dp)
                    .border(3.dp, GoldPrimary, CircleShape)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Crossfade(targetState = currentUrl, label = "AvatarCrossfade") { url ->
                    ProfileAvatar(url = url, size = 90.dp)
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B2620))
                    .clickable(enabled = enabled && count > 1, onClick = onNext),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next avatar",
                    tint = if (count > 1) GoldPrimary else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (count > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until count) {
                    val active = i == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (active) 18.dp else 6.dp,
                        label = "dotWidth"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (active) GoldPrimary else Color.White.copy(alpha = 0.35f),
                        label = "dotColor"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(3.dp))
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = GoldTextLight,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun CreateProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    errorText: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = when {
        errorText != null -> DangerRed
        focused -> GoldPrimary
        else -> FormBorder
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(FormBg)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(GoldPrimary),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focused = it.isFocused },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = FieldPlaceholder,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = errorText != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = errorText.orEmpty(),
                color = DangerRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )
        }
    }
}

@Composable
private fun GenderDropdown(
    selected: String,
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selected.ifBlank { "Select gender" }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(FormBg)
                .border(1.dp, FormBorder, RoundedCornerShape(14.dp))
                .clickable(enabled = enabled) { expanded = true }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = displayText,
                color = if (selected.isBlank()) FieldPlaceholder else Color.White,
                fontSize = 15.sp
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand gender menu",
                tint = GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBgDark)
        ) {
            GenderOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option, color = Color.White) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CreateProfileButton(
    label: String = "Create Profile",
    enabled: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = shape,
        colors = ButtonDefaults.textButtonColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(Brush.horizontalGradient(GoldGradient)),
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = OnGold,
                    strokeWidth = 2.5.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = OnGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = OnGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = OnGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CancelButton(enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cancel",
            color = if (enabled) TextMuted else TextMuted.copy(alpha = 0.5f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SubmitErrorBanner(
    message: String?,
    retryEnabled: Boolean,
    onRetry: () -> Unit
) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        if (message != null) {
            val shape = RoundedCornerShape(14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Color(0xFF381F19))
                    .border(1.dp, DangerRed.copy(alpha = 0.6f), shape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                if (retryEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Retry",
                        color = GoldTextLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onRetry)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
