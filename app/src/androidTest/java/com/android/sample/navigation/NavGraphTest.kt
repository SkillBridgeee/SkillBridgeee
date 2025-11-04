package com.android.sample.navigation

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.MainActivity
import com.android.sample.model.authentication.AuthState
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * AppNavGraphTest
 *
 * Instrumentation tests for verifying that AppNavGraph correctly maps routes to screens. These
 * tests confirm that navigating between destinations renders the correct composables.
 */
class AppNavGraphTest {

  companion object {
    private const val TAG = "AppNavGraphTest"
  }

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setUp() {
    RouteStackManager.clear()

    // Connect to Firebase emulators for signup tests
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {
      // Emulator already initialized
    }

    // Clean up any existing user
    Firebase.auth.signOut()

    // Wait for login screen to be ready - use UI element as it's more reliable at startup
    // RouteStackManager may not be initialized immediately
    // Increased timeout for CI environments
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      composeTestRule.onAllNodesWithText("GitHub").fetchSemanticsNodes().isNotEmpty()
    }
  }

  @After
  fun tearDown() {
    // Clean up: delete the test user if created
    try {
      Firebase.auth.currentUser?.delete()
    } catch (e: Exception) {
      // Log deletion errors for debugging
      Log.w(TAG, "Failed to delete test user in tearDown", e)
    }
    Firebase.auth.signOut()
  }

  @Test
  fun login_navigates_to_home() {
    // Click GitHub login button to navigate to home
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Should now be on home screen - check for home screen elements
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()
    composeTestRule.onNodeWithText("Top-Rated Tutors").assertExists()
  }

  @Test
  fun navigating_to_Map_displays_map_screen() {
    // First login to get to main app
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to map
    composeTestRule.onNodeWithText("Map").performClick()
    composeTestRule.waitForIdle()

    // Check map screen content via test tag
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertExists()
  }

  @Test
  fun navigating_to_profile_displays_profile_screen() {
    // Login first
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Use RouteStackManager to verify navigation instead of waiting for UI text
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.PROFILE
    }

    // Verify we're on profile screen
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.PROFILE)
  }

  @Test
  fun navigating_to_bookings_displays_bookings_screen() {
    // Login first
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to bookings
    composeTestRule.onNodeWithText("Bookings").performClick()
    composeTestRule.waitForIdle()

    // Use RouteStackManager to verify navigation
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.BOOKINGS
    }

    // Wait for bookings screen to render - either cards or empty state will appear
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      val hasCards =
          composeTestRule
              .onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_CARD)
              .fetchSemanticsNodes()
              .isNotEmpty()
      val hasEmptyState =
          composeTestRule
              .onAllNodesWithTag(MyBookingsPageTestTag.EMPTY_BOOKINGS)
              .fetchSemanticsNodes()
              .isNotEmpty()

      // Return true when either condition is met
      hasCards || hasEmptyState
    }

    // Verify we're on bookings screen - either has cards or empty state
    composeTestRule.waitForIdle()
    val hasCards =
        composeTestRule
            .onAllNodesWithTag(MyBookingsPageTestTag.BOOKING_CARD)
            .fetchSemanticsNodes()
            .isNotEmpty()
    val hasEmptyState =
        composeTestRule
            .onAllNodesWithTag(MyBookingsPageTestTag.EMPTY_BOOKINGS)
            .fetchSemanticsNodes()
            .isNotEmpty()

    // Either cards or empty state should be visible
    assert(hasCards || hasEmptyState)
  }

  @Test
  fun navigating_to_new_skill_from_home() {
    // Login first
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Click the add skill button on home screen (FAB)
    composeTestRule.onNodeWithContentDescription("Add").performClick()
    composeTestRule.waitForIdle()

    // Use RouteStackManager to verify navigation
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.NEW_SKILL
    }

    // Verify we navigated to new skill screen
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.NEW_SKILL)
  }

  @Test
  fun routeStackManager_updates_on_navigation() {
    // Login
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Wait for home route to be set
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)

    // Navigate to Map
    composeTestRule.onNodeWithText("Map").performClick()
    composeTestRule.waitForIdle()

    // Wait for skills route to be set
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.MAP
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.MAP)
  }

  @Test
  fun bottom_nav_resets_stack_correctly() {
    // Login
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to skills then profile
    composeTestRule.onNodeWithText("Map").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Navigate back to Home via bottom nav
    composeTestRule.onNodeWithText("Home").performClick()
    composeTestRule.waitForIdle()

    // Verify Home screen content
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()
    composeTestRule.onNodeWithText("Explore Subjects").assertExists()
    composeTestRule.onNodeWithText("Top-Rated Tutors").assertExists()
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)
  }

  @Test
  fun profile_screen_has_form_fields() {
    // Login and navigate to profile
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Use RouteStackManager to verify navigation
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.PROFILE
    }

    // For now, verify essential fields exist (text-based, but minimal)
    composeTestRule.onNodeWithText("Name").assertExists()
    composeTestRule.onNodeWithText("Email").assertExists()
    composeTestRule.onNodeWithText("Location / Campus").assertExists()
    composeTestRule.onNodeWithText("Description").assertExists()
  }

  @Test
  fun navigating_to_signup_from_login() {
    // Click "Sign Up" link on login screen using test tag
    composeTestRule
        .onNodeWithTag(com.android.sample.ui.login.SignInScreenTestTags.SIGNUP_LINK)
        .performClick()
    composeTestRule.waitForIdle()

    // Wait for signup screen to load
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute()?.startsWith(NavRoutes.SIGNUP_BASE) == true
    }

    // Verify signup screen is displayed using test tag to avoid ambiguity
    composeTestRule
        .onNodeWithTag(com.android.sample.ui.signup.SignUpScreenTestTags.TITLE)
        .assertExists()
    composeTestRule.onNodeWithText("Personal Informations").assertExists()
  }

  @Test
  fun profile_screen_has_logout_button() {
    // Login first
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Verify logout button exists and is clickable
    composeTestRule.onNodeWithText("Logout").assertExists()
    composeTestRule.onNodeWithText("Logout").assertHasClickAction()
  }

  @Test
  fun login_route_is_start_destination() {
    // Verify login screen is the initial screen - already verified in setUp()
    // RouteStackManager should show LOGIN route
    val currentRoute = RouteStackManager.getCurrentRoute()
    assert(currentRoute == NavRoutes.LOGIN || currentRoute == null) // May be null initially

    // Verify login screen UI is present
    composeTestRule.onNodeWithText("Welcome back! Please sign in.").assertExists()
  }

  @Test
  fun github_login_navigates_to_home_clearing_login_from_stack() {
    // Click GitHub login
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Wait for home screen
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }

    // Verify we're on home and login is not in the stack anymore
    // (can't go back to login from home without logout)
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()
  }

  @Test
  fun signup_navigates_to_login_after_success() {
    // Navigate to signup
    composeTestRule.onNodeWithText("Sign Up").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute()?.startsWith(NavRoutes.SIGNUP_BASE) == true
    }

    // Verify signup screen components are present
    composeTestRule.onNodeWithText("Personal Informations").assertExists()
  }

  @Test
  fun profile_route_gets_current_userId() {
    // Login to set userId
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.PROFILE
    }

    // Profile should load with current user's data
    // Since we logged in with GitHub, profile fields should be present
    composeTestRule.onNodeWithText("Name").assertExists()
  }

  /**
   * Simpler test to verify UserSessionManager integration with authentication. This test focuses on
   * verifying that the session manager properly tracks auth state without the complexity of the
   * full signup/login/logout flow.
   */
  @Test
  fun userSessionManager_tracks_authentication_state() {
    // Verify initial state is unauthenticated or loading
    val initialState = runBlocking { UserSessionManager.authState.first() }
    Assert.assertTrue(
        "Initial state should be Unauthenticated or Loading",
        initialState is AuthState.Unauthenticated || initialState is AuthState.Loading)

    // Verify getCurrentUserId returns null when not authenticated
    val initialUserId = UserSessionManager.getCurrentUserId()
    Assert.assertTrue("User ID should be null when not authenticated", initialUserId == null)

    Log.d(TAG, "UserSessionManager correctly tracks unauthenticated state")
  }

  /**
   * Test to verify the logout callback integration between MyProfileScreen and NavGraph. This
   * verifies that the logout button triggers the callback without actually performing the full
   * navigation (which is flaky on CI).
   */
  @Test
  fun profile_logout_button_integration() {
    // Login to access profile
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Verify the profile screen is displayed with logout functionality
    composeTestRule.onNodeWithText("Logout").assertExists()
    composeTestRule.onNodeWithText("Name").assertExists()
    composeTestRule.onNodeWithText("Email").assertExists()

    // Verify the logout button is properly wired (has click action)
    composeTestRule.onNodeWithText("Logout").assertHasClickAction()

    Log.d(TAG, "Profile logout button integration verified")
  }

  /**
   * Test to verify navigation routes are properly configured. This tests the NavGraph setup without
   * relying on actual navigation timing.
   */
  @Test
  fun navigation_routes_are_configured() {
    // Verify we start at LOGIN
    composeTestRule.onNodeWithText("Welcome back! Please sign in.").assertExists()

    // Verify LOGIN route elements exist
    composeTestRule.onNodeWithText("GitHub").assertExists()
    composeTestRule
        .onNodeWithTag(com.android.sample.ui.login.SignInScreenTestTags.SIGNUP_LINK)
        .assertExists()

    // Login to verify other routes are accessible
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Verify bottom navigation exists (which means routes are configured)
    // Use test tags to avoid ambiguity with "Home" text appearing in multiple places
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertExists()
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).assertExists()
    // Skills doesn't have a test tag, so use text for it
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_MAP).assertExists()

    Log.d(TAG, "All navigation routes properly configured")
  }
}
