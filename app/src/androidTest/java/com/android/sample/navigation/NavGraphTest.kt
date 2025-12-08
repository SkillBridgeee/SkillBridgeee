package com.android.sample.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import com.android.sample.model.authentication.UserSessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavGraphTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var navController: TestNavHostController

  @Before
  fun setup() {
    composeRule.setContent {
      val context = LocalContext.current
      navController =
          TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            graph = createTestNavGraph(this)
          }
    }
  }

  @Test
  fun navigateToListing_navigatesToCorrectRoute() {
    val listingId = "listing123"

    composeRule.runOnIdle {
      // We start at dummy
      assertEquals("dummy", navController.currentDestination?.route)

      // Call the helper
      navigateToListing(navController, listingId)

      // 1) Route should be the LISTING pattern, not the filled path
      assertEquals(NavRoutes.LISTING, navController.currentDestination?.route)

      // 2) Argument should contain the concrete id we passed
      val args = navController.currentBackStackEntry?.arguments
      val argListingId = args?.getString("listingId")
      assertEquals(listingId, argListingId)
    }
  }

  @Test
  fun navigateToNewListing_navigatesWhenUserExists() {
    val userId = "user42"
    val listingId = "listing999"
    UserSessionManager.setCurrentUserId(userId)

    composeRule.runOnIdle {
      val originalRoute = navController.currentDestination?.route
      assertEquals("dummy", originalRoute)

      // Call the helper
      navigateToNewListing(navController, listingId)

      // 1) Route pattern should match NEW_SKILL
      assertNotEquals(originalRoute, navController.currentDestination?.route)
      assertEquals(NavRoutes.NEW_SKILL, navController.currentDestination?.route)

      // 2) Arguments should contain the correct profileId + listingId
      val args = navController.currentBackStackEntry?.arguments
      val argProfileId = args?.getString("profileId")
      val argListingId = args?.getString("listingId")

      assertEquals(userId, argProfileId)
      assertEquals(listingId, argListingId)
    }
  }

  private fun createTestNavGraph(navController: NavHostController): NavGraph {
    return navController.createGraph(startDestination = "dummy") {
      composable("dummy") {}

      // These must match your actual route patterns in NavRoutes
      composable(NavRoutes.LISTING) {}
      composable(NavRoutes.NEW_SKILL) {}
    }
  }
}
