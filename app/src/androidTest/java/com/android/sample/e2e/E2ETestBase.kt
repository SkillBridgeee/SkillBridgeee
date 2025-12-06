package com.android.sample.e2e

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.android.sample.MainActivity
import com.android.sample.e2e.E2ETestHelper.forceEmailVerification
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay

/**
 * Base class for E2E tests that provides common setup and initialization.
 *
 * This class handles the common flow:
 * 1. Login screen verification
 * 2. UI-based Sign-Up with test email (@example.test)
 * 3. Email verification via Cloud Function
 * 4. UI-based Sign-In
 * 5. Navigation to home screen
 *
 * After calling `initializeUserAndNavigateToHome()`, the test will be on the home screen with an
 * authenticated user, ready to test specific user flows.
 */
abstract class E2ETestBase {

  companion object {
    protected const val BASE_TAG = "E2ETestBase"
    private const val TEST_PASSWORD = "TestPassword123!"

    @JvmStatic
    protected fun generateTestEmail() = "e2e.test.${System.currentTimeMillis()}@example.test"
  }

  protected var testUser: FirebaseUser? = null
  protected var testEmail: String = ""

  /**
   * Initializes a test user, creates their profile, and navigates to the home screen. This method
   * handles the complete UI-based login flow.
   *
   * @param composeTestRule The compose test rule
   * @param userName The first name for the test user (default: "E2E")
   * @param userSurname The surname for the test user (default: "Test")
   * @return The authenticated FirebaseUser
   */
  protected suspend fun initializeUserAndNavigateToHome(
      composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
      userName: String = "E2E",
      userSurname: String = "Test"
  ): FirebaseUser {
    val tag = "${BASE_TAG}:Init"

    Log.d(tag, "\n╔═══════════════════════════════════════════════════════╗")
    Log.d(tag, "║   INITIALIZING E2E TEST USER AND NAVIGATION          ║")
    Log.d(tag, "╚═══════════════════════════════════════════════════════╝\n")

    // ═══════════════════════════════════════════════════════════
    // STEP 1: Verify Login Screen
    // ═══════════════════════════════════════════════════════════
    Log.d(tag, "STEP 1: Verifying login screen is displayed")
    composeTestRule.waitForIdle()
    delay(1000)

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.TITLE)
        .assertExists("Login screen title should exist")
        .assertIsDisplayed()

    Log.d(tag, "✅ STEP 1 PASSED: Login screen displayed\n")

