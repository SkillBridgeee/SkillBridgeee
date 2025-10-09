package com.android.sample.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.MainActivity
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
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
  }

  @Test
  fun startDestination_is_home() {
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()
  }

  @Test
  fun navigating_to_skills_displays_skills_screen() {
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule
        .onNodeWithText("💡 Skills Screen Placeholder")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun navigating_to_profile_displays_profile_screen() {
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule
        .onNodeWithText("👤 Profile Screen Placeholder")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun navigating_to_settings_displays_settings_screen() {
    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule
        .onNodeWithText("⚙️ Settings Screen Placeholder")
        .assertExists()
        .assertIsDisplayed()
  }

  @Test
  fun navigating_to_piano_and_piano2_screens_displays_correct_content() {
    // Navigate to Skills
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.onNodeWithText("💡 Skills Screen Placeholder").assertIsDisplayed()

    // Click button -> Go to Piano
    composeTestRule.onNodeWithText("Go to Piano").performClick()
    composeTestRule.onNodeWithText("Piano Screen").assertIsDisplayed()

    // Click button -> Go to Piano 2
    composeTestRule.onNodeWithText("Go to Piano 2").performClick()
    composeTestRule.onNodeWithText("Piano 2 Screen").assertIsDisplayed()
  }

  @Test
  fun routeStackManager_updates_on_navigation() {
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.SKILLS)

    composeTestRule.onNodeWithText("Go to Piano").performClick()
    composeTestRule.waitForIdle()
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.PIANO_SKILL)

    composeTestRule.onNodeWithText("Go to Piano 2").performClick()
    composeTestRule.waitForIdle()
    assert(RouteStackManager.getCurrentRoute() == NavRoutes.PIANO_SKILL_2)
  }

  @Test
  fun back_navigation_from_piano2_returns_to_piano_then_skills_then_home() {
    // Skills -> Piano -> Piano 2
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.onNodeWithText("Go to Piano").performClick()
    composeTestRule.onNodeWithText("Go to Piano 2").performClick()

    // Verify on Piano 2
    composeTestRule.onNodeWithText("Piano 2 Screen").assertIsDisplayed()

    // Back → Piano
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.onNodeWithText("Piano Screen").assertIsDisplayed()

    // Back → Skills
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.onNodeWithText("💡 Skills Screen Placeholder").assertIsDisplayed()

    // Back → Home
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertIsDisplayed()
  }

  @Test
  fun navigating_between_main_tabs_resets_stack_correctly() {
    // Go to multiple main tabs
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithText("👤 Profile Screen Placeholder").assertIsDisplayed()

    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule.onNodeWithText("⚙️ Settings Screen Placeholder").assertIsDisplayed()

    // Back from Settings -> should go Home
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertIsDisplayed()

    // Stack should only contain HOME now
    val routes = RouteStackManager.getAllRoutes()
    assert(routes.lastOrNull() == NavRoutes.HOME)
    assert(!routes.contains(NavRoutes.SETTINGS))
  }
}
