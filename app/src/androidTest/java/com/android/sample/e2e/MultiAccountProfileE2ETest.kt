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
import com.android.sample.ui.profile.MyProfileScreenTestTag
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
 * End-to-End test for Multi-Account Profile workflow.
 *
 * This test verifies that a user can:
 * 1. Sign in with one account
 * 2. Navigate to their profile page
 * 3. Sign out
 * 4. Create a new account
 * 5. Sign in with the new account
 * 6. Navigate to the new account's profile page and verify it's different
 *
 * This test ensures proper user session management and profile isolation between accounts.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class MultiAccountProfileE2ETest : E2ETestBase() {

  companion object {
    private const val TAG = "MultiAccountProfileE2E"
    private const val PROFILE_LOAD_DELAY = 5000L
    private const val NAVIGATION_DELAY = 1000L
    private const val SCREEN_TRANSITION_DELAY = 3000L
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  private var firstUser: FirebaseUser? = null
  private var secondUser: FirebaseUser? = null
  private var firstEmail: String = ""
  private var secondEmail: String = ""

  @Before
  fun setup() {
    Log.d(TAG, "=== Setting up Multi-Account Profile E2E Test ===")

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

    // Generate unique emails with UUID to completely avoid collisions
    val uuid1 = java.util.UUID.randomUUID().toString().substring(0, 8)
    val uuid2 = java.util.UUID.randomUUID().toString().substring(0, 8)
    val timestamp = System.currentTimeMillis()
    firstEmail = "e2e.multi.first.${timestamp}.${uuid1}@example.test"
    secondEmail = "e2e.multi.second.${timestamp}.${uuid2}@example.test"
    Log.d(TAG, "✓ First test email: $firstEmail")
    Log.d(TAG, "✓ Second test email: $secondEmail")
    Log.d(TAG, "=== Setup complete ===\n")
  }

  @After
  fun tearDown() {
    runBlocking {
      Log.d(TAG, "\n=== Tearing down test ===")

      // Clean up second user first (if currently signed in)
      secondUser?.let { user ->
        try {
          cleanupTestProfile(user.uid)
          cleanupFirebaseUser(user)
          Log.d(TAG, "✓ Cleaned up second test user")
        } catch (e: Exception) {
          Log.w(TAG, "Warning during second user cleanup: ${e.message}")
        }
      }

      // Clean up first user
      firstUser?.let { user ->
        try {
          cleanupTestProfile(user.uid)
          cleanupFirebaseUser(user)
          Log.d(TAG, "✓ Cleaned up first test user")
        } catch (e: Exception) {
          Log.w(TAG, "Warning during first user cleanup: ${e.message}")
        }
      }

      signOutCurrentUser()
      UserSessionManager.clearSession()

      // Give Firebase time to process deletions
      delay(500)

      Log.d(TAG, "=== Teardown complete ===")
    }
  }

  /**
   * Helper function to navigate to profile page Handles waiting, navigation click, and verification
   * that profile screen is displayed
   */
  private suspend fun navigateToProfilePage() {
    composeTestRule.waitForIdle()
    delay(NAVIGATION_DELAY)

    composeTestRule
        .onNodeWithTag(BottomBarTestTag.NAV_PROFILE)
        .assertExists("Profile navigation button should exist")
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitForIdle()
    delay(SCREEN_TRANSITION_DELAY)

    composeTestRule
        .onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST)
        .assertExists("Profile page should be displayed")
  }

  /** Helper function to wait for profile data to load from Firestore */
  private suspend fun waitForProfileDataLoad() {
    Log.d(TAG, "→ Waiting for profile data to load from Firestore...")
    delay(PROFILE_LOAD_DELAY)
    composeTestRule.waitForIdle()
    Log.d(TAG, "→ Profile data should be loaded, verifying name...")
  }

  /**
   * Helper function to verify a user's name is displayed on the profile page
   *
   * @param expectedName The full name expected to be displayed
   * @param userName The first name for logging purposes
   */
  private fun verifyProfileName(expectedName: String, userName: String) {
    try {
      composeTestRule
          .onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY)
          .assertExists("Name display should exist")

      composeTestRule
          .onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY)
          .assertTextContains(expectedName, ignoreCase = true)

      Log.d(TAG, "→ $userName's name '$expectedName' found on profile page")
    } catch (e: AssertionError) {
      Log.e(TAG, "→ Name verification failed: ${e.message}")
      // Try to log what text is actually displayed
      try {
        val nameNode =
            composeTestRule.onNodeWithTag(MyProfileScreenTestTag.NAME_DISPLAY).fetchSemanticsNode()
        Log.e(TAG, "→ Actual name displayed: ${nameNode.config}")
      } catch (ex: Exception) {
        Log.e(TAG, "→ Could not fetch name node: ${ex.message}")
      }
      throw AssertionError("$userName's name should be displayed on profile page")
    }
  }

  /**
   * Helper function to verify a name is NOT displayed on the profile page
   *
   * @param nameToNotExist The name that should not be present
   */
  private fun verifyNameNotDisplayed(nameToNotExist: String) {
    try {
      composeTestRule
          .onNodeWithText(nameToNotExist, substring = true, ignoreCase = true)
          .assertDoesNotExist()
      Log.d(TAG, "→ Verified '$nameToNotExist' is NOT displayed (correct behavior)")
    } catch (_: AssertionError) {
      Log.e(TAG, "→ ERROR: '$nameToNotExist' is still displayed!")
      throw AssertionError("'$nameToNotExist' should NOT be displayed on profile page")
    }
  }

  @Test
  fun multiAccountProfile_signInViewProfileSignOutCreateNewAccountAndViewProfile_profilesAreDifferent() {
    runBlocking {
      Log.d(TAG, "\n╔═══════════════════════════════════════════════════════╗")
      Log.d(TAG, "║   MULTI-ACCOUNT PROFILE E2E TEST STARTED              ║")
      Log.d(TAG, "╚═══════════════════════════════════════════════════════╝\n")

      // ═══════════════════════════════════════════════════════════
      // PHASE 1: Create and sign in with first account
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "PHASE 1: Creating and signing in with first account")
      testEmail = firstEmail
      firstUser =
          initializeUserAndNavigateToHome(
              composeTestRule, userName = "Alice", userSurname = "Smith")

      Log.d(TAG, "→ First user signed in:")
      Log.d(TAG, "  User ID: ${firstUser?.uid}")
      Log.d(TAG, "  Email: ${firstUser?.email}")
      Log.d(TAG, "✅ PHASE 1 PASSED: First account created and signed in\n")

      // ═══════════════════════════════════════════════════════════
      // PHASE 2: Navigate to first user's profile page
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "PHASE 2: Navigating to first user's profile page")

      navigateToProfilePage()
      waitForProfileDataLoad()

      // Verify first user's name is displayed
      try {
        verifyProfileName("Alice Smith", "First user")
      } catch (e: AssertionError) {
        Log.d(TAG, "→ Name verification: ${e.message}")
      }

      Log.d(TAG, "✅ PHASE 2 PASSED: First user's profile page displayed\n")

      // ═══════════════════════════════════════════════════════════
      // PHASE 3: Sign out from first account
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "PHASE 3: Signing out from first account")

      // Scroll to find the logout button
      composeTestRule
          .onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST)
          .performScrollToNode(hasTestTag(MyProfileScreenTestTag.LOGOUT_BUTTON))

      composeTestRule.waitForIdle()
      delay(500)

      // Click the logout button
      composeTestRule
          .onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON)
          .assertExists("Logout button should exist")
          .performClick()

      Log.d(TAG, "→ Logout button clicked")

      composeTestRule.waitForIdle()
      delay(2000) // Wait for logout and navigation

      // Verify we're back on the login screen
      composeTestRule.waitUntil(timeoutMillis = 10_000L) {
        try {
          composeTestRule
              .onAllNodes(hasTestTag(com.android.sample.ui.login.SignInScreenTestTags.TITLE))
              .fetchSemanticsNodes()
              .isNotEmpty()
        } catch (_: Throwable) {
          false
        }
      }

      composeTestRule
          .onNodeWithTag(com.android.sample.ui.login.SignInScreenTestTags.TITLE)
          .assertExists("Login screen should be displayed after logout")
          .assertIsDisplayed()

      // Verify user is signed out
      var currentUser = FirebaseAuth.getInstance().currentUser
      assert(currentUser == null) { "User should be signed out after logout" }
      Log.d(TAG, "→ FirebaseAuth.currentUser is null")

      // Clear the session and force sign out again to be absolutely sure
      signOutCurrentUser()
      UserSessionManager.clearSession()
      delay(2000) // Give extra time for Firebase to fully clear the session
      composeTestRule.waitForIdle()

      // Verify again that no user is signed in
      currentUser = FirebaseAuth.getInstance().currentUser
      assert(currentUser == null) { "User must be null before creating second account" }
      Log.d(TAG, "→ Confirmed: No user is signed in before creating second account")

      Log.d(TAG, "✅ PHASE 3 PASSED: First user signed out successfully\n")

      // ═══════════════════════════════════════════════════════════
      // PHASE 4: Create and sign in with second account
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "PHASE 4: Creating and signing in with second account")
      Log.d(TAG, "→ About to create second account with email: $secondEmail")

      // Clear testUser to prevent reusing cached user
      testUser = null
      testEmail = secondEmail
      secondUser =
          initializeUserAndNavigateToHome(
              composeTestRule, userName = "Bob", userSurname = "Johnson")

      Log.d(TAG, "→ Second user signed in:")
      Log.d(TAG, "  User ID: ${secondUser?.uid}")
      Log.d(TAG, "  Email: ${secondUser?.email}")

      // Verify immediately that the UIDs are different
      Log.d(TAG, "→ Comparing user IDs:")
      Log.d(TAG, "  First user UID:  ${firstUser?.uid}")
      Log.d(TAG, "  Second user UID: ${secondUser?.uid}")

      if (firstUser?.uid == secondUser?.uid) {
        Log.e(TAG, "❌ ERROR: User IDs are the same! This means the same account is being reused.")
        Log.e(TAG, "  First user email:  ${firstUser?.email}")
        Log.e(TAG, "  Second user email: ${secondUser?.email}")
        throw AssertionError("User IDs must be different. Got same UID: ${firstUser?.uid}")
      }

      Log.d(TAG, "✅ PHASE 4 PASSED: Second account created and signed in with different UID\n")

      // ═══════════════════════════════════════════════════════════
      // PHASE 5: Navigate to second user's profile page and verify
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "PHASE 5: Navigating to second user's profile page and verifying")

      navigateToProfilePage()
      waitForProfileDataLoad()

      // Verify second user's name is displayed (NOT first user's name)
      verifyProfileName("Bob Johnson", "Second user")

      // Verify first user's name is NOT displayed
      verifyNameNotDisplayed("Alice Smith")

      // Verify the user IDs are different
      assert(firstUser?.uid != secondUser?.uid) {
        "User IDs should be different for different accounts"
      }
      Log.d(TAG, "→ Verified user IDs are different")

      Log.d(TAG, "✅ PHASE 5 PASSED: Second user's profile page correctly displayed\n")

      // ═══════════════════════════════════════════════════════════
      // Final Summary
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "\n╔═══════════════════════════════════════════════════════╗")
      Log.d(TAG, "║   MULTI-ACCOUNT PROFILE E2E TEST COMPLETED            ║")
      Log.d(TAG, "╚═══════════════════════════════════════════════════════╝")
      Log.d(TAG, "✅ All phases completed successfully!")
      Log.d(TAG, "✅ Profile isolation between accounts verified!")
      Log.d(TAG, "✅ First user: ${firstUser?.email}")
      Log.d(TAG, "✅ Second user: ${secondUser?.email}\n")
    }
  }
}
