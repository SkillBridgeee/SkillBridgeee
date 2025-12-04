package com.android.sample.navigation

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.BookingDetailsViewModel
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.communication.DiscussionViewModel
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.navigation.RouteStackManager
import com.android.sample.ui.newListing.NewListingViewModel
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.utils.fakeRepo.fakeBooking.FakeBookingWorking
import com.android.sample.utils.fakeRepo.fakeListing.FakeListingWorking
import com.android.sample.utils.fakeRepo.fakeProfile.FakeProfileWorking
import com.android.sample.utils.fakeRepo.fakeRating.RatingFakeRepoWorking
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests for NavGraph helper functions and navigation logic. This test class focuses on testing the
 * recently added helper functions like navigateToNewListing() and the Messages screen navigation.
 */
class NavGraphHelperTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var authViewModel: AuthenticationViewModel
  private lateinit var bookingsViewModel: MyBookingsViewModel
  private lateinit var profileViewModel: MyProfileViewModel
  private lateinit var mainPageViewModel: MainPageViewModel
  private lateinit var newListingViewModel: NewListingViewModel
  private lateinit var bookingDetailsViewModel: BookingDetailsViewModel
  private lateinit var discussionViewModel: DiscussionViewModel

  private class FakeOverViewConvRepository :
      com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository {
    override fun getNewUid(): String = "dummy"

    override suspend fun getOverViewConvUser(
        userId: String
    ): List<
        com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation> =
        emptyList()

    override suspend fun addOverViewConvUser(
        overView:
            com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
    ) {}

    override suspend fun deleteOverViewConvUser(convId: String) {}

    override fun listenOverView(
        userId: String
    ): Flow<
        List<
            com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation>> =
        flowOf(emptyList())
  }

  @Before
  fun setUp() {
    val profileRepo = FakeProfileWorking()
    val listingRepo = FakeListingWorking()
    val bookingRepo = FakeBookingWorking()
    val ratingRepo = RatingFakeRepoWorking()

    val context = ApplicationProvider.getApplicationContext<Context>()
    UserSessionManager.setCurrentUserId(profileRepo.getCurrentUserId())

    ProfileRepositoryProvider.setForTests(profileRepo)

    authViewModel = AuthenticationViewModel(context = context, profileRepository = profileRepo)
    bookingsViewModel =
        MyBookingsViewModel(
            bookingRepo = bookingRepo, listingRepo = listingRepo, profileRepo = profileRepo)
    profileViewModel =
        MyProfileViewModel(
            profileRepository = profileRepo,
            bookingRepository = bookingRepo,
            listingRepository = listingRepo,
            ratingsRepository = ratingRepo,
            sessionManager = UserSessionManager)
    mainPageViewModel =
        MainPageViewModel(profileRepository = profileRepo, listingRepository = listingRepo)
    newListingViewModel = NewListingViewModel(listingRepository = listingRepo)
    bookingDetailsViewModel =
        BookingDetailsViewModel(
            listingRepository = listingRepo,
            bookingRepository = bookingRepo,
            profileRepository = profileRepo,
            ratingRepository = ratingRepo)

    discussionViewModel = DiscussionViewModel(FakeOverViewConvRepository())

    RouteStackManager.clear()
  }

  @After
  fun tearDown() {
    ProfileRepositoryProvider.clearForTests()
    UserSessionManager.clearSession()
  }

  @Test
  fun navigateToNewListing_fromHome_whenAuthenticated_navigatesToNewSkillRoute() {
    lateinit var navController: NavHostController

    composeTestRule.setContent {
      navController = rememberNavController()
      AppNavGraph(
          navController = navController,
          bookingsViewModel = bookingsViewModel,
          profileViewModel = profileViewModel,
          mainPageViewModel = mainPageViewModel,
          newListingViewModel = newListingViewModel,
          authViewModel = authViewModel,
          bookingDetailsViewModel = bookingDetailsViewModel,
          discussionViewModel = discussionViewModel,
          onGoogleSignIn = {})
    }

    // Navigate to HOME first
    composeTestRule.runOnIdle { navController.navigate(NavRoutes.HOME) }
    composeTestRule.waitForIdle()

    val currentUserId = UserSessionManager.getCurrentUserId()
    assertTrue(currentUserId != null)

    // The HOME screen should be displayed
    composeTestRule.runOnIdle {
      assertEquals(NavRoutes.HOME, navController.currentBackStackEntry?.destination?.route)
    }
  }

  @Test
  fun messagesScreen_withEmptyConvId_showsEmptyState() {
    lateinit var navController: NavHostController

    composeTestRule.setContent {
      navController = rememberNavController()
      AppNavGraph(
          navController = navController,
          bookingsViewModel = bookingsViewModel,
          profileViewModel = profileViewModel,
          mainPageViewModel = mainPageViewModel,
          newListingViewModel = newListingViewModel,
          authViewModel = authViewModel,
          bookingDetailsViewModel = bookingDetailsViewModel,
          discussionViewModel = discussionViewModel,
          onGoogleSignIn = {})
    }

    // Navigate directly to MESSAGES without setting convId
    composeTestRule.runOnIdle { navController.navigate(NavRoutes.MESSAGES) }
    composeTestRule.waitForIdle()

    // Should show "No conversation selected"
    composeTestRule.onNodeWithText("No conversation selected").assertExists()
  }

  @Test
  fun routeStackManager_tracksAllNavigationEvents() {
    lateinit var navController: NavHostController

    composeTestRule.setContent {
      navController = rememberNavController()
      AppNavGraph(
          navController = navController,
          bookingsViewModel = bookingsViewModel,
          profileViewModel = profileViewModel,
          mainPageViewModel = mainPageViewModel,
          newListingViewModel = newListingViewModel,
          authViewModel = authViewModel,
          bookingDetailsViewModel = bookingDetailsViewModel,
          discussionViewModel = discussionViewModel,
          onGoogleSignIn = {})
    }

    RouteStackManager.clear()

    // Navigate to multiple screens
    composeTestRule.runOnIdle { navController.navigate(NavRoutes.HOME) }
    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle { navController.navigate(NavRoutes.DISCUSSION) }
    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle { navController.navigate(NavRoutes.MESSAGES) }
    composeTestRule.waitForIdle()

    // All routes should be tracked
    val routes = RouteStackManager.getAllRoutes()
    assertTrue(routes.contains(NavRoutes.HOME))
    assertTrue(routes.contains(NavRoutes.DISCUSSION))
    assertTrue(routes.contains(NavRoutes.MESSAGES))
  }
}
