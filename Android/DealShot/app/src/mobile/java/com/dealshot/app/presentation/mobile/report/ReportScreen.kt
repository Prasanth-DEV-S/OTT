package com.dealshot.app.presentation.mobile.report

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.dealshot.app.data.remote.repository.report.ReportReason
import com.dealshot.app.data.remote.repository.report.ReportRepository
import com.dealshot.app.presentation.shared.report.ReportUiState
import com.dealshot.app.presentation.shared.report.ReportViewModel
import com.dealshot.app.ui.AnimatedGoldenBackground
import com.dealshot.app.ui.theme.CardBgDark
import com.dealshot.app.ui.theme.GoldPrimary
import com.dealshot.app.ui.theme.GoldTextLight
import com.dealshot.app.ui.theme.TextMuted

private val ErrorRed = Color(0xFFFF5252)
private val OnGold = Color(0xFF140E02)
private val CardBorder = Color(0xFF383025)

@Composable
fun ReportRoute(
    videoId: String,
    videoTitle: String,
    thumbnailUrl: String,
    repository: ReportRepository,
    isLoggedIn: () -> Boolean,
    onBack: () -> Unit
) {
    val viewModel: ReportViewModel = viewModel(key = "report_$videoId") {
        ReportViewModel(
            savedStateHandle = createSavedStateHandle(),
            repository = repository,
            videoId = videoId,
            isLoggedIn = isLoggedIn
        )
    }
    val state by viewModel.uiState.collectAsState()

    ReportScreen(
        state = state,
        videoTitle = videoTitle,
        thumbnailUrl = thumbnailUrl,
        onReasonSelected = viewModel::onReasonSelected,
        onRetryReasons = viewModel::loadMasterData,
        onSubmit = viewModel::submit,
        onBack = onBack
    )
}

@Composable
fun ReportScreen(
    state: ReportUiState,
    videoTitle: String,
    thumbnailUrl: String,
    onReasonSelected: (ReportReason) -> Unit,
    onRetryReasons: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    // Don't leave the screen while a request is in flight.
    BackHandler(enabled = state.isSubmitting) { }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedGoldenBackground(modifier = Modifier.matchParentSize())
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            // ---------- Header ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Report Video",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tell us why you are reporting this video.",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ---------- Video info ----------
            VideoInfoCard(title = videoTitle, thumbnailUrl = thumbnailUrl)

            Spacer(modifier = Modifier.height(22.dp))

            // ---------- Reasons ----------
            SectionLabel("WHY ARE YOU REPORTING THIS?")
            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoadingReasons) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GoldPrimary,
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                }
            } else if (state.reasonsError != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF381F19))
                        .border(1.dp, ErrorRed.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.reasonsError,
                        color = Color.White,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRetryReasons) {
                        Text(text = "Retry Loading Reasons", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.reasons.forEach { reason ->
                        ReasonCard(
                            reason = reason,
                            selected = state.selectedReason == reason,
                            enabled = !state.isSubmitting,
                            onClick = { onReasonSelected(reason) }
                        )
                    }
                }
            }
            ValidationMessage(message = state.reasonError)

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Submit error + Retry ----------
            SubmitErrorBanner(
                message = state.submitError,
                retryEnabled = !state.isSubmitting,
                onRetry = onSubmit
            )

            // ---------- Submit ----------
            SubmitButton(
                isSubmitting = state.isSubmitting,
                enabled = !state.isLoadingReasons && state.reasons.isNotEmpty() && !state.isSubmitting,
                onClick = onSubmit
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (state.isSuccess) {
            SuccessDialog(message = state.successMessage, onDone = onBack)
        }
    }
}

// =====================================================================
// Pieces
// =====================================================================

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = GoldTextLight,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun VideoInfoCard(title: String, thumbnailUrl: String) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CardBgDark.copy(alpha = 0.92f))
            .border(1.dp, CardBorder, shape)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(96.dp)
                .aspectRatio(16f / 10f)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1F1A14))
        ) {
            if (thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "You are reporting", color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title.ifBlank { "Untitled video" },
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReasonCard(
    reason: ReportReason,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    val borderColor by animateColorAsState(
        targetValue = if (selected) GoldPrimary else CardBorder,
        animationSpec = tween(200),
        label = "reasonBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF2C2417) else CardBgDark.copy(alpha = 0.9f),
        animationSpec = tween(200),
        label = "reasonBg"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "reasonScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(bgColor)
            .border(if (selected) 1.5.dp else 1.dp, borderColor, shape)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioIndicator(selected = selected)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = reason.label,
            color = if (selected) GoldTextLight else Color.White,
            fontSize = 14.5.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Composable
private fun RadioIndicator(selected: Boolean) {
    val ringColor by animateColorAsState(
        targetValue = if (selected) GoldPrimary else TextMuted,
        animationSpec = tween(200),
        label = "radioRing"
    )
    val dotScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "radioDot"
    )
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(2.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(dotScale)
                .background(GoldPrimary, CircleShape)
        )
    }
}

/** Fades/expands in when [message] is non-null; keeps the last text while animating out. */
@Composable
private fun ValidationMessage(message: String?) {
    var last by remember { mutableStateOf("") }
    if (message != null) last = message

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = last, color = ErrorRed, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SubmitErrorBanner(
    message: String?,
    retryEnabled: Boolean,
    onRetry: () -> Unit
) {
    var last by remember { mutableStateOf("") }
    if (message != null) last = message

    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val shape = RoundedCornerShape(12.dp)
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Color(0xFF381F19))
                    .border(1.dp, ErrorRed.copy(alpha = 0.6f), shape)
                    .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = last,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onRetry, enabled = retryEnabled) {
                    Text(text = "Retry", color = GoldPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun SubmitButton(
    isSubmitting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "submitScale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GoldPrimary,
            contentColor = OnGold,
            disabledContainerColor = GoldPrimary.copy(alpha = 0.55f),
            disabledContentColor = OnGold
        )
    ) {
        Crossfade(targetState = isSubmitting, label = "submitContent") { submitting ->
            if (submitting) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OnGold,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Submitting…", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                Text(text = "Submit Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun SuccessDialog(message: String, onDone: () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        var appeared by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { appeared = true }
        val checkScale by animateFloatAsState(
            targetValue = if (appeared) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "successCheck"
        )

        val shape = RoundedCornerShape(20.dp)
        Column(
            modifier = Modifier
                .clip(shape)
                .background(Color(0xFF1A150E))
                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), shape)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(checkScale)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2417))
                    .border(1.5.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Report Submitted",
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message.ifBlank { "Thank you. Your report has been submitted successfully." },
                color = TextMuted,
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Thanks, we will review this video.",
                color = TextMuted,
                fontSize = 13.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = OnGold
                )
            ) {
                Text(text = "Done", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
