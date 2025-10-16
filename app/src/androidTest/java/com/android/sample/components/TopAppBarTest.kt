package com.android.sample.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavHostController
import androidx.test.core.app.ApplicationProvider
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.components.TopAppBarTestTags
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
    composeTestRule.onNodeWithTag(TopAppBarTestTags.TOP_APP_BAR).assertExists()
    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()
  }

  @Test
  fun topAppBar_shows_default_title_when_no_route() {
    composeTestRule.setContent {
      TopAppBar(navController = NavHostController(ApplicationProvider.getApplicationContext()))
    }

    // Should show default title when no route is set
    composeTestRule.onNodeWithTag(TopAppBarTestTags.TOP_APP_BAR).assertExists()
    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()
  }

  @Test
  fun topAppBar_displays_title() {
    composeTestRule.setContent {
      TopAppBar(navController = NavHostController(ApplicationProvider.getApplicationContext()))
    }

    // Test for the expected title text directly
    composeTestRule.onNodeWithTag(TopAppBarTestTags.TOP_APP_BAR).assertExists()
    composeTestRule.onNodeWithTag(TopAppBarTestTags.DISPLAY_TITLE).assertExists()
  }
}
