package com.android.sample.utils

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.rating.RatingRepository
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.utils.fakeRepo.fakeProfile.FakeProfileRepo
import kotlin.collections.contains
import org.junit.After
import org.junit.Before

abstract class AppTest() {

  abstract fun createInitializedProfileRepo(): FakeProfileRepo

  abstract fun createInitializedListingRepo(): ListingRepository

  abstract fun createInitializedBookingRepo(): BookingRepository

  abstract fun createInitializedRatingRepo(): RatingRepository

  val profileRepository: FakeProfileRepo
    get() = createInitializedProfileRepo()

  val listingRepository: ListingRepository
    get() = createInitializedListingRepo()

  val bookingRepository: BookingRepository
    get() = createInitializedBookingRepo()

  val ratingRepository: RatingRepository
    get() = createInitializedRatingRepo()

  lateinit var authViewModel: AuthenticationViewModel
  lateinit var bookingsViewModel: MyBookingsViewModel
  lateinit var profileViewModel: MyProfileViewModel
  lateinit var mainPageViewModel: MainPageViewModel

  @Before
  open fun setUp() {
    //    ProfileRepositoryProvider.setForTests(createInitializedProfileRepo())
    //    HttpClientProvider.client = initializeHTTPClient()

    val context = ApplicationProvider.getApplicationContext<Context>()
    authViewModel =
        AuthenticationViewModel(context = context, profileRepository = profileRepository)
    bookingsViewModel =
        MyBookingsViewModel(
            bookingRepo = bookingRepository,
            listingRepo = listingRepository,
            profileRepo = profileRepository)
    profileViewModel =
        MyProfileViewModel(
            profileRepository = profileRepository,
            listingRepository = listingRepository,
            ratingsRepository = ratingRepository)
    mainPageViewModel =
        MainPageViewModel(
            profileRepository = profileRepository, listingRepository = listingRepository)

    val currentUserId = profileRepository.getCurrentUserId()
    UserSessionManager.setCurrentUserId(currentUserId)
  }

  @Composable
  fun CreateEveryThing() {
    val navController = rememberNavController()

    val mainScreenRoutes =
        listOf(NavRoutes.HOME, NavRoutes.BOOKINGS, NavRoutes.PROFILE, NavRoutes.MAP)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = mainScreenRoutes.contains(currentRoute)

    Scaffold(
        topBar = { TopAppBar(navController) },
        bottomBar = {
          if (showBottomNav) {
            BottomNavBar(navController)
          }
        }) { paddingValues ->
          Box(modifier = Modifier.padding(paddingValues)) {
            AppNavGraph(
                navController = navController,
                bookingsViewModel = bookingsViewModel,
                profileViewModel = profileViewModel,
                mainPageViewModel = mainPageViewModel,
                authViewModel = authViewModel,
                onGoogleSignIn = {})
          }
          LaunchedEffect(Unit) {
            navController.navigate(NavRoutes.HOME) { popUpTo(0) { inclusive = true } }
          }
        }
  }

  @After open fun tearDown() {}

  fun ComposeTestRule.enterText(testTag: String, text: String) {
    onNodeWithTag(testTag).performTextClearance()
    onNodeWithTag(testTag).performTextInput(text)
  }

  //////// HelperFunction to navigate from Home Screen

  fun ComposeTestRule.navigateToNewListing() {
    onNodeWithTag(HomeScreenTestTags.FAB_ADD).performClick()
  }

  fun ComposeTestRule.navigateToMyProfile() {
    onNodeWithTag(BottomBarTestTag.NAV_PROFILE).performClick()
  }

  ///////

}
