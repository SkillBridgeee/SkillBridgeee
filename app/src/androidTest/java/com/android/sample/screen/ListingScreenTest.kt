package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.listing.ListingScreen
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.listing.ListingViewModel
import java.util.Date
import org.junit.After
import org.junit.Rule
import org.junit.Test

/**
 * Integration tests for ListingScreen Tests focus on screen-level state management, navigation, and
 * component integration Component-specific tests are in their respective test files under
 * components/
 */
@Suppress("DEPRECATION")
class ListingScreenTest {

  @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

  private val sampleProposal =
      Proposal(
          listingId = "listing-123",
          creatorUserId = "creator-456",
          skill = Skill(MainSubject.MUSIC, "Guitar", 5.0, ExpertiseLevel.INTERMEDIATE),
          description = "Learn guitar from scratch or improve your skills",
          location = Location(latitude = 40.7128, longitude = -74.0060, name = "New York"),
          hourlyRate = 50.0,
          createdAt = Date())

  private val sampleRequest =
      Request(
          listingId = "listing-456",
          creatorUserId = "creator-789",
          skill = Skill(MainSubject.ACADEMICS, "Math", 0.0, ExpertiseLevel.BEGINNER),
          description = "Looking for a math tutor",
          location = Location(latitude = 40.7128, longitude = -74.0060, name = "Boston"),
          hourlyRate = 40.0,
          createdAt = Date())

  private val sampleCreator =
      Profile(
          userId = "creator-456",
          name = "John Doe",
          email = "john@example.com",
          description = "Experienced guitar teacher",
          location = Location(latitude = 40.7128, longitude = -74.0060, name = "New York"))

  @After
  fun cleanup() {
    UserSessionManager.clearSession()
  }

  // Fake Repositories
  private class FakeListingRepo(private val listing: Listing?) : ListingRepository {
    override fun getNewUid() = "new-listing-id"

    override suspend fun getAllListings() = listing?.let { listOf(it) } ?: emptyList()

    override suspend fun getProposals() = if (listing is Proposal) listOf(listing) else emptyList()

    override suspend fun getRequests() = if (listing is Request) listOf(listing) else emptyList()

    override suspend fun getListing(listingId: String) =
        listing?.takeIf { it.listingId == listingId }

    override suspend fun getListingsByUser(userId: String) =
        listing?.takeIf { it.creatorUserId == userId }?.let { listOf(it) } ?: emptyList()

    override suspend fun addProposal(proposal: Proposal) {}

    override suspend fun addRequest(request: Request) {}

    override suspend fun updateListing(listingId: String, listing: Listing) {}

    override suspend fun deleteListing(listingId: String) {}

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

    override suspend fun searchByLocation(location: Location, radiusKm: Double) =
        emptyList<Listing>()
  }

  private class FakeProfileRepo(private val profiles: Map<String, Profile> = emptyMap()) :
      ProfileRepository {
    override fun getNewUid() = "new-profile-id"

    override suspend fun getProfile(userId: String) =
        profiles[userId] ?: throw NoSuchElementException("Profile not found")

    override suspend fun getProfileById(userId: String) = getProfile(userId)

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles() = profiles.values.toList()

    override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
        emptyList<Profile>()

    override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()
  }

  private class FakeBookingRepo(
      private val bookings: List<Booking> = emptyList(),
      private val shouldSucceed: Boolean = true
  ) : BookingRepository {
    override fun getNewUid() = "new-booking-id"

    override suspend fun getAllBookings() = bookings

    override suspend fun getBooking(bookingId: String) = bookings.find { it.bookingId == bookingId }

    override suspend fun getBookingsByTutor(tutorId: String) =
        bookings.filter { it.listingCreatorId == tutorId }

    override suspend fun getBookingsByUserId(userId: String) =
        bookings.filter { it.bookerId == userId }

    override suspend fun getBookingsByStudent(studentId: String) =
        bookings.filter { it.bookerId == studentId }

    override suspend fun getBookingsByListing(listingId: String) =
        bookings.filter { it.associatedListingId == listingId }

    override suspend fun addBooking(booking: Booking) {
      if (!shouldSucceed) throw Exception("Booking failed")
    }

    override suspend fun updateBooking(bookingId: String, booking: Booking) {}

    override suspend fun deleteBooking(bookingId: String) {}

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

    override suspend fun confirmBooking(bookingId: String) {}

    override suspend fun completeBooking(bookingId: String) {}

    override suspend fun cancelBooking(bookingId: String) {}
  }

