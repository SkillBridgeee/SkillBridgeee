package com.android.sample

import android.content.Intent
import android.util.Log
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.FirestoreProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.bookings.MyBookingsScreen
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.android.sample.ui.signup.SignUpScreen
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.android.sample.ui.theme.SampleAppTheme
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import okhttp3.internal.wait
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Helpers (inspired by SignUpScreenTest)


private const val DEFAULT_TIMEOUT_MS = 10_000L // Reduced from 30_000

private fun waitForTag(
    rule: ComposeContentTestRule,
    tag: String,
    timeoutMs: Long = DEFAULT_TIMEOUT_MS
) {
    rule.waitUntil(timeoutMs) {
        rule.onAllNodes(hasTestTag(tag), useUnmergedTree = false).fetchSemanticsNodes().isNotEmpty()
    }
}

private fun waitForText(
    rule: ComposeContentTestRule,
    tag: String,
    timeoutMs: Long = DEFAULT_TIMEOUT_MS
) {
    rule.waitUntil(timeoutMs) {
        rule.onAllNodes(hasText(tag), useUnmergedTree = false).fetchSemanticsNodes().isNotEmpty()
    }
}

private fun ComposeContentTestRule.nodeByTag(tag: String) =
    onNodeWithTag(tag, useUnmergedTree = false)

private fun ComposeContentTestRule.nodeByText(text: String) =
    onNodeWithText(text, useUnmergedTree = false)


@RunWith(AndroidJUnit4::class)
class EndToEndM2 {


    private lateinit var auth: FirebaseAuth

    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {

        // Connect to Firebase emulators
        try {
            Firebase.firestore.useEmulator("10.0.2.2", 8080)
            Firebase.auth.useEmulator("10.0.2.2", 9099)
        } catch (_: IllegalStateException) {
            // Emulator already initialized
        }

        auth = Firebase.auth

        // Initialize ProfileRepositoryProvider with real Firestore
        ProfileRepositoryProvider.setForTests(FirestoreProfileRepository(Firebase.firestore))

        // Clean up any existing user before starting
        auth.signOut()
    }

    @After
    fun tearDown() {
        // Clean up: delete the test user if created
        try {
            auth.currentUser?.delete()
        } catch (_: Exception) {
            // Ignore deletion errors
        }
        auth.signOut()
    }

    @Test
    fun userSignsIn(){

        val testEmail = "guillaume.lepinus@epfl.ch"
        val testPassword = "testPassword123!"

        waitForTag(compose, SignInScreenTestTags.SIGN_IN_BUTTON)

        // Create user
        compose.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK)
            .assertIsDisplayed().performClick()

        waitForTag(compose, SignUpScreenTestTags.NAME)

        // Fill sign-up form

        compose.onNodeWithTag(SignUpScreenTestTags.NAME)
            .assertIsDisplayed()
            .performClick()
            .performTextInput("Lepin")
        compose.onNodeWithTag(SignUpScreenTestTags.SURNAME)
            .assertIsDisplayed()
            .performClick()
            .performTextInput("Guillaume")
        compose
            .onNodeWithTag(LocationInputFieldTestTags.INPUT_LOCATION, useUnmergedTree = true)
            .performTextInput("London Street 1")
        compose.onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION)
            .assertIsDisplayed()
            .performClick()
            .performTextInput("CS, 3rd year")
        compose.onNodeWithTag(SignUpScreenTestTags.DESCRIPTION)
            .assertIsDisplayed()
            .performClick()
            .performTextInput("Gay")

        compose.onNodeWithTag(SignUpScreenTestTags.EMAIL)
            .assertIsDisplayed()
            .performClick()
            .performTextInput(testEmail)
        compose.onNodeWithTag(SignUpScreenTestTags.PASSWORD)
            .assertIsDisplayed()
            .performClick()
            .performTextInput(testPassword)

        compose.onNodeWithTag(SignUpScreenTestTags.PASSWORD)
            .performImeAction()

        compose.waitForIdle()

        compose.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertIsEnabled()
        compose.onNodeWithTag(SignUpScreenTestTags.SIGN_UP)
            .performScrollTo().performClick()

        // Wait for navigation to home screen

        compose.onNodeWithContentDescription("Back").performClick()
        waitForTag(compose, SignInScreenTestTags.SIGN_IN_BUTTON)

        // Now sign in with the created user
        compose.onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT)
            .assertIsDisplayed()
            .performClick()
            .performTextInput(testEmail)

        compose.onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT)
            .assertIsDisplayed()
            .performClick()
            .performTextInput(testPassword)

        compose.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON)
            .assertIsEnabled()
            .performClick()

        // Verify navigation to home screen
        waitForTag(compose, HomeScreenTestTags.WELCOME_SECTION)
        compose.onNodeWithTag(HomeScreenTestTags.WELCOME_SECTION)
            .assertIsDisplayed()

        // Go to my profile
        compose.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE)
            .assertIsDisplayed()
            .performClick()

        waitForTag(compose, MyProfileScreenTestTag.PROFILE_ICON)
        compose.onNodeWithTag(MyProfileScreenTestTag.PROFILE_ICON)
            .assertIsDisplayed()


        waitForTag(compose, MyProfileScreenTestTag.INPUT_PROFILE_NAME)
        waitForText(compose, "Lepin Guillaume")

        compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_NAME)
            .assertIsDisplayed()
            .assertTextContains("Lepin Guillaume")

        compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
            .assertIsDisplayed()
            .assertTextContains("Gay")

        compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON)
            .assertIsNotEnabled()

        compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
            .assertIsDisplayed()
            .performClick()
            .performTextInput(" Man")

        compose.onNodeWithTag(MyProfileScreenTestTag.SAVE_BUTTON)
            .assertIsEnabled()
            .performClick()

        waitForText(compose, "Gay Man")
        compose.onNodeWithTag(MyProfileScreenTestTag.INPUT_PROFILE_DESC)
            .assertIsDisplayed()
            .assertTextContains("Gay Man")


        compose.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME)
            .assertIsDisplayed()
            .performClick()

        waitForTag(compose, HomeScreenTestTags.WELCOME_SECTION)


    }







}