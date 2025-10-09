package com.android.sample.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule
import org.junit.Test

class TopAppBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun topAppBar_renders_without_crashing() {
    composeTestRule.setContent {
      TopAppBar(navController = NavHostController(ApplicationProvider.getApplicationContext()))
    }

    // Basic test that the component renders
    composeTestRule.onNodeWithText("SkillBridge").assertExists()
  }

  @Test
  fun topAppBar_shows_default_title_when_no_route() {
    composeTestRule.setContent {
      TopAppBar(navController = NavHostController(ApplicationProvider.getApplicationContext()))
    }

    // Should show default title when no route is set
    composeTestRule.onNodeWithText("SkillBridge").assertExists()
  }

  @Test
  fun topAppBar_displays_title() {
    composeTestRule.setContent {
      TopAppBar(navController = NavHostController(ApplicationProvider.getApplicationContext()))
    }

    // Test for the expected title text directly
    composeTestRule.onNodeWithText("SkillBridge").assertExists()
  }
}
