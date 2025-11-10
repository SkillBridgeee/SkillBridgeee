package com.android.sample.navigation

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.MainActivity
import com.android.sample.model.authentication.AuthState
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.testutils.TestAuthHelpers
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignUpNavigationTest {

  companion object {
    private const val TAG = "SignUpNavigationTest"
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    // Ensure emulator is configured and no user is signed in for these tests
    try {
      TestAuthHelpers.signOut()
    } catch (e: Exception) {}

    RouteStackManager.clear()
    // Allow the activity/auth listener to settle
    composeTestRule.waitForIdle()
  }

  @After
  fun tearDown() {
    // Keep test isolation by signing out after each test
    try {
      TestAuthHelpers.signOut()
    } catch (e: Exception) {}
  }

  @Test
  fun userSessionManager_tracks_authentication_state() {
    val initialState = runBlocking { UserSessionManager.authState.first() }
    Assert.assertTrue(
        "Initial state should be Unauthenticated or Loading",
        initialState is AuthState.Unauthenticated || initialState is AuthState.Loading)

    val initialUserId = UserSessionManager.getCurrentUserId()
    Assert.assertTrue("User ID should be null when not authenticated", initialUserId == null)

    Log.d(TAG, "UserSessionManager correctly tracks unauthenticated state")
  }

  @Test
  fun navigating_to_signup_from_login() {
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute()?.startsWith(NavRoutes.SIGNUP_BASE) == true
    }
  }
}
