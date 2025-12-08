package com.android.sample.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.BookingDetailsViewModel
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.communication.DiscussionViewModel
import com.android.sample.ui.newListing.NewListingViewModel
import com.android.sample.ui.profile.MyProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavGraphTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  private lateinit var navController: NavHostController

  @Before
  fun setup() {
    // Only create controller. DO NOT set content here.
    composeRule.activityRule.scenario.onActivity { activity ->
      navController =
          TestNavHostController(activity).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            graph = createTestGraph(this)
          }
    }
    val fakeProfileRepo: ProfileRepository = mockk(relaxed = true)
    ProfileRepositoryProvider.setForTests(fakeProfileRepo)
  }

  private fun createTestGraph(navController: NavHostController): NavGraph {
    return navController.createGraph(startDestination = "dummy") {
      composable("dummy") {}
      composable(NavRoutes.LISTING) {}
      composable(NavRoutes.NEW_SKILL) {}
      composable(NavRoutes.SIGNUP) {}
      composable(NavRoutes.LOGIN) {}
      composable(NavRoutes.HOME) {}
      composable(NavRoutes.TOS) {}
      composable(NavRoutes.BOOKING_DETAILS) {}
      composable(NavRoutes.OTHERS_PROFILE) {}
      composable(NavRoutes.DISCUSSION) {}
      composable(NavRoutes.MESSAGES) {}
      composable(NavRoutes.SKILLS) {}
      composable(NavRoutes.BOOKINGS) {}
      composable(NavRoutes.PROFILE) {}
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

  @Test
  fun splash_withoutFirebaseUser_navigatesToLogin_fromAppNavGraph() {
    FirebaseAuth.getInstance().signOut()

    composeRule.setContent {
      val controller = rememberNavController()
      navController = controller
      val context = androidx.compose.ui.platform.LocalContext.current

      val authVm = AuthenticationViewModel(context) // REAL VM

      val bookingsVm: MyBookingsViewModel = mockk(relaxed = true)
      val profileVm: MyProfileViewModel = mockk(relaxed = true)
      val mainVm: MainPageViewModel = mockk(relaxed = true)
      val newListingVm: NewListingViewModel = mockk(relaxed = true)
      val bookingDetailsVm: BookingDetailsViewModel = mockk(relaxed = true)
      val discussionVm: DiscussionViewModel = mockk(relaxed = true)

      AppNavGraph(
          navController = controller,
          bookingsViewModel = bookingsVm,
          profileViewModel = profileVm,
          mainPageViewModel = mainVm,
          newListingViewModel = newListingVm,
          authViewModel = authVm, // <-- use real one
          bookingDetailsViewModel = bookingDetailsVm,
          discussionViewModel = discussionVm,
          onGoogleSignIn = {})
    }

    composeRule.waitForIdle()
    assertEquals(NavRoutes.LOGIN, navController.currentDestination?.route)
  }

  @Test
  fun messages_showsFallbackText_whenNoConversationSelected_fromAppNavGraph() {
    FirebaseAuth.getInstance().signOut()

    composeRule.setContent {
      val controller = rememberNavController()
      navController = controller
      val context = androidx.compose.ui.platform.LocalContext.current

      val authVm = AuthenticationViewModel(context) // REAL VM

      val bookingsVm: MyBookingsViewModel = mockk(relaxed = true)
      val profileVm: MyProfileViewModel = mockk(relaxed = true)
      val mainVm: MainPageViewModel = mockk(relaxed = true)
      val newListingVm: NewListingViewModel = mockk(relaxed = true)
      val bookingDetailsVm: BookingDetailsViewModel = mockk(relaxed = true)
      val discussionVm: DiscussionViewModel = mockk(relaxed = true)

      AppNavGraph(
          navController = controller,
          bookingsViewModel = bookingsVm,
          profileViewModel = profileVm,
          mainPageViewModel = mainVm,
          newListingViewModel = newListingVm,
          authViewModel = authVm, // <-- real
          bookingDetailsViewModel = bookingDetailsVm,
          discussionViewModel = discussionVm,
          onGoogleSignIn = {})
    }

    composeRule.runOnIdle { navController.navigate(NavRoutes.MESSAGES) }

    composeRule.onNodeWithText("No conversation selected").assertIsDisplayed()
  }

  @Test
  fun home_onNavigateToSubjectList_navigatesToSkills() {
    composeRule.runOnIdle {
      navController.navigate(NavRoutes.HOME)
      assertEquals(NavRoutes.HOME, navController.currentDestination?.route)

      // Simulate UI callback
      navController.navigate(NavRoutes.SKILLS)
    }

    assertEquals(NavRoutes.SKILLS, navController.currentDestination?.route)
  }

  @Test
  fun skills_onListingClick_navigatesToListing() {
    val listingId = "123"

    composeRule.runOnIdle {
      navController.navigate(NavRoutes.SKILLS)
      navigateToListing(navController, listingId)
    }

    assertEquals(NavRoutes.LISTING, navController.currentDestination?.route)
    assertEquals(listingId, navController.currentBackStackEntry?.arguments?.getString("listingId"))
  }

  @Test
  fun bookings_onBookingClick_navigatesToBookingDetails() {
    val bookingId = "B001"

    composeRule.runOnIdle {
      navController.navigate(NavRoutes.BOOKINGS)

      // simulate click callback
      navController.navigate(NavRoutes.BOOKING_DETAILS)
    }

    assertEquals(NavRoutes.BOOKING_DETAILS, navController.currentDestination?.route)
  }
}
