package com.android.sample.navigation

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
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
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.newImplementation.overViewConv.OverViewConversation
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppNavGraphSmokeTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var authViewModel: AuthenticationViewModel
    private lateinit var bookingsViewModel: MyBookingsViewModel
    private lateinit var profileViewModel: MyProfileViewModel
    private lateinit var mainPageViewModel: MainPageViewModel
    private lateinit var newListingViewModel: NewListingViewModel
    private lateinit var bookingDetailsViewModel: BookingDetailsViewModel
    private lateinit var discussionViewModel: DiscussionViewModel

    private class FakeOverViewConvRepository : OverViewConvRepository {
        override fun getNewUid(): String = "dummy"

        override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> =
            emptyList()

        override suspend fun addOverViewConvUser(overView: OverViewConversation) {}

        override suspend fun deleteOverViewConvUser(convId: String) {}

        override fun listenOverView(userId: String): Flow<List<OverViewConversation>> =
            flowOf(
                listOf(
                    OverViewConversation(
                        convName = "Chat with Alice",
                        linkedConvId = "conv1",
                        lastMsg = null,
                        nonReadMsgNumber = 0,
                    )))
    }

    @Before
    fun setUp() {
        val profileRepo = FakeProfileWorking()
        val listingRepo = FakeListingWorking()
        val bookingRepo = FakeBookingWorking()
        val ratingRepo = RatingFakeRepoWorking()

        val context = ApplicationProvider.getApplicationContext<Context>()
        UserSessionManager.setCurrentUserId(profileRepo.getCurrentUserId())

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

    @Test
    fun navigateToDiscussion_doesNotCrash() {
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

        composeTestRule.runOnIdle { navController.navigate(NavRoutes.DISCUSSION) }

        composeTestRule.runOnIdle {
            assertEquals(NavRoutes.DISCUSSION, navController.currentBackStackEntry?.destination?.route)
        }
    }

    @Test
    fun navigateToToS_doesNotCrash() {
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

        composeTestRule.runOnIdle { navController.navigate(NavRoutes.TOS) }

        composeTestRule.runOnIdle {
            assertEquals(NavRoutes.TOS, navController.currentBackStackEntry?.destination?.route)
        }
    }
}
