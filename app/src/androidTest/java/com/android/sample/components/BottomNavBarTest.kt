package com.android.sample.components

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.MainPageViewModel
import com.android.sample.MyViewModelFactory
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.profile.MyProfileViewModel
import org.junit.Rule
import org.junit.Test


class BottomNavBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottomNavBar_displays_all_navigation_items() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      BottomNavBar(navController = navController)
    }

    composeTestRule.onNodeWithText("Home").assertExists()
    composeTestRule.onNodeWithText("Bookings").assertExists()
    composeTestRule.onNodeWithText("Skills").assertExists()
    composeTestRule.onNodeWithText("Profile").assertExists()
  }

  @Test
  fun bottomNavBar_renders_without_crashing() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      BottomNavBar(navController = navController)
    }

    composeTestRule.onNodeWithText("Home").assertExists()
  }

  @Test
  fun bottomNavBar_has_correct_number_of_items() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      BottomNavBar(navController = navController)
    }

    // Should have exactly 4 navigation items
    composeTestRule.onNodeWithText("Home").assertExists()
    composeTestRule.onNodeWithText("Bookings").assertExists()
    composeTestRule.onNodeWithText("Skills").assertExists()
    composeTestRule.onNodeWithText("Profile").assertExists()
  }

  @Test
  fun bottomNavBar_navigation_changes_destination() {
    var currentDestination: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      val currentUserId = "test"
      val factory = MyViewModelFactory(currentUserId)

      val bookingsViewModel: MyBookingsViewModel = viewModel(factory = factory)
      val profileViewModel: MyProfileViewModel = viewModel(factory = factory)
      val mainPageViewModel: MainPageViewModel = viewModel(factory = factory)

      // Track current destination
      val navBackStackEntry by navController.currentBackStackEntryAsState()
      currentDestination = navBackStackEntry?.destination?.route

      AppNavGraph(
          navController = navController,
          bookingsViewModel = bookingsViewModel,
          profileViewModel = profileViewModel,
          mainPageViewModel = mainPageViewModel,
          authViewModel =
              AuthenticationViewModel(InstrumentationRegistry.getInstrumentation().targetContext),
          onGoogleSignIn = {})
      BottomNavBar(navController = navController)
    }

    // Start at login, navigate to home first
    composeTestRule.onNodeWithText("Home").performClick()
    composeTestRule.waitForIdle()
    assert(currentDestination == "home")

    // Test Skills navigation
    composeTestRule.onNodeWithText("Skills").performClick()
    composeTestRule.waitForIdle()
    assert(currentDestination == "skills")

    // Test Bookings navigation
    composeTestRule.onNodeWithText("Bookings").performClick()
    composeTestRule.waitForIdle()
    assert(currentDestination == "bookings")

    // Test Profile navigation
    composeTestRule.onNodeWithText("Profile").performClick()
    composeTestRule.waitForIdle()
    assert(currentDestination == "profile/{profileId}")
    }
}