    // ═══════════════════════════════════════════════════════════
    // STEP 2: Navigate to Sign Up Screen
    // ═══════════════════════════════════════════════════════════
    Log.d(tag, "STEP 2: Navigating to sign up screen via UI")

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK)
        .assertExists("Sign up link should exist")
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()
    delay(1000)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.TITLE)
        .assertExists("Sign up screen title should exist")
        .assertIsDisplayed()

    Log.d(tag, "✅ STEP 2 PASSED: Sign up screen displayed\n")

    // ═══════════════════════════════════════════════════════════
    // STEP 3: Fill in Sign Up Form
    // ═══════════════════════════════════════════════════════════
    Log.d(tag, "STEP 3: Filling in sign up form with test credentials")

    // Enter name
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.NAME)
        .assertExists()
        .performTextInput(userName)
    delay(300)

    // Enter surname
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SURNAME)
        .assertExists()
        .performTextInput(userSurname)
    delay(300)

    // Enter email (using test domain)
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL)
        .assertExists()
        .performTextInput(testEmail)
    delay(300)

    // Enter password
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD)
        .assertExists()
        .performTextInput(TEST_PASSWORD)
    delay(300)

    // Enter location - type "epfl" and select first suggestion
    composeTestRule
        .onNodeWithTag(com.android.sample.ui.components.LocationInputFieldTestTags.INPUT_LOCATION)
        .assertExists()
        .performTextInput("epfl")
    delay(5000) // Wait for suggestions to appear

    // Click the first location suggestion
    composeTestRule
        .onAllNodesWithTag(com.android.sample.ui.components.LocationInputFieldTestTags.SUGGESTION)
        .onFirst()
        .performClick()
    delay(300)

    // Enter education level
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION)
        .assertExists()
        .performTextInput("CS, 3rd year")
    delay(300)

    // Scroll to and check ToS checkbox
    try {
      composeTestRule.onNodeWithTag(SignUpScreenTestTags.TOS_CHECKBOX).performScrollTo()
    } catch (@Suppress("SwallowedException") _: Exception) {
      Log.d(tag, "→ ToS checkbox already visible, no scroll needed")
    }

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.TOS_CHECKBOX).assertExists().performClick()
    delay(500)

    Log.d(tag, "→ Entered credentials:")
    Log.d(tag, "  Name: $userName $userSurname")
    Log.d(tag, "  Email: $testEmail")
    Log.d(tag, "  Location: epfl")
    Log.d(tag, "  Education: CS, 3rd year")
    Log.d(tag, "✅ STEP 3 PASSED: Sign up form filled\n")

    // ═══════════════════════════════════════════════════════════
    // STEP 4: Submit Sign Up Form
    // ═══════════════════════════════════════════════════════════
    Log.d(tag, "STEP 4: Submitting sign up form")

    composeTestRule.waitForIdle()
    delay(500)

    // Scroll to sign up button
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertExists().performScrollTo()
    delay(500)
    composeTestRule.waitForIdle()

    // Click the sign up button
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertIsDisplayed().performClick()

    Log.d(tag, "→ Sign up button clicked")
    delay(2000)
    composeTestRule.waitForIdle()

    Log.d(tag, "✅ STEP 4 PASSED: Sign up submitted successfully\n")

    // ═══════════════════════════════════════════════════════════
    // STEP 5: Force Email Verification via Cloud Function
    // ═══════════════════════════════════════════════════════════
    Log.d(tag, "STEP 5: Forcing email verification via Cloud Function")

    try {
      forceEmailVerification(testEmail)
      Log.d(tag, "✅ STEP 5 PASSED: Email verification forced successfully\n")
    } catch (e: Exception) {
      Log.e(tag, "❌ STEP 5 FAILED: Could not force email verification", e)
      throw e
    }

    // ═══════════════════════════════════════════════════════════
    // STEP 6: Wait for Navigation Back to Login Screen
    // ═══════════════════════════════════════════════════════════
    Log.d(tag, "STEP 6: Waiting for navigation back to login screen")

    delay(2000)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.TITLE)
        .assertExists("Login screen title should exist after sign-up")
        .assertIsDisplayed()

    Log.d(tag, "✅ STEP 6 PASSED: Navigated back to login screen\n")

    // ═══════════════════════════════════════════════════════════
    // STEP 7: Sign In with Created Account
    // ═══════════════════════════════════════════════════════════
    Log.d(tag, "STEP 7: Signing in with created account")

    // Enter email
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT)
        .assertExists()
        .performTextInput(testEmail)
    delay(300)

    // Enter password
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT)
        .assertExists()
        .performTextInput(TEST_PASSWORD)
    delay(500)
    composeTestRule.waitForIdle()

    // Click sign in button
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON)
        .assertExists()
        .assertIsDisplayed()
        .performClick()

    Log.d(tag, "→ Sign in button clicked")
    delay(2000)
    composeTestRule.waitForIdle()

    // Get the signed-in user
    testUser = FirebaseAuth.getInstance().currentUser
    Log.d(tag, "→ User signed in with ID: ${testUser?.uid}")
    Log.d(tag, "→ User email: ${testUser?.email}")
    Log.d(tag, "→ Email verified: ${testUser?.isEmailVerified}")

    Log.d(tag, "✅ STEP 7 PASSED: Successfully signed in\n")

    // ═══════════════════════════════════════════════════════════
    // STEP 8: Wait for Home Screen
    // ═══════════════════════════════════════════════════════════
    Log.d(tag, "STEP 8: Waiting for home screen")

    var attempts = 0
    composeTestRule.waitUntil(timeoutMillis = 15_000L) {
      attempts++
      try {
        val homeButtonExists =
            composeTestRule
                .onAllNodes(hasTestTag(BottomBarTestTag.NAV_HOME))
                .fetchSemanticsNodes()
                .isNotEmpty()

        if (homeButtonExists) {
          Log.d(tag, "→ Home screen detected!")
          true
        } else {
          if (attempts % 5 == 0) {
            Log.d(tag, "→ Still waiting... (attempt $attempts)")
          }
          false
        }
      } catch (_: Exception) {
        false
      }
    }

    composeTestRule
        .onNodeWithTag(BottomBarTestTag.NAV_HOME)
        .assertExists("Home button should exist")
        .assertIsDisplayed()

    Log.d(tag, "✅ STEP 8 PASSED: Home screen reached\n")
    Log.d(tag, "╔═══════════════════════════════════════════════════════╗")
    Log.d(tag, "║   INITIALIZATION COMPLETE - READY FOR USER FLOW       ║")
    Log.d(tag, "╚═══════════════════════════════════════════════════════╝\n")

    return testUser!!
  }
}
