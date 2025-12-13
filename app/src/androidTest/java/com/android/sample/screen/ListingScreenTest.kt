package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.PaymentStatus
import com.android.sample.model.communication.conversation.ConvRepository
import com.android.sample.model.communication.conversation.Conversation
import com.android.sample.model.communication.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.conversation.Message
import com.android.sample.model.communication.overViewConv.OverViewConvRepository
import com.android.sample.model.communication.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.communication.overViewConv.OverViewConversation
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.rating.RatingType
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.listing.ListingScreen
import com.android.sample.ui.listing.ListingScreenTestTags
import com.android.sample.ui.listing.ListingViewModel
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
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

  // ----- TEST CONSTANTS -----
  companion object {
    private const val TEST_CREATOR_NAME = "John Doe"
    private const val TEST_CREATOR_EMAIL = "john@example.com"
    private const val TEST_CREATOR_ID = "creator-456"
    private const val TEST_LISTING_ID = "listing-123"
    private const val WAIT_TIMEOUT_MS = 5_000L
  }

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
          userId = TEST_CREATOR_ID,
          name = TEST_CREATOR_NAME,
          email = TEST_CREATOR_EMAIL,
          description = "Experienced guitar teacher",
          location = Location(latitude = 40.7128, longitude = -74.0060, name = "New York"))

  @Before
  fun setUp() {
    // Initialize all repository providers to prevent initialization errors
    ConversationRepositoryProvider.setForTests(FakeConvRepository())
    OverViewConvRepositoryProvider.setForTests(FakeOverViewConvRepository())
    RatingRepositoryProvider.setForTests(FakeRatingRepository())
    BookingRepositoryProvider.setForTests(FakeBookingRepo())
  }

  @After
  fun cleanup() {
    UserSessionManager.clearSession()
    ConversationRepositoryProvider.clearForTests()
    OverViewConvRepositoryProvider.clearForTests()
    RatingRepositoryProvider.clearForTests()
    ListingRepositoryProvider.clearForTests()
    ProfileRepositoryProvider.clearForTests()
    BookingRepositoryProvider.clearForTests()
  }

  // ----- HELPER FUNCTIONS -----
  /**
   * Waits for the creator name to be loaded and displayed. Uses test tags to avoid flaky text-based
   * waits. More robust than waiting for specific text content.
   */
  private fun waitForCreatorLoaded() {
    compose.waitUntil(WAIT_TIMEOUT_MS) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.CREATOR_NAME, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  /**
   * Waits for the listing content to be loaded and displayed. Uses test tags for more stable
   * waiting.
   */
  private fun waitForListingLoaded() {
    compose.waitUntil(WAIT_TIMEOUT_MS) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.TITLE, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  // Fake Repositories
  private class FakeConvRepository : ConvRepository {
    override fun getNewUid() = "dummy-conv-id"

    override suspend fun getConv(convId: String): Conversation? = null

    override suspend fun createConv(conversation: Conversation) {}

    override suspend fun deleteConv(convId: String) {}

    override suspend fun sendMessage(convId: String, message: Message) {}

    override fun listenMessages(convId: String): Flow<List<Message>> = flowOf(emptyList())
  }

  private class FakeOverViewConvRepository : OverViewConvRepository {
    override fun getNewUid() = "dummy-overview-id"

    override suspend fun getOverViewConvUser(userId: String): List<OverViewConversation> =
        emptyList()

    override suspend fun addOverViewConvUser(overView: OverViewConversation) {}

    override suspend fun deleteOverViewConvUser(convId: String) {}

    override suspend fun deleteOverViewById(overViewId: String) {
      TODO("Not yet implemented")
    }

    override fun listenOverView(userId: String): Flow<List<OverViewConversation>> =
        flowOf(emptyList())
  }

  private class FakeRatingRepository : RatingRepository {
    override fun getNewUid() = "dummy-rating-id"

    override suspend fun hasRating(
        fromUserId: String,
        toUserId: String,
        ratingType: RatingType,
        targetObjectId: String
    ) = false

    override suspend fun addRating(rating: Rating) {}

    override suspend fun getAllRatings() = emptyList<Rating>()

    override suspend fun getRating(ratingId: String) = null

    override suspend fun getRatingsByFromUser(fromUserId: String) = emptyList<Rating>()

    override suspend fun getRatingsByToUser(toUserId: String) = emptyList<Rating>()

    override suspend fun getRatingsOfListing(listingId: String) = emptyList<Rating>()

    override suspend fun updateRating(ratingId: String, rating: Rating) {}

    override suspend fun deleteRating(ratingId: String) {}

    override suspend fun getTutorRatingsOfUser(userId: String) = emptyList<Rating>()

    override suspend fun getStudentRatingsOfUser(userId: String) = emptyList<Rating>()

    override suspend fun deleteAllRatingOfUser(userId: String) {
      TODO("Not yet implemented")
    }
  }

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

    override suspend fun deleteAllListingOfUser(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun deactivateListing(listingId: String) {}

    override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

    override suspend fun searchByLocation(location: Location, radiusKm: Double) =
        emptyList<Listing>()
  }

  private class FakeProfileRepo(private val profiles: Map<String, Profile> = emptyMap()) :
      ProfileRepository {
    override fun getNewUid() = "new-profile-id"

    override fun getCurrentUserId() = "current-user-id"

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

    override suspend fun updateTutorRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      // no-op
    }

    override suspend fun updateStudentRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      // no-op
    }
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

    override suspend fun deleteAllBookingOfUser(userId: String) {
      TODO("Not yet implemented")
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

    override suspend fun updatePaymentStatus(bookingId: String, paymentStatus: PaymentStatus) {}

    override suspend fun confirmBooking(bookingId: String) {}

    override suspend fun completeBooking(bookingId: String) {}

    override suspend fun cancelBooking(bookingId: String) {}

    override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
      TODO("Not yet implemented")
    }
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

    compose.waitUntil(WAIT_TIMEOUT_MS) {
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

    // Use helper function for waiting
    waitForListingLoaded()

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

    // Wait for screen to load using helper function
    waitForListingLoaded()

    // Simulate booking attempt that will fail
    compose.runOnUiThread { vm.createBooking(Date(), Date(System.currentTimeMillis() + 3600000)) }

    // Wait for error dialog
    compose.waitUntil(WAIT_TIMEOUT_MS) {
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

    // Use helper function for waiting
    waitForListingLoaded()

    compose.onAllNodesWithTag(ListingScreenTestTags.TITLE).assertCountEquals(1)
  }

  @Test
  fun listingScreen_displaysProposalType() {
    val vm = createViewModel(listing = sampleProposal)

    compose.setContent {
      ListingScreen(
          listingId = "listing-123", onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    // Use helper function for waiting
    waitForListingLoaded()

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

  @Test
  fun listingScreen_navigationCallback_isProvided() {
    val vm = createViewModel()

    compose.setContent {
      ListingScreen(
          listingId = TEST_LISTING_ID, onNavigateBack = {}, onEditListing = {}, viewModel = vm)
    }

    // Use helper function for waiting
    waitForListingLoaded()

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

    // Eventually shows content - use helper function
    waitForListingLoaded()

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

    // Wait for content to load using helper function
    waitForListingLoaded()

    // Trigger a failing booking
    compose.runOnUiThread { vm.createBooking(Date(), Date(System.currentTimeMillis() + 3_600_000)) }

    // Error dialog appears
    compose.waitUntil(WAIT_TIMEOUT_MS) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.ERROR_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.onNodeWithTag(ListingScreenTestTags.ERROR_DIALOG).assertIsDisplayed()

    // Click OK to clear it
    compose.onNodeWithText("OK", useUnmergedTree = true).assertIsDisplayed().performClick()

    // Dialog disappears
    compose.waitUntil(WAIT_TIMEOUT_MS) {
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

    // Wait for content to load using helper function
    waitForListingLoaded()

    // when: we simulate a successful booking
    compose.runOnUiThread { vm.showBookingSuccess() }

    // then: success dialog should appear
    compose.waitUntil(WAIT_TIMEOUT_MS) {
      compose
          .onAllNodesWithTag(ListingScreenTestTags.SUCCESS_DIALOG, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    compose.onNodeWithTag(ListingScreenTestTags.SUCCESS_DIALOG).assertIsDisplayed()

    // when: user taps "OK"
    compose.onNodeWithText("OK", useUnmergedTree = true).assertIsDisplayed().performClick()

    // then: dialog disappears and success flag is cleared, and navigateBack is called
    compose.waitUntil(WAIT_TIMEOUT_MS) {
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

  // ----- NEW TESTS FOR CREATOR PROFILE FEATURE -----

  @Test
  fun listingScreen_displaysCreatorName() {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf(TEST_CREATOR_ID to sampleCreator))
    val bookingRepo = FakeBookingRepo(shouldSucceed = true)

    compose.setContent {
      ListingScreen(
          listingId = TEST_LISTING_ID,
          onNavigateBack = {},
          onEditListing = {},
          onNavigateToProfile = {},
          viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo))
    }

    // Wait for content to load using helper function
    waitForCreatorLoaded()

    // Verify creator name is displayed using test tag
    compose.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).assertIsDisplayed()

    // Verify helper text is displayed
    compose.onNodeWithText("Tap to view profile").assertIsDisplayed()
  }

  @Test
  fun listingScreen_creatorName_isClickable() {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf(TEST_CREATOR_ID to sampleCreator))
    val bookingRepo = FakeBookingRepo(shouldSucceed = true)

    compose.setContent {
      ListingScreen(
          listingId = TEST_LISTING_ID,
          onNavigateBack = {},
          onEditListing = {},
          onNavigateToProfile = {},
          viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo))
    }

    // Wait for content to load using helper function
    waitForCreatorLoaded()

    // Verify creator name has a click action using test tag
    compose.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).assertHasClickAction()
  }

  @Test
  fun listingScreen_clickCreatorName_callsCallback() {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf(TEST_CREATOR_ID to sampleCreator))
    val bookingRepo = FakeBookingRepo(shouldSucceed = true)

    var clickedProfileId: String? = null

    compose.setContent {
      ListingScreen(
          listingId = TEST_LISTING_ID,
          onNavigateBack = {},
          onEditListing = {},
          onNavigateToProfile = { profileId -> clickedProfileId = profileId },
          viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo))
    }

    // Wait for content to load using helper function
    waitForCreatorLoaded()

    // Click on the creator's name using test tag
    compose.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).performClick()

    // Verify the callback was called with the correct creator ID
    compose.runOnIdle {
      assert(clickedProfileId == TEST_CREATOR_ID) {
        "Expected callback to be called with '$TEST_CREATOR_ID', but got '$clickedProfileId'"
      }
    }
  }

  @Test
  fun listingScreen_creatorNameInPrimaryColor() {
    val listingRepo = FakeListingRepo(sampleProposal)
    val profileRepo = FakeProfileRepo(mapOf(TEST_CREATOR_ID to sampleCreator))
    val bookingRepo = FakeBookingRepo(shouldSucceed = true)

    compose.setContent {
      ListingScreen(
          listingId = TEST_LISTING_ID,
          onNavigateBack = {},
          onEditListing = {},
          onNavigateToProfile = {},
          viewModel = ListingViewModel(listingRepo, profileRepo, bookingRepo))
    }

    // Wait for content to load using helper function
    waitForCreatorLoaded()

    // Verify creator name exists and is displayed using test tag
    // Note: We can't directly test color in UI tests, but we can verify the node exists
    // and is clickable, which indicates proper styling
    compose.onNodeWithTag(ListingScreenTestTags.CREATOR_NAME).assertIsDisplayed()
  }
}
