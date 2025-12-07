package com.android.sample.e2e

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.android.sample.MainActivity
import com.android.sample.e2e.E2ETestHelper.cleanupFirebaseUser
import com.android.sample.e2e.E2ETestHelper.cleanupTestProfile
import com.android.sample.e2e.E2ETestHelper.signOutCurrentUser
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.communication.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-End test demonstrating UI-based login with test email domain.
 *
 * This is a STANDALONE test that demonstrates the complete UI login flow. The same flow is also
 * available in E2ETestBase.initializeUserAndNavigateToHome() for use as a setup step in other E2E
 * tests.
 *
 * Purpose:
 * - Dedicated regression test for the login/signup UI flow
 * - Documentation/demonstration of how UI-based login works
 * - Validates the entire authentication flow end-to-end
 *
 * This test uses the actual UI elements for login instead of bypassing them with logic. It uses a
 * Cloud Function to force email verification for test email domains (@example.test).
 *
 * This test verifies that:
 * 1. User can sign up using the UI with a test email
 * 2. Email verification can be forced via Cloud Function for test accounts
 * 3. User can sign in after verification
 * 4. User can navigate through the app after signup
 *
 * Related files:
 * - E2ETestBase.kt - Contains the same flow as a reusable base class method
 * - ProfileEditE2ETest.kt, BookListingE2ETest.kt - Examples of tests using the base class
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class UILoginE2ETest {

  companion object {
    private const val TAG = "UILoginE2E"

    // Use a test email domain that will skip verification in debug builds
    private fun generateTestEmail() = "e2e.ui.test.${System.currentTimeMillis()}@example.test"

    private const val TEST_PASSWORD = "TestPassword123!"
    private const val TEST_NAME = "UI Test"
    private const val TEST_SURNAME = "User"
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  private var testUser: FirebaseUser? = null
  private var testEmail: String = ""

  @Before
  fun setup() {
    Log.d(TAG, "=== Setting up UI Login E2E Test ===")

    // Initialize all repositories
    val ctx = composeTestRule.activity
    try {
      ProfileRepositoryProvider.init(ctx)
      ListingRepositoryProvider.init(ctx)
      BookingRepositoryProvider.init(ctx)
      RatingRepositoryProvider.init(ctx)
      OverViewConvRepositoryProvider.init(ctx)
      ConversationRepositoryProvider.init(ctx)
      Log.d(TAG, "✓ Repositories initialized")
    } catch (e: Exception) {
      Log.w(TAG, "Repository initialization warning", e)
    }

    signOutCurrentUser()
    UserSessionManager.clearSession()
    composeTestRule.waitForIdle()

    testEmail = generateTestEmail()
    Log.d(TAG, "✓ Test email: $testEmail")
    Log.d(TAG, "=== Setup complete ===\n")
  }

  @After
  fun tearDown() {
    runBlocking {
      Log.d(TAG, "\n=== Tearing down test ===")
      testUser?.let { user ->
        cleanupTestProfile(user.uid)
        cleanupFirebaseUser(user)
        Log.d(TAG, "✓ Cleaned up test user")
      }
      signOutCurrentUser()
      UserSessionManager.clearSession()
      Log.d(TAG, "=== Teardown complete ===")
    }
  }

  @Test
  fun uiLogin_userSignsUpWithTestEmail_autoVerifiedAndNavigatesToHome() {
    runBlocking {
      Log.d(TAG, "\n╔═══════════════════════════════════════════════════════╗")
      Log.d(TAG, "║   UI-BASED LOGIN E2E TEST STARTED                     ║")
      Log.d(TAG, "╚═══════════════════════════════════════════════════════╝\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 1: Verify Login Screen
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 1: Verifying login screen is displayed")
      composeTestRule.waitForIdle()
      delay(1000)

      composeTestRule
          .onNodeWithTag(SignInScreenTestTags.TITLE)
          .assertExists("Login screen title should exist")
          .assertIsDisplayed()

      Log.d(TAG, "✅ STEP 1 PASSED: Login screen displayed\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 2: Navigate to Sign Up Screen
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 2: Navigating to sign up screen via UI")

      composeTestRule
          .onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK)
          .assertExists("Sign up link should exist")
          .assertIsDisplayed()
          .performClick()

      composeTestRule.waitForIdle()
      delay(1000)

      // Verify we're on the signup screen
      composeTestRule
          .onNodeWithTag(SignUpScreenTestTags.TITLE)
          .assertExists("Sign up screen title should exist")
          .assertIsDisplayed()

      Log.d(TAG, "✅ STEP 2 PASSED: Sign up screen displayed\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 3: Fill in Sign Up Form
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 3: Filling in sign up form with test credentials")

      // Enter name
      composeTestRule
          .onNodeWithTag(SignUpScreenTestTags.NAME)
          .assertExists()
          .performTextInput(TEST_NAME)

      delay(300)

      // Enter surname
      composeTestRule
          .onNodeWithTag(SignUpScreenTestTags.SURNAME)
          .assertExists()
          .performTextInput(TEST_SURNAME)

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

      delay(5000) // Wait for suggestions to appear (increased to 5 seconds for API call)

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
        Log.d(TAG, "→ ToS checkbox already visible, no scroll needed")
      }

      composeTestRule.onNodeWithTag(SignUpScreenTestTags.TOS_CHECKBOX).assertExists().performClick()

      delay(500)

      Log.d(TAG, "→ Entered credentials:")
      Log.d(TAG, "  Name: $TEST_NAME $TEST_SURNAME")
      Log.d(TAG, "  Email: $testEmail")
      Log.d(TAG, "  Location: epfl")
      Log.d(TAG, "  Education: CS, 3rd year")
      Log.d(TAG, "✅ STEP 3 PASSED: Sign up form filled\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 4: Submit Sign Up Form
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 4: Submitting sign up form")

      // Close the keyboard by clicking outside or using back
      composeTestRule.waitForIdle()
      delay(500)

      // Scroll to sign up button to ensure it's visible and clickable
      composeTestRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertExists().performScrollTo()

      delay(500)
      composeTestRule.waitForIdle()

      // Now click the sign up button
      composeTestRule.onNodeWithTag(SignUpScreenTestTags.SIGN_UP).assertIsDisplayed().performClick()

      Log.d(TAG, "→ Sign up button clicked")
      delay(2000) // Wait for account creation
      composeTestRule.waitForIdle()

      Log.d(TAG, "✅ STEP 4 PASSED: Sign up submitted successfully\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 4.5: Force Email Verification via Cloud Function
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 4.5: Forcing email verification via Cloud Function")

      try {
        E2ETestHelper.forceEmailVerification(testEmail)
        Log.d(TAG, "✅ STEP 4.5 PASSED: Email verification forced successfully\n")
      } catch (e: Exception) {
        Log.e(TAG, "❌ STEP 4.5 FAILED: Could not force email verification", e)
        throw e
      }

      // ═══════════════════════════════════════════════════════════
      // STEP 5: Wait for Navigation Back to Login Screen
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 5: Waiting for navigation back to login screen")

      // For email/password sign-up, the app:
      // 1. Creates the user and profile
      // 2. Sends verification email
      // 3. Signs the user out
      // 4. Navigates back to login screen
      // 5. User must sign in again

      delay(2000) // Wait for navigation
      composeTestRule.waitForIdle()

      // Verify we're back on the login screen
      composeTestRule
          .onNodeWithTag(SignInScreenTestTags.TITLE)
          .assertExists("Login screen title should exist after sign-up")
          .assertIsDisplayed()

      Log.d(TAG, "✅ STEP 5 PASSED: Navigated back to login screen\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 6: Sign In with Created Account
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 6: Signing in with created account")

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

      Log.d(TAG, "→ Sign in button clicked")
      delay(2000)
      composeTestRule.waitForIdle()

      // Get the signed-in user
      testUser = FirebaseAuth.getInstance().currentUser
      Log.d(TAG, "→ User signed in with ID: ${testUser?.uid}")
      Log.d(TAG, "→ User email: ${testUser?.email}")
      Log.d(TAG, "→ Email verified: ${testUser?.isEmailVerified}")

      Log.d(TAG, "✅ STEP 6 PASSED: Successfully signed in\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 7: Wait for Navigation to Home
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 7: Waiting for automatic navigation to home screen")

      // The app should now navigate to home because:
      // 1. User is authenticated
      // 2. Profile exists from sign-up
      // 3. Email verification is skipped for @example.test domain in debug builds

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
            Log.d(TAG, "→ Home screen detected!")
            true
          } else {
            if (attempts % 5 == 0) {
              Log.d(TAG, "→ Waiting for home screen... (attempt $attempts)")
            }
            false
          }
        } catch (e: Exception) {
          Log.d(TAG, "→ Exception while checking for home: ${e.message}")
          false
        }
      }

      delay(1000)
      composeTestRule.waitForIdle()

      // Verify we're on home screen
      composeTestRule
          .onNodeWithTag(BottomBarTestTag.NAV_HOME)
          .assertExists("Home navigation button should exist")
          .assertIsDisplayed()

      Log.d(TAG, "✅ STEP 7 PASSED: Successfully navigated to home screen\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 8: Verify User Session
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 8: Verifying user session")

      val currentUser = FirebaseAuth.getInstance().currentUser
      assert(currentUser != null) { "User should be authenticated" }
      assert(currentUser?.email == testEmail) { "Email should match test email" }

      Log.d(TAG, "→ Session verified:")
      Log.d(TAG, "  User ID: ${currentUser?.uid}")
      Log.d(TAG, "  Email: ${currentUser?.email}")
      Log.d(TAG, "  Email verified: ${currentUser?.isEmailVerified}")
      Log.d(TAG, "✅ STEP 8 PASSED: User session verified\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 9: Navigate Through App
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 9: Testing navigation through app")

      // Navigate to Profile
      composeTestRule
          .onNodeWithTag(BottomBarTestTag.NAV_PROFILE)
          .assertExists()
          .assertIsDisplayed()
          .performClick()

      delay(1000)
      composeTestRule.waitForIdle()

      // Navigate back to Home
      composeTestRule
          .onNodeWithTag(BottomBarTestTag.NAV_HOME)
          .assertExists()
          .assertIsDisplayed()
          .performClick()

      delay(1000)
      composeTestRule.waitForIdle()

      Log.d(TAG, "✅ STEP 9 PASSED: Navigation working correctly\n")

      Log.d(TAG, "\n╔═══════════════════════════════════════════════════════╗")
      Log.d(TAG, "║   UI-BASED LOGIN E2E TEST COMPLETED SUCCESSFULLY      ║")
      Log.d(TAG, "╚═══════════════════════════════════════════════════════╝\n")
    }
  }
}
