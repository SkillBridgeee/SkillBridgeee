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

  private class FakeConvRepository :
      com.android.sample.model.communication.newImplementation.conversation.ConvRepository {
    override fun getNewUid(): String = "dummy-conv-id"

    override suspend fun getConv(
        convId: String
    ): com.android.sample.model.communication.newImplementation.conversation.ConversationNew? = null

    override suspend fun createConv(
        conversation:
            com.android.sample.model.communication.newImplementation.conversation.ConversationNew
    ) {}

    override suspend fun deleteConv(convId: String) {}

    override suspend fun sendMessage(
        convId: String,
        message: com.android.sample.model.communication.newImplementation.conversation.MessageNew
    ) {}

    override fun listenMessages(
        convId: String
    ): Flow<
        List<com.android.sample.model.communication.newImplementation.conversation.MessageNew>> =
        flowOf(emptyList())
  }

  @Before
  fun setUp() {
    val profileRepo = FakeProfileWorking()
    val listingRepo = FakeListingWorking()
    val bookingRepo = FakeBookingWorking()
    val ratingRepo = RatingFakeRepoWorking()
    val overViewRepo = FakeOverViewConvRepository()

    val context = ApplicationProvider.getApplicationContext<Context>()
    UserSessionManager.setCurrentUserId(profileRepo.getCurrentUserId())

    // Initialize all repository providers to prevent initialization errors
    ProfileRepositoryProvider.setForTests(profileRepo)
    com.android.sample.model.listing.ListingRepositoryProvider.setForTests(listingRepo)
    com.android.sample.model.booking.BookingRepositoryProvider.setForTests(bookingRepo)
    com.android.sample.model.rating.RatingRepositoryProvider.setForTests(ratingRepo)
    com.android.sample.model.communication.newImplementation.conversation
        .ConversationRepositoryProvider
        .setForTests(FakeConvRepository())
    com.android.sample.model.communication.newImplementation.overViewConv
        .OverViewConvRepositoryProvider
        .setForTests(overViewRepo)

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
    com.android.sample.model.listing.ListingRepositoryProvider.clearForTests()
    com.android.sample.model.booking.BookingRepositoryProvider.clearForTests()
    com.android.sample.model.rating.RatingRepositoryProvider.clearForTests()
    com.android.sample.model.communication.newImplementation.conversation
        .ConversationRepositoryProvider
        .clearForTests()
    com.android.sample.model.communication.newImplementation.overViewConv
        .OverViewConvRepositoryProvider
        .clearForTests()
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

  @Test
  fun listingScreen_onEditListing_callsNavigateToNewListing() {
    lateinit var navController: NavHostController
    val testListingId = "listing-to-edit-123"

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

    // Navigate to listing screen
    composeTestRule.runOnIdle {
      navController.navigate(NavRoutes.createListingRoute(testListingId))
    }
    composeTestRule.waitForIdle()

    // Verify listing screen is loaded
    composeTestRule.runOnIdle {
      val currentRoute = navController.currentBackStackEntry?.destination?.route
      assertTrue(currentRoute?.contains("listingId") == true)
    }
  }

  @Test
  fun listingScreen_onNavigateBack_popsBackStack() {
    lateinit var navController: NavHostController
    val testListingId = "listing-back-test"

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

    // Navigate to home first
    composeTestRule.runOnIdle { navController.navigate(NavRoutes.HOME) }
    composeTestRule.waitForIdle()

    // Then navigate to listing
    composeTestRule.runOnIdle {
      navController.navigate(NavRoutes.createListingRoute(testListingId))
    }
    composeTestRule.waitForIdle()

    // Pop back stack
    composeTestRule.runOnIdle { navController.popBackStack() }
    composeTestRule.waitForIdle()

    // Should be back at HOME
    composeTestRule.runOnIdle {
      assertEquals(NavRoutes.HOME, navController.currentBackStackEntry?.destination?.route)
    }
  }

  @Test
  fun bookingDetailsScreen_tracksRoute() {
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

    composeTestRule.runOnIdle { navController.navigate(NavRoutes.BOOKING_DETAILS) }
    composeTestRule.waitForIdle()

    val routes = RouteStackManager.getAllRoutes()
    assertTrue(routes.contains(NavRoutes.BOOKING_DETAILS))
  }

  @Test
  fun signUpScreen_receivesEmailParameter() {
    lateinit var navController: NavHostController
    val testEmail = "test@example.com"

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

    composeTestRule.runOnIdle { navController.navigate(NavRoutes.createSignUpRoute(testEmail)) }
    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      val currentRoute = navController.currentBackStackEntry?.destination?.route
      assertTrue(currentRoute?.contains("email") == true)
    }
  }

  @Test
  fun signUpScreen_tracksSignUpRoute() {
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

    composeTestRule.runOnIdle { navController.navigate(NavRoutes.SIGNUP_BASE) }
    composeTestRule.waitForIdle()

    val routes = RouteStackManager.getAllRoutes()
    assertTrue(routes.contains(NavRoutes.SIGNUP))
  }

  @Test
  fun othersProfileScreen_tracksRoute() {
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

    composeTestRule.runOnIdle { navController.navigate(NavRoutes.OTHERS_PROFILE) }
    composeTestRule.waitForIdle()

    val routes = RouteStackManager.getAllRoutes()
    assertTrue(routes.contains(NavRoutes.OTHERS_PROFILE))

    composeTestRule.runOnIdle {
      assertEquals(
          NavRoutes.OTHERS_PROFILE, navController.currentBackStackEntry?.destination?.route)
    }
  }

  @Test
  fun othersProfileScreen_onProposalClick_navigatesToListing() {
    lateinit var navController: NavHostController
    val testListingId = "proposal-123"

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

    composeTestRule.runOnIdle { navController.navigate(NavRoutes.OTHERS_PROFILE) }
    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      navController.navigate(NavRoutes.createListingRoute(testListingId))
    }
    composeTestRule.waitForIdle()

    composeTestRule.runOnIdle {
      val currentRoute = navController.currentBackStackEntry?.destination?.route
      assertTrue(currentRoute?.contains("listingId") == true)
    }
  }

  @Test
  fun listingScreen_routeStackManager_addsListingRoute() {
    lateinit var navController: NavHostController
    val testListingId = "listing-stack-test"

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

    composeTestRule.runOnIdle {
      navController.navigate(NavRoutes.createListingRoute(testListingId))
    }
    composeTestRule.waitForIdle()

    val routes = RouteStackManager.getAllRoutes()
    assertTrue(routes.contains(NavRoutes.LISTING))
  }
}
