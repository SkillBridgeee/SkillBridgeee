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
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.authentication.AuthenticationViewModel
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.listing.Listing
import com.android.sample.ui.HomePage.HomeScreenTestTags
import com.android.sample.ui.HomePage.MainPageViewModel
import com.android.sample.ui.bookings.BookingDetailsViewModel
import com.android.sample.ui.bookings.MyBookingsViewModel
import com.android.sample.ui.components.BookingCardTestTag
import com.android.sample.ui.components.BottomBarTestTag
import com.android.sample.ui.components.BottomNavBar
import com.android.sample.ui.components.LocationInputFieldTestTags
import com.android.sample.ui.components.TopAppBar
import com.android.sample.ui.components.TopAppBarTestTags
import com.android.sample.ui.login.SignInScreenTestTags
import com.android.sample.ui.navigation.AppNavGraph
import com.android.sample.ui.navigation.NavRoutes
import com.android.sample.ui.newListing.NewListingScreenTestTag
import com.android.sample.ui.newListing.NewListingViewModel
import com.android.sample.ui.profile.MyProfileViewModel
import com.android.sample.ui.signup.SignUpScreenTestTags
import com.android.sample.utils.fakeRepo.fakeBooking.FakeBookingRepo
import com.android.sample.utils.fakeRepo.fakeBooking.FakeBookingWorking
import com.android.sample.utils.fakeRepo.fakeListing.FakeListingRepo
import com.android.sample.utils.fakeRepo.fakeListing.FakeListingWorking
import com.android.sample.utils.fakeRepo.fakeProfile.FakeProfileRepo
import com.android.sample.utils.fakeRepo.fakeProfile.FakeProfileWorking
import com.android.sample.utils.fakeRepo.fakeRating.FakeRatingRepo
import com.android.sample.utils.fakeRepo.fakeRating.RatingFakeRepoWorking
import kotlin.collections.contains
import org.junit.After
import org.junit.Before

abstract class AppTest() {

  // These factory methods allow swapping between different Fake repos
  // (e.g., working repos vs. error repos) depending on the test scenario.
  open fun createInitializedProfileRepo(): FakeProfileRepo = FakeProfileWorking()

  open fun createInitializedListingRepo(): FakeListingRepo = FakeListingWorking()

  open fun createInitializedBookingRepo(): FakeBookingRepo = FakeBookingWorking()

  open fun createInitializedRatingRepo(): FakeRatingRepo = RatingFakeRepoWorking()

  lateinit var listingRepository: FakeListingRepo
  lateinit var profileRepository: FakeProfileRepo
  lateinit var bookingRepository: FakeBookingRepo
  lateinit var ratingRepository: FakeRatingRepo

  lateinit var authViewModel: AuthenticationViewModel
  lateinit var bookingsViewModel: MyBookingsViewModel
  lateinit var profileViewModel: MyProfileViewModel
  lateinit var mainPageViewModel: MainPageViewModel
  lateinit var newListingViewModel: NewListingViewModel
  lateinit var bookingDetailsViewModel: BookingDetailsViewModel

  @Before
  open fun setUp() {

    profileRepository = createInitializedProfileRepo()
    listingRepository = createInitializedListingRepo()
    bookingRepository = createInitializedBookingRepo()
    ratingRepository = createInitializedRatingRepo()

    val currentUserId = profileRepository.getCurrentUserId()
    UserSessionManager.setCurrentUserId(currentUserId)

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
            bookingRepository = bookingRepository,
            listingRepository = listingRepository,
            ratingsRepository = ratingRepository,
            sessionManager = UserSessionManager)
    mainPageViewModel =
        MainPageViewModel(
            profileRepository = profileRepository, listingRepository = listingRepository)

    newListingViewModel = NewListingViewModel(listingRepository = listingRepository)

