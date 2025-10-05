package com.android.sample.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph
import org.junit.Rule
import org.junit.Test

/**
 * NavigationTests
 *
 * Instrumented UI tests for verifying navigation functionality within the Jetpack Compose
 * navigation framework.
 *
 * These tests:
 * - Verify that the home screen is displayed by default.
 * - Verify that tapping bottom navigation items changes the screen.
 *
 * NOTE:
 * - These are instrumentation tests (run on device/emulator).
 * - Place this file under app/src/androidTest/java.
 */
class NavigationTestsWithPlaceHolderScreens {

  // Compose test rule — handles launching composables and simulating user input.
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun app_launches_with_home_screen_displayed() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      AppNavGraph(navController = navController)
    }

    // Verify the home screen placeholder text is visible
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertIsDisplayed()
  }

  @Test
  fun clicking_profile_tab_navigates_to_profile_screen() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      Scaffold(bottomBar = { BottomNavBar(navController) }) { paddingValues ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
          AppNavGraph(navController = navController)
        }
      }
    }

    // Click on the "Profile" tab in the bottom navigation bar
    composeTestRule.onNodeWithText("Profile").performClick()

    // Verify the Profile screen placeholder text appears
    composeTestRule.onNodeWithText("👤 Profile Screen Placeholder").assertIsDisplayed()
  }

  @Test
  fun clicking_skills_tab_navigates_to_skills_screen() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      Scaffold(bottomBar = { BottomNavBar(navController) }) { paddingValues ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
          AppNavGraph(navController = navController)
        }
      }
    }

    // Click on the "Skills" tab
    composeTestRule.onNodeWithText("Skills").performClick()

    // Verify the Skills screen placeholder text appears
    composeTestRule.onNodeWithText("💡 Skills Screen Placeholder").assertIsDisplayed()
  }

  /** Test that the back button is NOT visible on root-level destinations (Home, Skills, Profile) */
  @Test
  fun topBar_backButton_isNotVisible_onRootScreens() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      Scaffold(bottomBar = { BottomNavBar(navController) }) { paddingValues ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
          AppNavGraph(navController = navController)
        }
      }
    }

    val backButton = composeTestRule.onAllNodesWithContentDescription("Back")

    // On Home screen (root)
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()
    backButton.assertCountEquals(0)

    // Navigate to Profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithText("👤 Profile Screen Placeholder").assertIsDisplayed()
    backButton.assertCountEquals(0)

    // Navigate to Skills
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.onNodeWithText("💡 Skills Screen Placeholder").assertIsDisplayed()
    backButton.assertCountEquals(0)
  }

  /**
   * Test that pressing the system back button on a non-root screen navigates back to the previous
   * screen.
   *
   * This test:
   * - Navigates to Profile screen
   * - Simulates a system back press
   * - Verifies we return to the Home screen
   */
  @Test
  fun topBar_backButton_navigatesFromSettingsToHome() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      Scaffold(
          topBar = { TopAppBar(navController) }, bottomBar = { BottomNavBar(navController) }) {
              paddingValues ->
            androidx.compose.foundation.layout.Box(modifier = Modifier.padding(paddingValues)) {
              AppNavGraph(navController = navController)
            }
          }
    }

    // Wait for Compose UI to initialize
    composeTestRule.waitForIdle()

    // Verify we start on Home
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()

    // Click the Settings tab in the bottom nav
    composeTestRule.onNodeWithText("Settings").performClick()

    // Verify Settings screen is displayed
    composeTestRule
        .onNodeWithText("⚙️ Settings Screen Placeholder")
        .assertExists()
        .assertIsDisplayed()

    // Verify TopAppBar back button is visible
    val backButton = composeTestRule.onNodeWithContentDescription("Back")
    backButton.assertExists()
    backButton.assertIsDisplayed()

    // Click the back button
    backButton.performClick()

    // Verify we are back on Home
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()
  }
}
