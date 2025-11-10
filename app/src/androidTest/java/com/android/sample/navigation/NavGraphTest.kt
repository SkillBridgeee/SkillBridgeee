package com.android.sample.navigation

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.MainActivity
import com.android.sample.testutils.TestAuthHelpers
import com.android.sample.ui.map.MapScreenTestTags
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

class AppNavGraphTest {

  companion object {
    private const val TAG = "AppNavGraphTest"

    @BeforeClass
    @JvmStatic
    fun globalSignIn() {
      try {
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } catch (_: IllegalStateException) {}

      TestAuthHelpers.signInAsGoogleUserBlocking(
          email = "class.user@example.com", displayName = "Class User", createAppProfile = true)
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
  fun setUp() {
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
    composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun login_navigates_to_home() {
    waitForHome(timeoutMs = 15_000)
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()
    composeTestRule.onNodeWithText("All Tutors").assertExists()
  }

  @Test
  fun navigating_to_Map_displays_map_screen() {
    waitForHome()
    composeTestRule.onNodeWithText("Map").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertExists()
  }
  // ... other tests remain the same
}
