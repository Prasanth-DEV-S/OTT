package com.dealshot.app.presentation.tv.subscription

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dealshot.app.presentation.shared.subscription.SubscriptionEffect
import com.dealshot.app.presentation.shared.subscription.SubscriptionIntent
import com.dealshot.app.presentation.shared.subscription.SubscriptionViewModel

private val Purple = Color(0xFF8B5CF6)

/**
 * Same flow as SubscriptionActivity:
 *  NavigateBack      -> close
 *  ShowSuccessDialog -> success dialog; Continue sends SuccessDialogDismissed, then close
 *  ShowError         -> toast (mobile left this empty)
 *
 * title / description / url are accepted for parity with the mobile route;
 * the mobile activity also just closes after success.
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun TvSubscriptionRoute(
    title: String,
    description: String,
    url: String,
    onFinished: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SubscriptionEffect.NavigateBack -> onFinished()
                SubscriptionEffect.ShowSuccessDialog -> showSuccessDialog = true
                is SubscriptionEffect.ShowError -> {
                    Toast.makeText(
                        context,
                        "Something went wrong. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    TvSubscriptionScreen(
        state = state,
        onIntent = viewModel::onIntent
    )

    if (showSuccessDialog) {
        TvSuccessDialog(
            onContinue = {
                showSuccessDialog = false
                viewModel.onIntent(SubscriptionIntent.SuccessDialogDismissed)
                onFinished()
            }
        )
    }
}

/** Compose replacement for R.layout.dialog_subscription_success (not D-pad focusable). */
@Composable
private fun TvSuccessDialog(onContinue: () -> Unit) {
    val focus = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E1E1E))
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Subscription activated",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enjoy unlimited watching.",
                color = Color(0xFFBDBDBD),
                fontSize = 18.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Continue",
                color = if (focused) Color.Black else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .focusRequester(focus)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (focused) Purple else Color(0xFF333333))
                    .border(
                        2.dp,
                        if (focused) Color.White else Color.Transparent,
                        RoundedCornerShape(14.dp)
                    )
                    .onFocusChanged { focused = it.isFocused }
                    .clickable(onClick = onContinue)
                    .padding(horizontal = 40.dp, vertical = 14.dp)
            )
        }
    }
}
