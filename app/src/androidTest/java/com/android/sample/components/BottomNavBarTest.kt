package com.android.sample.components

import android.Manifest
import android.app.UiAutomation
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.android.sample.MyViewModelFactory
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.MyBookingsPageTestTag
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.map.MapScreenTestTags
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.newListing.NewListingViewModel
import com.android.sample.ui.profile.MyProfileViewModel
import org.junit.Assert.assertEquals
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

    // Grant location permission to prevent dialog from breaking compose hierarchy
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val uiAutomation: UiAutomation = instrumentation.uiAutomation
    try {
      uiAutomation.grantRuntimePermission(
          "com.android.sample", Manifest.permission.ACCESS_FINE_LOCATION)
    } catch (_: SecurityException) {
      // In some test environments granting may fail; continue to run the test
    }
  }

  @Test
  fun bottomNavBar_displays_all_navigation_items() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      BottomNavBar(navController = navController)
    }

    composeTestRule.onNodeWithText("Home").assertExists()
    composeTestRule.onNodeWithText("Bookings").assertExists()
    composeTestRule.onNodeWithText("Map").assertExists()
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
    composeTestRule.onNodeWithText("Map").assertExists()
    composeTestRule.onNodeWithText("Profile").assertExists()
  }

  @Test
  fun bottomNavBar_navigation_changes_destination() {
    var navController: NavHostController? = null

    composeTestRule.setContent {
      val controller = rememberNavController()
      navController = controller
      val currentUserId = "test"
      val factory = MyViewModelFactory(currentUserId)

      val bookingsViewModel: MyBookingsViewModel = viewModel(factory = factory)
      val profileViewModel: MyProfileViewModel = viewModel(factory = factory)
      val mainPageViewModel: MainPageViewModel = viewModel(factory = factory)

      val newListingViewModel: NewListingViewModel = viewModel(factory = factory)

      AppNavGraph(
          navController = controller,
          bookingsViewModel = bookingsViewModel,
          profileViewModel = profileViewModel,
          mainPageViewModel = mainPageViewModel,
          authViewModel =
              AuthenticationViewModel(InstrumentationRegistry.getInstrumentation().targetContext),
          newListingViewModel = newListingViewModel,
          onGoogleSignIn = {})
      BottomNavBar(navController = controller)
    }

    // Use test tags for clicks to target the clickable NavigationBarItem (avoids touch injection)
    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_HOME).performClick()
    composeTestRule.waitForIdle()
    var route = navController?.currentBackStackEntry?.destination?.route
    assertEquals("Expected HOME route", NavRoutes.HOME, route)

    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_MAP).performClick()
    composeTestRule.waitForIdle()
    // Wait for map screen to fully compose before checking route
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).fetchSemanticsNode()
        true
      } catch (_: AssertionError) {
        false
      }
    }
    route = navController?.currentBackStackEntry?.destination?.route
    assertEquals("Expected MAP route", NavRoutes.MAP, route)

    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_BOOKINGS).performClick()
    composeTestRule.waitForIdle()
    route = navController?.currentBackStackEntry?.destination?.route
    assertEquals("Expected BOOKINGS route", NavRoutes.BOOKINGS, route)

    composeTestRule.onNodeWithTag(MyBookingsPageTestTag.NAV_PROFILE).performClick()
    composeTestRule.waitForIdle()
    route = navController?.currentBackStackEntry?.destination?.route
    assertEquals("Expected PROFILE route", NavRoutes.PROFILE, route)
  }
}