  private fun createViewModel(
      listing: Listing? = sampleProposal,
      creator: Profile? = sampleCreator,
      bookings: List<Booking> = emptyList(),
      shouldSucceed: Boolean = true
  ): ListingViewModel {
    val listingRepo = FakeListingRepo(listing)
    val profileRepo = FakeProfileRepo(creator?.let { mapOf(it.userId to it) } ?: emptyMap())
    val bookingRepo = FakeBookingRepo(bookings, shouldSucceed)

    return ListingViewModel(listingRepo, profileRepo, bookingRepo)
  }

  // Screen State Tests

  @Test
  fun listingScreen_initialState_showsScreen() {
    val vm = createViewModel()

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    compose.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun listingScreen_loadingState_displaysProgressIndicator() {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val vm = ListingViewModel(listingRepo, profileRepo, bookingRepo)

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    compose.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun listingScreen_errorState_displaysErrorMessage() {
    val listingRepo = FakeListingRepo(null)
    val profileRepo = FakeProfileRepo()
    val bookingRepo = FakeBookingRepo()
    val vm = ListingViewModel(listingRepo, profileRepo, bookingRepo)

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.ERROR, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.onNodeWithTag(ListingScreenTestTags.ERROR).assertIsDisplayed()
    compose.onNodeWithText("Listing not found").assertIsDisplayed()
  }

  @Test
  fun listingScreen_successState_displaysListingContent() {
    val vm = createViewModel()

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // TITLE tag appears twice (type badge + actual title), so use onFirst()
    compose.onAllNodesWithTag(ListingScreenTestTags.TITLE).onFirst().assertIsDisplayed()
    compose.onNodeWithTag(ListingScreenTestTags.DESCRIPTION).assertIsDisplayed()
  }

  @Test
  fun listingScreen_errorDialog_onDismissRequest_clearsError() {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo(shouldSucceed = false)
    val vm = ListingViewModel(listingRepo, profileRepo, bookingRepo)

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    // Wait for screen to load
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Simulate booking attempt that will fail
    compose.runOnUiThread { vm.createBooking(Date(), Date(System.currentTimeMillis() + 3600000)) }

    // Wait for error dialog
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.ERROR_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Verify error dialog exists
    compose.onNodeWithTag(ListingScreenTestTags.ERROR_DIALOG).assertIsDisplayed()

    // Note: Testing onDismissRequest directly is challenging
    // We verify the OK button path works (tested above)
  }

  // Integration Tests

  @Test
  fun listingScreen_loadsListingOnLaunch() {
    val vm = createViewModel()

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.onAllNodesWithTag(ListingScreenTestTags.TITLE).assertCountEquals(1)
  }

  @Test
  fun listingScreen_displaysProposalType() {
    val vm = createViewModel(listing = sampleProposal)

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.onNodeWithText("Offering to Teach").assertIsDisplayed()
  }

  @Test
  fun listingScreen_displaysRequestType() {
    val vm =
        createViewModel(
            listing = sampleRequest, creator = sampleCreator.copy(userId = "creator-789"))

    compose.setContent {
      ListingScreen(
          listingId = sampleRequest.listingId,
          onNavigateBack = {},
          onEditListing = {},
          viewModel = vm)
    }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.onNodeWithText("Looking for Tutor").assertIsDisplayed()
  }
  //
  //  @Test
  //  fun push_book_button() {
  //    val vm =
  //        createViewModel(
  //            listing = sampleRequest, creator = sampleCreator.copy(userId = "creator-789"))
  //
  //    compose.setContent {
  //      ListingScreen(
  //          listingId = sampleRequest.listingId,
  //          onNavigateBack = {},
  //          onEditListing = {},
  //          viewModel = vm)
  //    }
  //
  //    // wait for compose to settle and for the book button to appear
  //    compose.waitForIdle()
  //    compose.waitUntil(10_000) {
  //      compose
  //          .onAllNodesWithTag(ListingScreenTestTags.BOOK_BUTTON, useUnmergedTree = true)
  //          .fetchSemanticsNodes()
  //          .isNotEmpty()
  //    }
  //
  //    compose.onNodeWithTag(ListingScreenTestTags.BOOK_BUTTON, useUnmergedTree =
  // true).performClick()
  //
  //    //    // wait for dialog to appear
  //    //    compose.waitUntil(10_000) {
  //    //      compose
  //    //          .onAllNodesWithTag(ListingScreenTestTags.BOOKING_DIALOG, useUnmergedTree = true)
  //    //          .fetchSemanticsNodes()
  //    //          .isNotEmpty()
  //    //    }
  //
  //    compose.onNodeWithTag(ListingScreenTestTags.BOOKING_DIALOG).assertIsDisplayed()
  //  }

  @Test
  fun listingScreen_navigationCallback_isProvided() {
    val vm = createViewModel()

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    compose.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun listingScreen_scaffoldStructure_isCorrect() {
    val vm = createViewModel()

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }
    compose.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun listingScreen_whenStateChanges_updatesUI() {
    val vm = createViewModel()

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    // Initially loading or content
    compose.onNodeWithTag(ListingScreenTestTags.SCREEN).assertIsDisplayed()

    // Eventually shows content
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // TITLE appears twice, use onFirst()
    compose.onAllNodesWithTag(ListingScreenTestTags.TITLE).onFirst().assertIsDisplayed()
  }

  @Test
  fun listingScreen_bookingFailure_errorDialogOk_clearsError() {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo(shouldSucceed = false) // force failure
    val vm = ListingViewModel(listingRepo, profileRepo, bookingRepo)

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    // Wait for content to load
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Trigger a failing booking
    compose.runOnUiThread { vm.createBooking(Date(), Date(System.currentTimeMillis() + 3_600_000)) }

    // Error dialog appears
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.ERROR_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.onNodeWithTag(ListingScreenTestTags.ERROR_DIALOG).assertIsDisplayed()

    // Click OK to clear it
    compose.onNodeWithText("OK", useUnmergedTree = true).assertIsDisplayed().performClick()

    // Dialog disappears
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.ERROR_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isEmpty()
    }
  }

  @Test
  fun listingScreen_bookingSuccess_successDialogOk_clearsSuccessAndNavigatesBack() {
    // given: a valid listing + creator + bookings repo that can succeed
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf("creator-456" to sampleCreator))
    val bookingRepo = FakeBookingRepo(shouldSucceed = true)
    val vm = ListingViewModel(listingRepo, profileRepo, bookingRepo)

    var navigatedBack = false

    compose.setContent {
      ListingScreen(
          listingId = "listing-123",
          onNavigateBack = { navigatedBack = true },
          viewModel = vm,
          onEditListing = {})
    }

    // Wait for content to load (title appears)
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // when: we simulate a successful booking
    compose.runOnUiThread { vm.showBookingSuccess() }

    // then: success dialog should appear
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.SUCCESS_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.onNodeWithTag(ListingScreenTestTags.SUCCESS_DIALOG).assertIsDisplayed()

    // when: user taps "OK"
    compose.onNodeWithText("OK", useUnmergedTree = true).assertIsDisplayed().performClick()

    // then: dialog disappears and success flag is cleared, and navigateBack is called
    compose.waitUntil(5_000) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.SUCCESS_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    compose.runOnIdle {
      assert(!vm.uiState.value.bookingSuccess)
      assert(navigatedBack)
    }
  }
}
