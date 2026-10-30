package com.example.stream.mobile

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dealshot.app.presentation.mobile.login.LoginScreen
import com.dealshot.app.presentation.shared.login.LoginIntent
import com.dealshot.app.presentation.shared.login.LoginState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for [LoginScreen].
 *
 * Run as an instrumented test (androidTest) on a device or emulator:
 *   ./gradlew connectedAndroidTest
 *
 * The screen is stateless, so each test renders it with a fixed [LoginState]
 * and records every [LoginIntent] it emits.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val intents = mutableListOf<LoginIntent>()

    @Before
    fun setUp() {
        intents.clear()
    }

    private fun setScreen(state: LoginState) {
        composeRule.setContent {
            LoginScreen(
                state = state,
                onIntent = { intents.add(it) }
            )
        }
    }

    // ───────────────────────────── Static content ─────────────────────────────

    @Test
    fun showsBrandAndSignInText() {
        setScreen(LoginState(phoneNumber = ""))

        composeRule.onNodeWithText("DealShot").assertExists()
        composeRule.onNodeWithText("sign in to watch premium stories ").assertExists()
    }

    @Test
    fun showsMobileNumberFieldWithCountryCodeAndHelperText() {
        setScreen(LoginState(phoneNumber = ""))

        composeRule.onNodeWithText("Mobile Number").assertExists()
        composeRule.onNodeWithText("+91").assertExists()
        composeRule.onNodeWithText("10-digit mobile number").assertExists()
    }

    // ───────────────────────────── Digit counter ─────────────────────────────

    @Test
    fun counterShowsZeroWhenEmpty() {
        setScreen(LoginState(phoneNumber = ""))

        composeRule.onNodeWithText("0/10").assertExists()
    }

    @Test
    fun counterReflectsEnteredDigits() {
        setScreen(LoginState(phoneNumber = "98765"))

        composeRule.onNodeWithText("5/10").assertExists()
    }

    @Test
    fun counterShowsTenOfTenForFullNumber() {
        setScreen(LoginState(phoneNumber = "9876543210"))

        composeRule.onNodeWithText("10/10").assertExists()
    }

    // ───────────────────────────── Continue button state ─────────────────────────────

    @Test
    fun continueButtonDisabledWhenNumberEmpty() {
        setScreen(LoginState(phoneNumber = ""))

        composeRule.onNodeWithText("Continue").assertIsNotEnabled()
    }

    @Test
    fun continueButtonDisabledWhenNumberShorterThanTenDigits() {
        setScreen(LoginState(phoneNumber = "987654321"))

        composeRule.onNodeWithText("Continue").assertIsNotEnabled()
    }

    @Test
    fun continueButtonEnabledWhenNumberHasTenDigits() {
        setScreen(LoginState(phoneNumber = "9876543210"))

        composeRule.onNodeWithText("Continue").assertIsEnabled()
    }

    // ───────────────────────────── Loading state ─────────────────────────────

    @Test
    fun loadingStateShowsLoadingTextAndHidesContinue() {
        setScreen(LoginState(phoneNumber = "9876543210", isLoading = true))

        composeRule.onNodeWithText("Loading...").assertExists()
        composeRule.onNodeWithText("Continue").assertDoesNotExist()
    }

    @Test
    fun loadingStateDisablesButtonEvenWithValidNumber() {
        setScreen(LoginState(phoneNumber = "9876543210", isLoading = true))

        composeRule.onNodeWithText("Loading...").assertIsNotEnabled()
    }

    @Test
    fun loadingStateDisablesPhoneField() {
        setScreen(LoginState(phoneNumber = "9876543210", isLoading = true))

        composeRule.onNode(hasSetTextAction()).assertIsNotEnabled()
    }

    // ───────────────────────────── Error message ─────────────────────────────

    @Test
    fun errorMessageIsShownWhenPresent() {
        setScreen(LoginState(phoneNumber = "9876543210", error = "Something went wrong"))

        composeRule.onNodeWithText("Something went wrong").assertExists()
    }

    @Test
    fun errorMessageIsNotShownWhenNull() {
        setScreen(LoginState(phoneNumber = "9876543210", error = null))

        composeRule.onNodeWithText("Something went wrong").assertDoesNotExist()
    }

    // ───────────────────────────── Intents ─────────────────────────────

    @Test
    fun typingDigitsEmitsPhoneChangedIntent() {
        setScreen(LoginState(phoneNumber = ""))

        composeRule.onNode(hasSetTextAction()).performTextInput("987")

        assertEquals(listOf<LoginIntent>(LoginIntent.PhoneChanged("987")), intents)
    }

    @Test
    fun clickingEnabledContinueEmitsContinueClicked() {
        setScreen(LoginState(phoneNumber = "9876543210"))

        composeRule.onNodeWithText("Continue").performClick()

        assertEquals(listOf<LoginIntent>(LoginIntent.ContinueClicked), intents)
    }

    @Test
    fun clickingDisabledContinueEmitsNothing() {
        setScreen(LoginState(phoneNumber = "12345"))

        composeRule.onNodeWithText("Continue").performClick()

        assertTrue(intents.isEmpty())
    }

    @Test
    fun imeDoneWithValidNumberEmitsContinueClicked() {
        setScreen(LoginState(phoneNumber = "9876543210"))

        composeRule.onNode(hasSetTextAction()).performImeAction()

        assertEquals(listOf<LoginIntent>(LoginIntent.ContinueClicked), intents)
    }

    @Test
    fun imeDoneWithIncompleteNumberEmitsNothing() {
        setScreen(LoginState(phoneNumber = "12345"))

        composeRule.onNode(hasSetTextAction()).performImeAction()

        assertTrue(intents.isEmpty())
    }
}