package com.android.sample.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.MainActivity
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

    // Wait for login screen to be fully loaded
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule
          .onAllNodes(hasText("Welcome back! Please sign in."))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  @After
  fun tearDown() {
    // Clean up: delete the test user if created
    try {
      Firebase.auth.currentUser?.delete()
    } catch (_: Exception) {
      // Ignore deletion errors
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
    composeTestRule.onNodeWithText("Explore skills").assertExists()
    composeTestRule.onNodeWithText("Top-Rated Tutors").assertExists()
  }

  @Test
  fun navigating_to_skills_displays_skills_screen() {
    // First login to get to main app
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to skills
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()

    // Should display skills screen content
    composeTestRule.onNodeWithText("Find a tutor about...").assertExists()
  }

  @Test
  fun navigating_to_profile_displays_profile_screen() {
    // Login first
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Wait for profile screen to fully load before asserting
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onAllNodes(hasText("Personal Details")).fetchSemanticsNodes().isNotEmpty()
    }

    // Should display profile screen - check for profile screen elements
    composeTestRule.onNodeWithText("Student").assertExists()
    composeTestRule.onNodeWithText("Personal Details").assertExists()
    composeTestRule.onNodeWithText("Save Profile Changes").assertExists()
  }

  @Test
  fun navigating_to_bookings_displays_bookings_screen() {
    // Login first
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Navigate to bookings
    composeTestRule.onNodeWithText("Bookings").performClick()
    composeTestRule.waitForIdle()

    // Should display bookings screen
    composeTestRule.onNodeWithText("My Bookings").assertExists()
  }

  @Test
  fun navigating_to_new_skill_from_home() {
    // Login first
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Click the add skill button on home screen (FAB)
    composeTestRule.onNodeWithContentDescription("Add").performClick()
    composeTestRule.waitForIdle()

    // Should navigate to new skill screen
    composeTestRule.onNodeWithText("Create Your Lessons !").assertExists()
  }

  @Test
  fun routeStackManager_updates_on_navigation() {
    // Login
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    // Wait for home screen to fully load
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule
          .onAllNodes(hasText("Ready to learn something new today?"))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)

    // Navigate to skills
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()

    // Wait for skills screen to load
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodes(hasText("Find a tutor about..."))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS)

    // Navigate to profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Wait for profile screen to load (more time as it loads user data)
    composeTestRule.waitUntil(timeoutMillis = 15000) {
      composeTestRule.onAllNodes(hasText("Personal Details")).fetchSemanticsNodes().isNotEmpty()
    }

    // Give extra time for async profile loading to complete
    Thread.sleep(1000)
    composeTestRule.waitForIdle()

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

    // Should be on home screen - check for actual home content
    composeTestRule.onNodeWithText("Ready to learn something new today?").assertExists()
    composeTestRule.onNodeWithText("Explore skills").assertExists()
    composeTestRule.onNodeWithText("Top-Rated Tutors").assertExists()
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.HOME)
  }

  @Test
  fun skills_screen_has_search_and_category() {
    // Login and navigate to skills
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()

    // Verify skills screen components
    composeTestRule.onNodeWithText("Find a tutor about...").assertExists()
    composeTestRule.onNodeWithText("Category").assertExists()
  }

  @Test
  fun profile_screen_has_form_fields() {
    // Login and navigate to profile
    composeTestRule.onNodeWithText("GitHub").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()

    // Wait for profile to fully load
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule.onAllNodes(hasText("Personal Details")).fetchSemanticsNodes().isNotEmpty()
    }

    // Verify profile form fields exist
    composeTestRule.onNodeWithText("Name").assertExists()
    composeTestRule.onNodeWithText("Email").assertExists()
    composeTestRule.onNodeWithText("Location / Campus").assertExists()
    composeTestRule.onNodeWithText("Description").assertExists()
  }
}
