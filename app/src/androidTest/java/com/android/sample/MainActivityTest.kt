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
import com.android.sample.testutils.TestUiHelpers
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

    // Ensure navigation state is fresh
    RouteStackManager.clear()

    // Connect to Firebase emulators (safe to call multiple times)
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {}

    // Ensure the class-scoped user is present for the Activity's auth listener.
    try {
      Firebase.auth.signOut()
      TestAuthHelpers.signInAsGoogleUserBlocking(
          email = "class.user@example.com", displayName = "Class User", createAppProfile = true)
    } catch (e: Exception) {
      Log.w(TAG, "Re-sign in in initRepositories failed", e)
    }

    // Allow the activity / auth listener to react and, if routed to SignUp, complete it.
    composeTestRule.waitForIdle()
    val detectStart = System.currentTimeMillis()
    val detectTimeout = 5_000L
    while (System.currentTimeMillis() - detectStart < detectTimeout) {
      val current = RouteStackManager.getCurrentRoute()
      if (current == NavRoutes.HOME) break
      if (current?.startsWith(NavRoutes.SIGNUP_BASE) == true) {
        // Complete the signup UI (email should be pre-filled for Google signups)
        try {
          TestUiHelpers.signUpThroughUi(
              composeTestRule = composeTestRule,
              password = "P@ssw0rd!",
              name = "Class",
              surname = "User",
              levelOfEducation = "Test",
              description = "Class-level test user",
              timeoutMs = 8_000L)
        } catch (e: Exception) {
          Log.w(TAG, "signUpThroughUi failed in initRepositories", e)
        }
        break
      }
      Thread.sleep(200)
    }
  }

  @After
  fun tearDown() {
    // Per-test: sign out to keep emulator clean. Class teardown handles deletion.
    try {
      Firebase.auth.signOut()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to sign out in tearDown", e)
    }
  }

  private fun waitForHome(timeoutMs: Long = 5_000L) {
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
    // Wait for the class-level sign-in to land the app on HOME
    waitForHome(timeoutMs = 15_000)
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

// package com.android.sample
//
// import android.util.Log
// import androidx.compose.ui.test.assertIsDisplayed
// import androidx.compose.ui.test.hasTestTag
// import androidx.compose.ui.test.junit4.createAndroidComposeRule
// import androidx.compose.ui.test.onNodeWithTag
// import androidx.compose.ui.test.onRoot
// import androidx.compose.ui.test.performClick
// import androidx.test.ext.junit.runners.AndroidJUnit4
// import androidx.test.platform.app.InstrumentationRegistry
// import com.android.sample.model.booking.BookingRepositoryProvider
// import com.android.sample.model.listing.ListingRepositoryProvider
// import com.android.sample.model.rating.RatingRepositoryProvider
// import com.android.sample.model.user.ProfileRepositoryProvider
// import com.android.sample.ui.bookings.MyBookingsPageTestTag
// import com.android.sample.ui.login.SignInScreenTestTags
// import org.junit.Before
// import org.junit.Rule
// import org.junit.Test
// import org.junit.runner.RunWith
//
// @RunWith(AndroidJUnit4::class)
// class MainActivityTest {
//
//  companion object {
//    private const val TAG = "MainActivityTest"
//  }
//
//  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()
//
//  @Before
//  fun initRepositories() {
//    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
//    try {
//      ProfileRepositoryProvider.init(ctx)
//      ListingRepositoryProvider.init(ctx)
//      BookingRepositoryProvider.init(ctx)
//      RatingRepositoryProvider.init(ctx)
//      Log.d(TAG, "Repositories initialized successfully")
//    } catch (e: Exception) {
//      // Initialization may fail in some CI/emulator setups; log and continue
//      Log.w(TAG, "Repository initialization failed", e)
//    }
//  }
//
//  @Test
//  fun mainApp_composable_renders_without_crashing() {
//    // Activity is already launched by createAndroidComposeRule
//    composeTestRule.waitForIdle()
//
//    // Verify that the main app structure is rendered
//    try {
//      composeTestRule.onRoot().assertExists()
//      Log.d(TAG, "Main app rendered successfully")
//    } catch (e: AssertionError) {
//      Log.e(TAG, "Main app failed to render", e)
//      throw AssertionError("Main app root composable failed to render", e)
//    }
//  }
//
//  @Test
//  fun mainApp_contains_navigation_components() {
//    // Activity is already launched by createAndroidComposeRule
//    composeTestRule.waitForIdle()
//
//    // Wait for login screen using test tag instead of text
//    composeTestRule.waitUntil(timeoutMillis = 5_000) {
//      composeTestRule
//          .onAllNodes(hasTestTag(SignInScreenTestTags.AUTH_GITHUB))
//          .fetchSemanticsNodes()
//          .isNotEmpty()
//    }
//    Log.d(TAG, "Login screen loaded successfully")
//
//    // Navigate from login to main app using test tag
//    try {
//      composeTestRule.onNodeWithTag(SignInScreenTestTags.AUTH_GITHUB).performClick()
//      Log.d(TAG, "Clicked GitHub sign-in button")
//    } catch (e: AssertionError) {
//      Log.e(TAG, "Failed to click GitHub sign-in button", e)
//      throw AssertionError("GitHub sign-in button not found or not clickable", e)
//    }
//
//    composeTestRule.waitForIdle()
//
//    // Wait for bottom navigation to appear using test tags
//    composeTestRule.waitUntil(timeoutMillis = 5_000) {
//      composeTestRule
//          .onAllNodes(hasTestTag(MyBookingsPageTestTag.NAV_HOME))
//          .fetchSemanticsNodes()
//          .isNotEmpty()
//    }
//    Log.d(TAG, "Home screen and bottom navigation loaded successfully")
//
//    // Verify all bottom navigation items exist using test tags (not brittle text)
//    try {
//      composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertIsDisplayed()
//      Log.d(TAG, "Home nav button found")
//    } catch (e: AssertionError) {
//      Log.e(TAG, "Home nav button not displayed", e)
//      throw AssertionError("Bottom navigation 'Home' button not displayed", e)
//    }
//
//    try {
//      composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).assertIsDisplayed()
//      Log.d(TAG, "Bookings nav button found")
//    } catch (e: AssertionError) {
//      Log.e(TAG, "Bookings nav button not displayed", e)
//      throw AssertionError("Bottom navigation 'Bookings' button not displayed", e)
//    }
//
//    try {
//      composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertIsDisplayed()
//      Log.d(TAG, "Profile nav button found")
//    } catch (e: AssertionError) {
//      Log.e(TAG, "Profile nav button not displayed", e)
//      throw AssertionError("Bottom navigation 'Profile' button not displayed", e)
//    }
//
//    Log.d(TAG, "All bottom navigation components verified successfully")
//  }
// }
