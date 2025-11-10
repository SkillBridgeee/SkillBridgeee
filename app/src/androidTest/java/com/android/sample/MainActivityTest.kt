package com.android.sample

import android.util.Log
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.testutils.TestAuthHelpers
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

  companion object {
    private const val TAG = "MainActivityTest"

    @BeforeClass
    @JvmStatic
    fun globalSignIn() {
      try {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } catch (_: IllegalStateException) {}
      try {
        // Create & sign-in a persistent test Google user and create a minimal app profile.
        TestAuthHelpers.signInAsGoogleUserBlocking(
            email = "class.user@example.com", displayName = "Class User", createAppProfile = true)
      } catch (e: Exception) {
        Log.w(TAG, "globalSignIn failed", e)
      }
    }

    @AfterClass
    @JvmStatic
    fun globalTearDown() {
      try {
        // Attempt to delete the user, but don't fail the test if it throws.
        Firebase.auth.currentUser?.delete()
      } catch (e: Exception) {
        Log.w(TAG, "Failed to delete global test user in @AfterClass", e)
      }
      try {
        Firebase.auth.signOut()
      } catch (_: Exception) {}
    }
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
      Log.w(TAG, "Repository initialization failed", e)
    }

    // Ensure navigation state is fresh for each test.
    RouteStackManager.clear()

    // The user is already signed in via @BeforeClass. The Activity launches with this
    // user and should navigate to the HOME screen. We just need to wait for it.
    waitForHome(timeoutMs = 10_000L)
  }

  @After
  fun tearDown() {
    // Do not sign out here. The user should persist for all tests in this class.
    // @AfterClass will handle the final cleanup.
  }

  private fun waitForHome(timeoutMs: Long = 5_000L) {
    // First wait for navigation graph to be set
    composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
      try {
        RouteStackManager.getCurrentRoute() != null
      } catch (e: Exception) {
        false
      }
    }

    // Then wait for HOME route
    composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun mainApp_composable_renders_without_crashing() {
    composeTestRule.waitForIdle()
    try {
      composeTestRule.onRoot().assertExists()
      Log.d(TAG, "Main app rendered successfully")
    } catch (e: AssertionError) {
      Log.e(TAG, "Main app failed to render", e)
      throw AssertionError("Main app root composable failed to render", e)
    }
  }

  @Test
  fun mainApp_shows_home_when_signed_in() {
    // The @Before method already waits for the home screen.
    // We can proceed directly to assertions.
    composeTestRule.waitForIdle()

    try {
      composeTestRule.onRoot().assertIsDisplayed()
      Log.d(TAG, "App reached HOME successfully")
    } catch (e: AssertionError) {
      Log.e(TAG, "Home screen not displayed", e)
      throw e
    }

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
