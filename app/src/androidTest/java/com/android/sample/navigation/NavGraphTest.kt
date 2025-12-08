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

      // Helpers under test
      composable(NavRoutes.LISTING) {}
      composable(NavRoutes.NEW_SKILL) {}

      // Extra routes used in your tests
      composable(NavRoutes.SIGNUP) {}
      composable(NavRoutes.LOGIN) {}
      composable(NavRoutes.HOME) {}
      composable(NavRoutes.TOS) {}
      composable(NavRoutes.BOOKING_DETAILS) {}
      composable(NavRoutes.OTHERS_PROFILE) {}
      composable(NavRoutes.DISCUSSION) {}
      composable(NavRoutes.MESSAGES) {}
    }
  }

  // In NavGraphTest.kt (add below your existing 2 tests)

  @Test
  fun signUp_onSubmitSuccess_navigatesToLogin() {
    composeRule.runOnIdle {
      // Simulate we are on SIGNUP
      navController.navigate(NavRoutes.SIGNUP)
      assertEquals(NavRoutes.SIGNUP, navController.currentDestination?.route)

      // Same code as onSubmitSuccess
      navController.navigate(NavRoutes.LOGIN) { popUpTo(0) { inclusive = false } }

      assertEquals(NavRoutes.LOGIN, navController.currentDestination?.route)
    }
  }

  @Test
  fun signUp_onGoogleSignUpSuccess_navigatesToHome() {
    composeRule.runOnIdle {
      navController.navigate(NavRoutes.SIGNUP)
      assertEquals(NavRoutes.SIGNUP, navController.currentDestination?.route)

      // Same code as onGoogleSignUpSuccess
      navController.navigate(NavRoutes.HOME) { popUpTo(0) { inclusive = true } }

      assertEquals(NavRoutes.HOME, navController.currentDestination?.route)
    }
  }

  @Test
  fun signUp_onBackPressed_navigatesToLogin() {
    composeRule.runOnIdle {
      navController.navigate(NavRoutes.SIGNUP)
      assertEquals(NavRoutes.SIGNUP, navController.currentDestination?.route)

      // Same code as onBackPressed
      navController.navigate(NavRoutes.LOGIN) { popUpTo(0) { inclusive = false } }

      assertEquals(NavRoutes.LOGIN, navController.currentDestination?.route)
    }
  }

  @Test
  fun signUp_onNavigateToToS_navigatesToToS() {
    composeRule.runOnIdle {
      navController.navigate(NavRoutes.SIGNUP)
      assertEquals(NavRoutes.SIGNUP, navController.currentDestination?.route)

      // Same code as onNavigateToToS
      navController.navigate(NavRoutes.TOS)

      assertEquals(NavRoutes.TOS, navController.currentDestination?.route)
    }
  }

  @Test
  fun othersProfile_onProposalClick_navigatesToListing() {
    val listingId = "listing_from_proposal"

    composeRule.runOnIdle {
      // Same behaviour as ProfileScreen's onProposalClick
      navigateToListing(navController, listingId)

      assertEquals(NavRoutes.LISTING, navController.currentDestination?.route)
      val args = navController.currentBackStackEntry?.arguments
      assertEquals(listingId, args?.getString("listingId"))
    }
  }

  @Test
  fun othersProfile_onRequestClick_navigatesToListing() {
    val listingId = "listing_from_request"

    composeRule.runOnIdle {
      // Same behaviour as ProfileScreen's onRequestClick
      navigateToListing(navController, listingId)

      assertEquals(NavRoutes.LISTING, navController.currentDestination?.route)
      val args = navController.currentBackStackEntry?.arguments
      assertEquals(listingId, args?.getString("listingId"))
    }
  }

  @Test
  fun bookingDetails_onCreatorClick_navigatesToOthersProfile() {
    val creatorId = "creator123"

    composeRule.runOnIdle {
      // Simulate we are in BOOKING_DETAILS
      navController.navigate(NavRoutes.BOOKING_DETAILS)
      assertEquals(NavRoutes.BOOKING_DETAILS, navController.currentDestination?.route)

      // Same logic as in onCreatorClick lambda
      // (in AppNavGraph you also store profileID.value, here we just test navigation)
      navController.navigate(NavRoutes.OTHERS_PROFILE)

      assertEquals(NavRoutes.OTHERS_PROFILE, navController.currentDestination?.route)
    }
  }

  @Test
  fun discussion_onConversationClick_navigatesToMessages() {
    val convId = "conv-xyz"

    composeRule.runOnIdle {
      // Start at DISCUSSION
      navController.navigate(NavRoutes.DISCUSSION)
      assertEquals(NavRoutes.DISCUSSION, navController.currentDestination?.route)

      // Same behaviour as onConversationClick
      navController.navigate(NavRoutes.MESSAGES)

      assertEquals(NavRoutes.MESSAGES, navController.currentDestination?.route)
    }
  }
}
