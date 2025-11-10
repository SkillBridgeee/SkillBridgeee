package com.android.sample.navigation

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.MainActivity
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.testutils.TestAuthHelpers
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class NavGraphCoverageTest {

  companion object {
    private const val TAG = "NavGraphCoverageTest"

    @BeforeClass
    @JvmStatic
    fun globalSignIn() {
      try {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } catch (_: IllegalStateException) {}

      try {
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
    } catch (e: Exception) {
      e.printStackTrace()
    }
    RouteStackManager.clear()

    // The user is already signed in via @BeforeClass. The Activity launches with this
    // user and should navigate to the HOME screen. We just need to wait for it.
    waitForHome(timeoutMs = 10_000L)
  }

  private fun waitForHome(timeoutMs: Long = 5_000L) {
    composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun compose_all_nav_destinations_to_exercise_animated_lambdas() {
    waitForHome(timeoutMs = 15_000)
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()
    // ... rest of test
  }

  @Test
  fun skills_navigation_opens_subject_list() {
    waitForHome(timeoutMs = 15_000)
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)
    // ... rest of test
  }
  // ... other tests remain the same
}
