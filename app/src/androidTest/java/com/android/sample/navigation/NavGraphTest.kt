package com.android.sample.navigation

import android.util.Log
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.MainActivity
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import org.junit.After
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
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
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

    // Use RouteStackManager to verify navigation instead of checking UI text
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }

    // Verify we're on home screen
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)
  }

  @Test
  fun navigating_to_skills_displays_skills_screen() {
    // First login to get to main app
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to skills
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()

    // Use RouteStackManager to verify navigation
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS
    }

    // Verify we're on skills screen using test tag instead of UI text
    composeTestRule.onNodeWithTag("SubjectListTestTags.SEARCHBAR").assertExists()
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
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
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
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.BOOKINGS
    }

    // Wait for bookings screen to render - either cards or empty state will appear
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
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

    // Navigate to skills
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()

    // Wait for skills route to be set
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS)

    // Navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Wait for profile route to be set - no Thread.sleep needed!
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.PROFILE
    }

    assert(RouteStackManager.getCurrentRoute() == NavRoutes.PROFILE)
  }

  @Test
  fun bottom_nav_resets_stack_correctly() {
    // Login
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to skills then profile
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Navigate back to home via bottom nav
    composeTestRule.onNodeWithText("Home").performClick()
    composeTestRule.waitForIdle()

    // Use RouteStackManager to verify we're back on home
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      RouteStackManager.getCurrentRoute() == NavRoutes.HOME
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)
  }

  @Test
  fun skills_screen_has_search_and_category() {
    // Login and navigate to skills
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()

    // Use test tags instead of UI text for more robust assertions
    composeTestRule.onNodeWithTag("SubjectListTestTags.SEARCHBAR").assertExists()
    composeTestRule.onNodeWithTag("SubjectListTestTags.CATEGORY_SELECTOR").assertExists()
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
  }
}
