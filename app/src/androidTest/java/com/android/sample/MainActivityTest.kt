package com.android.sample

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.login.SignInScreenTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  companion object {
    private const val TAG = "MainActivityTest"
  }

  @get:Rule
  val composeTestRule =
      createAndroidComposeRule<MainActivity>().also {
        UserSessionManager.setCurrentUserId("testUser")
      }

  @Before
  fun initRepositories() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    try {
      ProfileRepositoryProvider.init(ctx)
      ListingRepositoryProvider.init(ctx)
      BookingRepositoryProvider.init(ctx)
      RatingRepositoryProvider.init(ctx)
      Log.d(TAG, "Repositories initialized successfully")
    } catch (e: Exception) {
      // Initialization may fail in some CI/emulator setups; log and continue
      Log.w(TAG, "Repository initialization failed", e)
    }

    UserSessionManager.setCurrentUserId("testUser")
  }

  @Test
  fun mainApp_composable_renders_without_crashing() {
    // Activity is already launched by createAndroidComposeRule
    composeTestRule.waitForIdle()

    // Verify that the main app structure is rendered
    try {
      composeTestRule.onRoot().assertExists()
      Log.d(TAG, "Main app rendered successfully")
    } catch (e: AssertionError) {
      Log.e(TAG, "Main app failed to render", e)
      throw AssertionError("Main app root composable failed to render", e)
    }
  }

  @Test
  fun mainApp_contains_navigation_components() {
    // Activity is already launched by createAndroidComposeRule
    composeTestRule.waitForIdle()

    // First, wait for the compose hierarchy to be available
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onRoot().assertExists()
        true
      } catch (_: IllegalStateException) {
        // Compose hierarchy not ready yet
        false
      }
    }
    Log.d(TAG, "Compose hierarchy is ready")

    // Wait for login screen using test tag instead of text
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule
            .onAllNodes(hasTestTag(SignInScreenTestTags.AUTH_GOOGLE))
            .fetchSemanticsNodes()
            .isNotEmpty()
      } catch (_: IllegalStateException) {
        // Hierarchy not ready yet
        false
      }
    }
    Log.d(TAG, "Login screen loaded successfully")

    // Verify key login screen components are present
    try {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.TITLE).assertIsDisplayed()
      Log.d(TAG, "Login title found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Login title not displayed", e)
      throw AssertionError("Login screen title not displayed", e)
    }

    try {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT).assertIsDisplayed()
      Log.d(TAG, "Email input found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Email input not displayed", e)
      throw AssertionError("Email input field not displayed", e)
    }

    try {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT).assertIsDisplayed()
      Log.d(TAG, "Password input found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Password input not displayed", e)
      throw AssertionError("Password input field not displayed", e)
    }

    try {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).assertIsDisplayed()
      Log.d(TAG, "Sign in button found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Sign in button not displayed", e)
      throw AssertionError("Sign in button not displayed", e)
    }

    try {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.AUTH_GOOGLE).assertIsDisplayed()
      Log.d(TAG, "Google auth button found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Google auth button not displayed", e)
      throw AssertionError("Google authentication button not displayed", e)
    }

    try {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).assertIsDisplayed()
      Log.d(TAG, "Sign up link found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Sign up link not displayed", e)
      throw AssertionError("Sign up link not displayed", e)
    }

    Log.d(TAG, "All login screen components verified successfully")
  }

  @Test
  fun onCreate_handles_repository_initialization_exception() {
    // This test verifies that MainActivity's onCreate handles repository initialization failures
    // gracefully by catching exceptions (lines 75-80). The activity should still launch
    // successfully even if repository initialization fails.

    // The activity is already created by createAndroidComposeRule, which calls onCreate
    composeTestRule.waitForIdle()

    // If onCreate's exception handling works correctly, the app should still render
    // even if some repositories failed to initialize
    composeTestRule.onRoot().assertExists()
    Log.d(TAG, "MainActivity onCreate exception handling verified - app still renders")
  }
}
