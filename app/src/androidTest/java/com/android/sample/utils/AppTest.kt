package com.android.sample.utils

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.utils.InMemoryBootcampTest.ProfileFake
import com.android.sample.utils.fakeRepo.BookingFake
import com.android.sample.utils.fakeRepo.ListingFake
import com.android.sample.utils.fakeRepo.RatingFake
import kotlin.collections.contains
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before

abstract class AppTest() {

  abstract fun createInitializedProfileRepo(): ProfileRepository

  open fun initializeHTTPClient(): OkHttpClient = FakeHttpClient.getClient()

  val profileRepository: ProfileRepository
    get() = ProfileRepositoryProvider.repository

  lateinit var authViewModel: AuthenticationViewModel
  lateinit var bookingsViewModel: MyBookingsViewModel
  lateinit var profileViewModel: MyProfileViewModel
  lateinit var mainPageViewModel: MainPageViewModel

  private lateinit var bookingRepo: BookingRepository
  private lateinit var listingRepo: ListingRepository
  private lateinit var profileRepo: ProfileRepository
  private lateinit var ratingRepo: RatingRepository

  @Before
  open fun setUp() {
    //    ProfileRepositoryProvider.setForTests(createInitializedProfileRepo())
    //    HttpClientProvider.client = initializeHTTPClient()

    bookingRepo = BookingFake()
    listingRepo = ListingFake()
    profileRepo = ProfileFake()
    ratingRepo = RatingFake()

    val context = ApplicationProvider.getApplicationContext<Context>()
    authViewModel = AuthenticationViewModel(context = context, profileRepository = profileRepo)

    // ✅ Initialiser les autres ViewModels (fakes ou defaults)
    bookingsViewModel =
        MyBookingsViewModel(
            bookingRepo = bookingRepo, listingRepo = listingRepo, profileRepo = profileRepo)
    profileViewModel =
        MyProfileViewModel(
            profileRepository = profileRepo,
            listingRepository = listingRepo,
            ratingsRepository = ratingRepo)
    mainPageViewModel =
        MainPageViewModel(profileRepository = profileRepo, listingRepository = listingRepo)
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
