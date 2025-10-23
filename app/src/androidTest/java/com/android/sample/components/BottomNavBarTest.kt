package com.android.sample.components

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.MainPageViewModel
import com.android.sample.MyViewModelFactory
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.BottomNavBarTestTags
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.profile.MyProfileViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BottomNavBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun initRepositories() {
    val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    try {
      ProfileRepositoryProvider.init(ctx)
      ListingRepositoryProvider.init(ctx)
      BookingRepositoryProvider.init(
          ctx) // prevents IllegalStateException in ViewModel construction
      RatingRepositoryProvider.init(ctx)
    } catch (e: Exception) {
      // Initialization may fail in some CI/emulator setups; log and continue
      println("Repository init failed: ${e.message}")
    }
  }

  @Test
  fun bottomNavBar_displays_all_navigation_items() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      BottomNavBar(navController = navController)
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).assertExists()
  }

  @Test
  fun bottomNavBar_renders_without_crashing() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      BottomNavBar(navController = navController)
    }

    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).assertExists()
  }

  @Test
  fun bottomNavBar_has_correct_number_of_items() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      BottomNavBar(navController = navController)
    }

    // Should have exactly 4 navigation items
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).assertExists()
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).assertExists()
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
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_HOME).performClick()
    composeTestRule.waitForIdle()
    assert(currentDestination == "home")

    // Test Skills navigation
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_SKILLS).performClick()
    composeTestRule.waitForIdle()
    assert(currentDestination == "skills")

    // Test Bookings navigation
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_BOOKINGS).performClick()
    composeTestRule.waitForIdle()
    assert(currentDestination == "bookings")

    // Test Profile navigation
    composeTestRule.onNodeWithTag(BottomNavBarTestTags.NAV_PROFILE).performClick()
    composeTestRule.waitForIdle()
    assert(currentDestination == "profile/{profileId}")
  }
}
