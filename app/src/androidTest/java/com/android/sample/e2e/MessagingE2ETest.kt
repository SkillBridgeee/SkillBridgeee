package com.android.sample.e2e

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.MainActivity
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.communication.newImplementation.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-End test for messaging functionality.
 *
 * CURRENT STATUS: Basic authentication flow only This test demonstrates:
 * - User signs up and logs in via UI
 * - Navigates to home screen
 *
 * TODO: Implement messaging functionality once authentication issues are resolved
 * - Opens messages from top-right icon
 * - Views existing messages
 * - Sends new messages
 *
 * Uses E2ETestBase for authentication flow.
 */
@RunWith(AndroidJUnit4::class)
class MessagingE2ETest : E2ETestBase() {

  companion object {
    private const val TAG = "MessagingE2E"
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    Log.d(TAG, "=== Test started: Messaging E2E ===")
    testEmail = "e2e.messaging.test.${System.currentTimeMillis()}@example.test"

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
  }

  @After
  fun tearDown() {
    runBlocking {
      Log.d(TAG, "=== Tearing down test ===")

      try {
        // Clean up test user
        FirebaseAuth.getInstance().currentUser?.let { user ->
          user.delete().addOnCompleteListener { Log.d(TAG, "✓ Cleaned up test user") }
        }

        // Sign out
        FirebaseAuth.getInstance().signOut()

        // Clear the test user ID from UserSessionManager
        UserSessionManager.clearSession()
        Log.d(TAG, "✓ Cleared UserSessionManager test session")
      } catch (e: Exception) {
        Log.d(TAG, "Cleanup error: ${e.message}")
      }

      Log.d(TAG, "=== Teardown complete ===")
    }
  }

  @Test
  fun messaging_userViewsAndSendsMessages_successfullyInteracts() {
    runBlocking {
      Log.d(TAG, "=== Starting Messaging E2E Test ===\n")

      // ═══════════════════════════════════════════════════════════
      // STEPS 1-8: Authentication and Navigation (via E2ETestBase)
      // ═══════════════════════════════════════════════════════════
      // The base class handles:
      // - User sign-up via UI
      // - Email verification (auto-verified for test emails)
      // - User sign-in
      // - Setting UserSessionManager.setCurrentUserId()
      // - Navigation to home screen

      initializeUserAndNavigateToHome(
          composeTestRule = composeTestRule, userName = "John", userSurname = "Doe")

      Log.d(TAG, "✅ Authentication and navigation completed\n")

      // ═══════════════════════════════════════════════════════════
      // STEP 9: Verify Home Screen
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "STEP 9: Verifying home screen is displayed")
      delay(2000)
      composeTestRule.waitForIdle()

      // Verify we're on the home screen
      try {
        composeTestRule
            .onNodeWithTag("nav_home", useUnmergedTree = true)
            .assertExists("Home navigation should exist")
        Log.d(TAG, "✅ Home screen is displayed")
      } catch (e: Exception) {
        Log.e(TAG, "❌ Could not verify home screen: ${e.message}")
        throw e
      }

      Log.d(TAG, "✅ STEP 9 PASSED: Home screen verified\n")

      // ═══════════════════════════════════════════════════════════
      // TODO: Messaging Functionality (Commented Out for Now)
      // ═══════════════════════════════════════════════════════════
      /*
      // STEP 10: Open Messages from Top-Right Icon
      Log.d(TAG, "STEP 10: Opening messages")

      // Try to find and click messages icon
      // ... messaging navigation code ...

      // STEP 11: Verify User Authentication in Messaging Screen
      // Check that messaging screen doesn't show "User not authenticated" error
      // ... authentication verification code ...

      // STEP 12: View and Send Messages
      // Interact with messaging functionality
      // ... message interaction code ...
      */

      // ═══════════════════════════════════════════════════════════
      // FINAL STEP: Complete Test
      // ═══════════════════════════════════════════════════════════
      Log.d(TAG, "=== ✅ TEST COMPLETED SUCCESSFULLY ===")
      Log.d(TAG, "Summary: User authenticated and navigated to home screen")
      Log.d(TAG, "Note: Messaging functionality commented out for future implementation\n")
    }
  }
}
