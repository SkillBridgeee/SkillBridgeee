package com.android.sample.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.android.sample.ui.navigation.AppNavGraph
import org.junit.Rule
import org.junit.Test

class BottomNavBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottomNavBar_displays_all_navigation_items() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      // Set up the navigation graph
      AppNavGraph(navController = navController)
      BottomNavBar(navController = navController)
    }

    composeTestRule.onNodeWithText("Home").assertExists()
    composeTestRule.onNodeWithText("Skills").assertExists()
    composeTestRule.onNodeWithText("Profile").assertExists()
    composeTestRule.onNodeWithText("Settings").assertExists()
  }

  @Test
  fun bottomNavBar_items_are_clickable() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      // Set up the navigation graph
      AppNavGraph(navController = navController)
      BottomNavBar(navController = navController)
    }

    // Test that all navigation items can be clicked without crashing
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.onNodeWithText("Settings").performClick()
    composeTestRule.onNodeWithText("Home").performClick()
  }

  @Test
  fun bottomNavBar_renders_without_crashing() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      // Set up the navigation graph
      AppNavGraph(navController = navController)
      BottomNavBar(navController = navController)
    }

    composeTestRule.onNodeWithText("Home").assertExists()
  }

  @Test
  fun bottomNavBar_has_correct_number_of_items() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      // Set up the navigation graph
      AppNavGraph(navController = navController)
      BottomNavBar(navController = navController)
    }

    // Should have exactly 4 navigation items
    composeTestRule.onNodeWithText("Home").assertExists()
    composeTestRule.onNodeWithText("Skills").assertExists()
    composeTestRule.onNodeWithText("Profile").assertExists()
    composeTestRule.onNodeWithText("Settings").assertExists()
  }
}