    bookingDetailsViewModel =
        BookingDetailsViewModel(
            listingRepository = listingRepository,
            bookingRepository = bookingRepository,
            profileRepository = profileRepository,
            ratingRepository = ratingRepository)
  }

  /**
   * Composable function that sets up the main UI structure used during tests.
   *
   * This function creates a NavController and configures the app's navigation graph, top bar, and
   * bottom navigation bar. It also initializes the start destination in the Home Page
   *
   * This function is typically used in UI tests to render the full app structure with fake
   * repositories and pre-initialized ViewModels.
   */
  @Composable
  fun CreateAppContent() {
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
                newListingViewModel = newListingViewModel,
                authViewModel = authViewModel,
                onGoogleSignIn = {},
                bookingDetailsViewModel = bookingDetailsViewModel)
          }
          LaunchedEffect(Unit) {
            navController.navigate(NavRoutes.HOME) { popUpTo(0) { inclusive = true } }
          }
        }
  }

  @After open fun tearDown() {}

  //////// HelperFunction to navigate from Home Screen

  fun ComposeTestRule.navigateToNewListing() {
    onNodeWithTag(HomeScreenTestTags.FAB_ADD).performClick()
  }

  fun ComposeTestRule.navigateToMyProfile() {
    onNodeWithTag(BottomBarTestTag.NAV_PROFILE).performClick()
  }

  fun ComposeTestRule.navigateToHome() {
    onNodeWithTag(BottomBarTestTag.NAV_HOME).performClick()
  }

  fun ComposeTestRule.navigateToMyBookings() {
    onNodeWithTag(BottomBarTestTag.NAV_BOOKINGS).performClick()
  }

  fun ComposeTestRule.navigateToMap() {
    onNodeWithTag(BottomBarTestTag.NAV_MAP).performClick()
  }

  fun ComposeTestRule.clickTopAppBarBack() {
    onNodeWithTag(TopAppBarTestTags.BACK_BUTTON).performClick()
  }

  fun ComposeTestRule.navigateToBookingDetails() {
    navigateToMyBookings()
    onNodeWithTag(BookingCardTestTag.CARD).assertExists().performClick()
  }

  /////// Helper Method to test components

  fun ComposeTestRule.enterText(testTag: String, text: String) {
    onNodeWithTag(testTag).performTextClearance()
    onNodeWithTag(testTag).performTextInput(text)
  }

  fun ComposeTestRule.clickOn(testTag: String) {
    onNodeWithTag(testTag = testTag).performClick()
  }

  fun ComposeTestRule.multipleChooseExposeMenu(
      multipleTestTag: String,
      differentChoiceTestTag: String
  ) {
    onNodeWithTag(multipleTestTag).performClick()
    waitUntil(timeoutMillis = 10_000) {
      onAllNodesWithTag(differentChoiceTestTag, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    onNodeWithTag(differentChoiceTestTag).performClick()
  }

  fun ComposeTestRule.enterAndChooseLocation(
      enterText: String,
      selectText: String,
      inputLocationTestTag: String
  ) {

    onNodeWithTag(inputLocationTestTag, useUnmergedTree = true).performTextInput(enterText)

    waitUntil(timeoutMillis = 20_000) {
      onAllNodesWithText(selectText).fetchSemanticsNodes().isNotEmpty()
    }
    onAllNodesWithText(selectText)[0].performClick()
  }

  // HelperMethode for Testing NewListing
  fun ComposeTestRule.fillNewListing(newListing: Listing) {

    // Enter Title
    enterText(NewListingScreenTestTag.INPUT_COURSE_TITLE, newListing.title)
    // Enter Desc
    enterText(NewListingScreenTestTag.INPUT_DESCRIPTION, newListing.description)
    // Enter Price
    enterText(NewListingScreenTestTag.INPUT_PRICE, newListing.hourlyRate.toString())

    // Choose ListingType
    multipleChooseExposeMenu(
        NewListingScreenTestTag.LISTING_TYPE_FIELD,
        "${NewListingScreenTestTag.LISTING_TYPE_DROPDOWN_ITEM_PREFIX}_${newListing.type.ordinal}")

    // Choose Main subject
    multipleChooseExposeMenu(
        NewListingScreenTestTag.SUBJECT_FIELD,
        "${NewListingScreenTestTag.SUBJECT_DROPDOWN_ITEM_PREFIX}_${newListing.skill.mainSubject.ordinal}")

    // Choose sub skill // todo hardcoded value for subskill (idk possible to do it other good way)
    multipleChooseExposeMenu(
        NewListingScreenTestTag.SUB_SKILL_FIELD,
        "${NewListingScreenTestTag.SUB_SKILL_DROPDOWN_ITEM_PREFIX}_0")

    enterAndChooseLocation(
        enterText = newListing.location.name.dropLast(1),
        selectText = newListing.location.name,
        inputLocationTestTag = LocationInputFieldTestTags.INPUT_LOCATION)
  }

  /**
   * Helper function to sign up a new user via the UI.
   * Navigates to signup screen, fills the form, and submits.
   * Returns to login screen after successful signup.
   * Automatically scrolls to ensure fields are visible on smaller screens.
   */
  fun ComposeTestRule.signUpNewUser(
    name: String,
    surname: String,
    address: String,
    levelOfEducation: String,
    description: String,
    email: String,
    password: String
  ) {
    // Navigate to signup screen from login
    onNodeWithTag(SignInScreenTestTags.SIGNUP_LINK).performClick()
    waitForIdle()

    // Fill signup form with scrolling
    onNodeWithTag(SignUpScreenTestTags.NAME).performScrollTo()
    enterText(SignUpScreenTestTags.NAME, name)

    onNodeWithTag(SignUpScreenTestTags.SURNAME).performScrollTo()
    enterText(SignUpScreenTestTags.SURNAME, surname)

    onNodeWithTag(SignUpScreenTestTags.ADDRESS).performScrollTo()
    enterAndChooseLocation(
      enterText = address,
      selectText = address,
      inputLocationTestTag = SignUpScreenTestTags.ADDRESS)

    onNodeWithTag(SignUpScreenTestTags.LEVEL_OF_EDUCATION).performScrollTo()
    enterText(SignUpScreenTestTags.LEVEL_OF_EDUCATION, levelOfEducation)

    onNodeWithTag(SignUpScreenTestTags.DESCRIPTION).performScrollTo()
    enterText(SignUpScreenTestTags.DESCRIPTION, description)

    // Email is pre-filled if coming from Google Sign-In, otherwise fill it
    if (email.isNotEmpty()) {
      onNodeWithTag(SignUpScreenTestTags.EMAIL).performScrollTo()
      enterText(SignUpScreenTestTags.EMAIL, email)
    }

    // Password (only if not Google sign-up)
    if (password.isNotEmpty()) {
      onNodeWithTag(SignUpScreenTestTags.PASSWORD).performScrollTo()
      enterText(SignUpScreenTestTags.PASSWORD, password)
    }

    // Scroll to submit button and click
    onNodeWithTag(SignUpScreenTestTags.SIGN_UP).performScrollTo()
    clickOn(SignUpScreenTestTags.SIGN_UP)
    waitForIdle()
  }

  /**
   * Helper function to login a user via the UI.
   * Fills email and password fields and clicks sign in button.
   * Includes scrolling for smaller screens.
   */
  fun ComposeTestRule.loginUser(email: String, password: String) {
    // Make sure we're on login screen
    onNodeWithTag(SignInScreenTestTags.TITLE).assertExists()

    // Fill login form with scrolling
    onNodeWithTag(SignInScreenTestTags.EMAIL_INPUT).performScrollTo()
    enterText(SignInScreenTestTags.EMAIL_INPUT, email)

    onNodeWithTag(SignInScreenTestTags.PASSWORD_INPUT).performScrollTo()
    enterText(SignInScreenTestTags.PASSWORD_INPUT, password)

    // Scroll to sign in button and click
    onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).performScrollTo()
    clickOn(SignInScreenTestTags.SIGN_IN_BUTTON)
    waitForIdle()
  }

  /**
   * Helper function for complete signup and login flow.
   * Signs up a new user, waits for return to login, then logs in.
   * Handles scrolling automatically for CI compatibility.
   */
  fun ComposeTestRule.signUpAndLogin(
    name: String,
    surname: String,
    address: String,
    levelOfEducation: String,
    description: String,
    email: String,
    password: String
  ) {
    signUpNewUser(name, surname, address, levelOfEducation, description, email, password)

    // After signup, should be back on login screen
    waitForIdle()
    onNodeWithTag(SignInScreenTestTags.TITLE).assertExists()

    // Now login with the same credentials
    loginUser(email, password)
  }
}
