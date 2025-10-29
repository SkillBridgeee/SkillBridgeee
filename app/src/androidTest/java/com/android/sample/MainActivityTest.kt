package com.android.sample

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.bookings.MyBookingsPageTestTag
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

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

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

    // Wait for login screen using test tag instead of text
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodes(hasTestTag(SignInScreenTestTags.AUTH_GITHUB))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    Log.d(TAG, "Login screen loaded successfully")

    // Navigate from login to main app using test tag
    try {
      composeTestRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).performClick()
      Log.d(TAG, "Clicked GitHub sign-in button")
    } catch (e: AssertionError) {
      Log.e(TAG, "Failed to click GitHub sign-in button", e)
      throw AssertionError("GitHub sign-in button not found or not clickable", e)
    }

    composeTestRule.waitForIdle()

    // Wait for bottom navigation to appear using test tags
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodes(hasTestTag(MyBookingsPageTestTag.NAV_HOME))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    Log.d(TAG, "Home screen and bottom navigation loaded successfully")

    // Verify all bottom navigation items exist using test tags (not brittle text)
    try {
      composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertIsDisplayed()
      Log.d(TAG, "Home nav button found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Home nav button not displayed", e)
      throw AssertionError("Bottom navigation 'Home' button not displayed", e)
    }

    try {
      composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).assertIsDisplayed()
      Log.d(TAG, "Bookings nav button found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Bookings nav button not displayed", e)
      throw AssertionError("Bottom navigation 'Bookings' button not displayed", e)
    }

    try {
      composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertIsDisplayed()
      Log.d(TAG, "Profile nav button found")
    } catch (e: AssertionError) {
      Log.e(TAG, "Profile nav button not displayed", e)
      throw AssertionError("Bottom navigation 'Profile' button not displayed", e)
    }

    Log.d(TAG, "All bottom navigation components verified successfully")
  }
}
