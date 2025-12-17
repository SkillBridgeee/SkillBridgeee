package com.android.sample.e2e

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

    // Wait for login screen to be displayed
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule
            .onAllNodes(hasTestTag(SignInScreenTestTags.TITLE))
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Throwable) {
        false
      }
    }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.TITLE)
        .assertExists("Login screen title should exist")
        .assertIsDisplayed()

    // Navigate to Sign Up Screen
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK)
        .assertExists("Sign up link should exist")
        .assertIsDisplayed()
        .performClick()

    // Wait for sign up screen to appear
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule
            .onAllNodes(hasTestTag(SignUpScreenTestTags.TITLE))
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Throwable) {
        false
      }
    }

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.TITLE)
        .assertExists("Sign up screen title should exist")
        .assertIsDisplayed()

    // Fill in Sign Up Form
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.NAME)
        .assertExists()
        .performTextInput(userName)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.SURNAME)
        .assertExists()
        .performTextInput(userSurname)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.EMAIL)
        .assertExists()
        .performTextInput(testEmail)

    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.PASSWORD)
        .assertExists()
        .performTextInput(TEST_PASSWORD)

    // Enter location - type "epfl" and select first suggestion
    composeTestRule
        .onNodeWithTag(com.android.sample.ui.components.LocationInputFieldTestTags.INPUT_LOCATION)
        .assertExists()
        .performTextInput("epfl")
    // Enter education level
    composeTestRule
        .onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION)
        .assertExists()
        .performTextInput("CS, 3rd year")

    // Scroll to and check ToS checkbox
    try {
      composeTestRule.onNodeWithTag(SignUpScreenTestTags.TOS_CHECKBOX).performScrollTo()
    } catch (@Suppress("SwallowedException") _: Exception) {
      // ToS checkbox already visible, no scroll needed
    }

    composeTestRule.onNodeWithTag(SignUpScreenTestTags.TOS_CHECKBOX).assertExists().performClick()

    composeTestRule.waitForIdle()

    // Submit Sign Up Form
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertExists().performScrollTo()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertIsDisplayed().performClick()

    composeTestRule.waitForIdle()

    // Force Email Verification via Cloud Function
    forceEmailVerification(testEmail)

    // Wait for navigation back to login screen
    composeTestRule.waitUntil(timeoutMillis = 8002) {
      try {
        composeTestRule
            .onAllNodes(hasTestTag(SignInScreenTestTags.TITLE))
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Throwable) {
        false
      }
    }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.TITLE)
        .assertExists("Login screen title should exist after sign-up")
        .assertIsDisplayed()

    // Sign In with Created Account
    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT)
        .assertExists()
        .performTextInput(testEmail)

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT)
        .assertExists()
        .performTextInput(TEST_PASSWORD)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON)
        .assertExists()
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()

    // Wait for Home Screen first
    composeTestRule.waitUntil(timeoutMillis = 15_000L) {
      try {
        composeTestRule
            .onAllNodes(hasTestTag(BottomBarTestTag.NAV_HOME))
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: Throwable) {
        false
      }
    }

    composeTestRule
        .onNodeWithTag(BottomBarTestTag.NAV_HOME)
        .assertExists("Home button should exist")
        .assertIsDisplayed()

    // Get the signed-in user with retry mechanism
    // Firebase authentication might take a moment to complete even after UI navigation
    // IMPORTANT: Always get fresh user from FirebaseAuth, don't reuse cached testUser
    var currentUser: FirebaseUser? = null
    var retries = 0
    val maxRetries = 10
    while (currentUser == null && retries < maxRetries) {
      currentUser = FirebaseAuth.getInstance().currentUser
      if (currentUser == null) {
        kotlinx.coroutines.delay(500) // Wait 500ms between retries
        retries++
      }
    }

    if (currentUser == null) {
      throw AssertionError(
          "Failed to get authenticated user after sign-in. FirebaseAuth.currentUser is null after $maxRetries retries.")
    }

    // Update the testUser field for compatibility with existing tests
    testUser = currentUser

    return currentUser
  }
}
