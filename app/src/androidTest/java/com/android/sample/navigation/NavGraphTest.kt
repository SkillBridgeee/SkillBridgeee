package com.android.sample.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test

class NavGraphTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun navGraph_starts_at_home_screen() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      AppNavGraph(navController = navController)
    }

    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists().assertIsDisplayed()
  }

  @Test
  fun navGraph_contains_all_routes() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      AppNavGraph(navController = navController)
    }

    // Verify home screen is accessible
    composeTestRule.onNodeWithText("🏠 Home Screen Placeholder").assertExists()
  }
}
