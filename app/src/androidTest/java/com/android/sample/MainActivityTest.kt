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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.After
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

  private lateinit var testEmail: String

  @Before
  fun setUp() {
    // Sign out first to ensure clean state
    try {
      Firebase.auth.signOut()
    } catch (_: Exception) {}

    // Configure emulators
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {}

    // Initialize repositories
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

    RouteStackManager.clear()

    // Sign in with Google (this will trigger the signup flow)
    testEmail = "test.user.${System.currentTimeMillis()}@example.com"
    var userId: String? = null

    runBlocking {
      try {
        Log.d(TAG, "Signing in as Google user with email: $testEmail")
        TestAuthHelpers.signInAsGoogleUser(email = testEmail, displayName = "Test User")

        val currentUser = Firebase.auth.currentUser
        require(currentUser != null) { "User not signed in after signInAsGoogleUser" }
        userId = currentUser.uid
        Log.d(TAG, "User signed in: ${currentUser.uid}, email: ${currentUser.email}")
      } catch (e: Exception) {
        Log.e(TAG, "Sign in failed in setUp", e)
        throw e
      }
    }

    // Wait for navigation to signup screen
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      val route = RouteStackManager.getCurrentRoute()
      Log.d(TAG, "Waiting for SIGNUP, current route: $route")
      route?.startsWith(NavRoutes.SIGNUP_BASE) == true
    }

    // Complete signup through UI
    Log.d(TAG, "Completing signup through UI")
    TestUiHelpers.signUpThroughUi(
      composeTestRule = composeTestRule,
      password = "TestPassw0rd!",
      name = "Test",
      surname = "User",
      levelOfEducation = "CS, 3rd year",
      description = "Test user for main activity tests",
      addressQuery = "Test Location",
      timeoutMs = 15_000L)

    // Wait for signup to complete (check if profile exists in Firestore)
    runBlocking {
      try {
        Log.d(TAG, "Waiting for profile to be created")
        var profileCreated = false
        val startTime = System.currentTimeMillis()

        while (!profileCreated && System.currentTimeMillis() - startTime < 15_000) {
          try {
            val profile = ProfileRepositoryProvider.repository.getProfile(userId!!)
            if (profile != null) {
              Log.d(TAG, "Profile verified: ${profile.name}")
              profileCreated = true
            }
          } catch (_: Exception) {}

          if (!profileCreated) {
            delay(500)
          }
        }

        require(profileCreated) { "Profile not created after signup" }
      } catch (e: Exception) {
        Log.e(TAG, "Profile verification failed", e)
        throw e
      }
    }

    // Re-authenticate to trigger AuthStateListener
    runBlocking {
      try {
        Log.d(TAG, "Re-authenticating after signup")
        TestAuthHelpers.signInAsGoogleUser(email = testEmail, displayName = "Test User")

        val currentUser = Firebase.auth.currentUser
        require(currentUser != null) { "User not signed in after re-authentication" }
        Log.d(TAG, "Re-authenticated: ${currentUser.uid}")

        delay(1000) // Give AuthStateListener time to fire
      } catch (e: Exception) {
        Log.e(TAG, "Re-authentication failed", e)
        throw e
      }
    }

    // Wait for home screen
    waitForHome(timeoutMs = 15_000L)
  }

  @After
  fun tearDown() {
    try {
      Firebase.auth.currentUser?.delete()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to delete test user", e)
    }
    try {
      TestAuthHelpers.signOut()
    } catch (_: Exception) {}
  }

  private fun waitForHome(timeoutMs: Long = 10_000L) {
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
      val route = RouteStackManager.getCurrentRoute()
      Log.d(TAG, "Waiting for HOME, current route: $route")
      route == NavRoutes.HOME
    }
    composeTestRule.waitForIdle()
    Log.d(TAG, "Reached HOME screen")
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

    // Verify all bottom navigation items exist using test tags
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
