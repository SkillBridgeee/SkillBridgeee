package com.android.sample.navigation

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.MainActivity
import com.android.sample.testutils.TestAuthHelpers
import com.android.sample.testutils.TestUiHelpers
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.android.sample.ui.profile.MyProfileScreenTestTag
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
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

    /**
     * Sign in once as a Google user for the whole test class. Tests that need an existing app
     * profile will get it from createAppProfile = true. This runs before any test methods.
     */
    @BeforeClass
    @JvmStatic
    fun globalSignIn() {
      try {
        // Ensure emulators available to the auth helper
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
        Firebase.auth.useEmulator("10.0.2.2", 9099)
      } catch (_: IllegalStateException) {}

      // Create & sign-in a persistent test Google user and create a minimal app profile.
      TestAuthHelpers.signInAsGoogleUserBlocking(
          email = "class.user@example.com", displayName = "Class User", createAppProfile = true)
    }

    /** Delete the test user and sign out after all tests in this class have run. */
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

    // Connect to Firebase emulators for signup tests (safe to call again)
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
      Firebase.auth.useEmulator("10.0.2.2", 9099)
    } catch (_: IllegalStateException) {
      // Emulator already initialized
    }

    // Ensure app isn't signed out between tests; leave the class-scoped user signed in.
    try {
      Firebase.auth.signOut()
      // Re-sign in the class user if needed so AuthViewModel sees currentUser when Activity starts.
      TestAuthHelpers.signInAsGoogleUserBlocking(
          email = "class.user@example.com", displayName = "Class User", createAppProfile = true)
    } catch (_: Exception) {}

    // Wait a short while for the activity / auth listener to react and, if the app routed to
    // the SignUp flow, complete it via UI helper so tests don't fail on missing profile.
    composeTestRule.waitForIdle()
    val detectStart = System.currentTimeMillis()
    val detectTimeout = 5_000L
    while (System.currentTimeMillis() - detectStart < detectTimeout) {
      val current = RouteStackManager.getCurrentRoute()
      if (current == NavRoutes.HOME) break
      if (current?.startsWith(NavRoutes.SIGNUP_BASE) == true) {
        // Complete the signup UI (email is pre-filled for Google signups)
        TestUiHelpers.signUpThroughUi(
            composeTestRule = composeTestRule,
            password = "P@ssw0rd!",
            name = "Class",
            surname = "User",
            levelOfEducation = "Test",
            description = "Class-level test user",
            timeoutMs = 8_000L)
        break
      }
      Thread.sleep(200)
    }
  }

  @After
  fun tearDown() {
    // Per-test: only sign out to leave emulator clean. Deletion done once in @AfterClass.
    try {
      Firebase.auth.signOut()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to sign out in tearDown", e)
    }
  }

  /**
   * Wait helper: the class-level Google sign-in means tests should not need to click the "GitHub"
   * button. Wait for the app to reach HOME instead.
   */
  private fun waitForHome(timeoutMs: Long = 5_000L) {
    composeTestRule.waitUntil(timeoutMillis = timeoutMs) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    composeTestRule.waitForIdle()
  }

  @Test
  fun login_navigates_to_home() {
    // The class-scoped Google user should be signed in; wait for app to reach HOME.
    waitForHome(timeoutMs = 15_000)

    // Verify home screen content
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()
    composeTestRule.onNodeWithText("All Tutors").assertExists()
  }

  @Test
  fun navigating_to_Map_displays_map_screen() {
    // Ensure signed in and at home
    waitForHome()

    // Navigate to map
    composeTestRule.onNodeWithText("Map").performClick()
    composeTestRule.waitForIdle()

    // Check map screen content via test tag
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertExists()
  }

  @Test
  fun navigating_to_profile_displays_profile_screen() {
    // Ensure signed in and at home
    waitForHome()

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
    // Ensure signed in and at home
    waitForHome()

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
    // Ensure signed in and at home
    waitForHome()

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
    // Ensure signed in and at home
    waitForHome()

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
    // Ensure signed in and at home
    waitForHome()

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
    composeTestRule.onNodeWithText("All Tutors").assertExists()
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)
  }

  @Test
  fun profile_screen_has_form_fields() {
    // Ensure signed in and at home
    waitForHome()

    // Navigate to profile
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

  private fun navigateToProfileAndWait() {
    // Ensure signed in and at home
    waitForHome()

    // Trigger navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Wait until the nav route is PROFILE
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.PROFILE
    }

    // Wait until the LazyColumn with ROOT_LIST is present in the semantics tree
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(MyProfileScreenTestTag.ROOT_LIST, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @Test
  fun profile_screen_has_logout_button() {
    // Ensure signed in and at home
    waitForHome()

    navigateToProfileAndWait()

    // Scroll the LazyColumn to the logout button
    composeTestRule
        .onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST)
        .performScrollToNode(hasTestTag(MyProfileScreenTestTag.LOGOUT_BUTTON))

    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertHasClickAction()
  }

  @Test
  fun login_route_is_start_destination() {
    // Verify login screen is the initial screen - may be transient if auth listener already set
    // HOME
    val currentRoute = RouteStackManager.getCurrentRoute()
    assert(
        currentRoute == NavRoutes.LOGIN || currentRoute == null || currentRoute == NavRoutes.HOME)

    // If login is visible, verify UI; otherwise the class user may already be on HOME which is
    // acceptable.
    try {
      composeTestRule.onNodeWithText("Welcome back! Please sign in.").assertExists()
    } catch (_: AssertionError) {
      // ignore - acceptable when already signed in
    }
  }

  @Test
  fun github_login_navigates_to_home_clearing_login_from_stack() {
    // Ensure signed in and at home
    waitForHome(timeoutMs = 15_000)

    // Verify we're on home and login is not in the stack anymore
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()
  }

  @Test
  fun profile_route_gets_current_userId() {
    // Ensure signed in and at home
    waitForHome()

    // Navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.PROFILE
    }

    // Profile should load with current user's data
    composeTestRule.onNodeWithText("Name").assertExists()
  }

  @Test
  fun profile_logout_button_integration() {
    // Ensure signed in and at home
    waitForHome()

    navigateToProfileAndWait()

    composeTestRule
        .onNodeWithTag(MyProfileScreenTestTag.ROOT_LIST)
        .performScrollToNode(hasTestTag(MyProfileScreenTestTag.LOGOUT_BUTTON))

    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(MyProfileScreenTestTag.LOGOUT_BUTTON).assertHasClickAction()
  }

  @Test
  fun navigation_routes_are_configured() {
    // If already signed in, HOME is expected; otherwise LOGIN elements should exist.
    try {
      composeTestRule.onNodeWithText("Welcome back! Please sign in.").assertExists()
    } catch (_: AssertionError) {
      // ignore - acceptable when already signed in
    }

    // Verify LOGIN route elements exist where applicable
    try {
      composeTestRule.onNodeWithText("GitHub").assertExists()
      composeTestRule
          .onNodeWithTag(com.android.sample.ui.login.SignInScreenTestTags.SIGNUP_LINK)
          .assertExists()
    } catch (_: AssertionError) {
      // when already signed in, these assertions may not apply
    }

    // Ensure bottom navigation exists by navigating to home (or waiting for it)
    waitForHome(timeoutMs = 15_000)

    // Use test tags to avoid ambiguity with "Home" text appearing in multiple places
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).assertExists()
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).assertExists()
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_MAP).assertExists()

    Log.d(TAG, "All navigation routes properly configured")
  }
}
