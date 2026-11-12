package com.dealshot.app.presentation.mobile.subscription

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.dealshot.app.R
import com.dealshot.app.presentation.shared.subscription.SubscriptionEffect
import com.dealshot.app.presentation.shared.subscription.SubscriptionIntent
import com.dealshot.app.presentation.shared.subscription.SubscriptionViewModel
import kotlinx.coroutines.launch

class SubscriptionActivity : ComponentActivity() {

    private val viewModel: SubscriptionViewModel by viewModels()

    private var title: String = ""
    private var description: String = ""
    private var url: String = ""

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        title =
            intent.getStringExtra("title") ?: ""

        description =
            intent.getStringExtra("description") ?: ""

        url =
            intent.getStringExtra("url") ?: ""

        observeEffects()

        setContent {

            val state =
                viewModel.state.collectAsStateWithLifecycle()

            MobileSubscriptionScreen(
                state = state.value,
                onIntent = viewModel::onIntent
            )
        }

        com.dealshot.app.ui.GoldenBackgroundHelper.apply(this)
    }

    private fun observeEffects() {

        lifecycleScope.launch {

            viewModel.effect.collect { effect ->

                when (effect) {

                    SubscriptionEffect.NavigateBack -> {

                        finish()
                    }

                    SubscriptionEffect.ShowSuccessDialog -> {

                        showSubscriptionSuccessDialog {

                            viewModel.onIntent(
                                SubscriptionIntent.SuccessDialogDismissed
                            )

                            openPremiumPlayer()
                        }
                    }

                    is SubscriptionEffect.ShowError -> {

                        // Show Toast/Snackbar if required.
                    }
                }
            }
        }
    }

    private fun showSubscriptionSuccessDialog(
        onContinue: () -> Unit
    ) {

        val dialog = Dialog(this)

        dialog.requestWindowFeature(
            Window.FEATURE_NO_TITLE
        )

        dialog.setContentView(
            R.layout.dialog_subscription_success
        )

        dialog.setCancelable(false)

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        val btnContinue =
            dialog.findViewById<TextView>(
                R.id.btn_continue
            )

        btnContinue.setOnClickListener {

            dialog.dismiss()

            onContinue()
        }

        dialog.show()
    }

    private fun openPremiumPlayer() {

        finish()
    }
}