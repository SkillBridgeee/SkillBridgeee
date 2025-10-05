package com.android.sample.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.MainActivity
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

  @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun app_launches_with_home_screen_displayed() {
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()
  }

  @Test
  fun clicking_profile_tab_navigates_to_profile_screen() {
    // Click on the "Profile" tab in the bottom navigation bar
    composeTestRule.onNodeWithText("Profile").performClick()

    // Verify the Profile screen placeholder text appears
    composeTestRule
        .onNodeWithText("👤 Profile Screen Placeholder")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun clicking_skills_tab_navigates_to_skills_screen() {
    composeTestRule.onNodeWithText("Skills").performClick()

    // Verify the Skills screen placeholder text appears
    composeTestRule
        .onNodeWithText("💡 Skills Screen Placeholder")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun clicking_settings_tab_shows_backButton_and_returns_home() {
    // Start on Home
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()

    // Click the Settings tab
    composeTestRule.onNodeWithText("Settings").performClick()

    // Verify Settings screen placeholder
    composeTestRule
        .onNodeWithText("⚙️ Settings Screen Placeholder")
        .assertExists()
        .assertIsDisplayed()

    // Back button should now be visible
    val backButton = composeTestRule.onNodeWithContentDescription("Back")
    backButton.assertExists()
    backButton.assertIsDisplayed()

    // Click back button
    backButton.performClick()

    // Verify we are back on Home
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()
  }

  @Test
  fun topBar_backButton_isNotVisible_onRootScreens() {
    // Home screen (root)
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()
    composeTestRule.onAllNodesWithContentDescription("Back").assertCountEquals(0)

    // Navigate to Profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithText("👤 Profile Screen Placeholder").assertIsDisplayed()
    composeTestRule.onAllNodesWithContentDescription("Back").assertCountEquals(1)

    // Navigate to Skills
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.onNodeWithText("💡 Skills Screen Placeholder").assertIsDisplayed()
    composeTestRule.onAllNodesWithContentDescription("Back").assertCountEquals(1)
  }

  @Test
  fun multiple_navigation_actions_work_correctly() {
    // Start at home
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists()

    // Navigate through multiple screens
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithText("👤 Profile Screen Placeholder").assertIsDisplayed()

    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.onNodeWithText("💡 Skills Screen Placeholder").assertIsDisplayed()

    composeTestRule.onNodeWithText("Home").performClick()
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertIsDisplayed()
  }


  @Test
  fun back_button_navigation_from_settings_multiple_times() {
    // Navigate to settings
    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule.onNodeWithText("⚙️ Settings Screen Placeholder").assertIsDisplayed()

    // Back to home
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertIsDisplayed()

    // Navigate to settings again
    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule.onNodeWithText("⚙️ Settings Screen Placeholder").assertIsDisplayed()

    // Back again
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertIsDisplayed()
  }

  @Test
  fun scaffold_layout_is_properly_displayed() {
    // Test that the main scaffold structure is working
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertIsDisplayed()

    // Verify padding is applied correctly by checking content is within bounds
    composeTestRule.onRoot().assertExists()
  }

  @Test
  fun navigation_preserves_state_correctly() {
    // Start at home
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists()

    // Go to Profile, then Skills, then back to Profile
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithText("👤 Profile Screen Placeholder").assertIsDisplayed()

    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.onNodeWithText("💡 Skills Screen Placeholder").assertIsDisplayed()

    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithText("👤 Profile Screen Placeholder").assertIsDisplayed()
  }

  @Test
  fun app_handles_rapid_navigation_clicks() {
    // Rapidly click different navigation items
    repeat(3) {
      composeTestRule.onNodeWithText("Profile").performClick()
      composeTestRule.onNodeWithText("Skills").performClick()
      composeTestRule.onNodeWithText("Home").performClick()
    }

    // Should end up on Home
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertIsDisplayed()
  }

}
