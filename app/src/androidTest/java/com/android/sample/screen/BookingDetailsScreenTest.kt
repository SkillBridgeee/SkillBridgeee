package com.android.sample.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.PaymentStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.BookingDetailsContent
import com.android.sample.ui.bookings.BookingDetailsScreen
import com.android.sample.ui.bookings.BookingDetailsTestTag
import com.android.sample.ui.bookings.BookingDetailsViewModel
import com.android.sample.ui.bookings.BookingUIState
import com.android.sample.ui.bookings.RatingProgress
import com.android.sample.ui.components.RatingStarsInputTestTags
import com.android.sample.ui.listing.ListingScreenTestTags
import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingDetailsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  companion object {
    private const val TEST_TUTOR_ID = "u1"
    private const val TEST_LISTING_ID = "l1"
  }

  private fun assertNoRatings(progress: RatingProgress) {
    assert(!progress.bookerRatedTutor)
    assert(!progress.bookerRatedStudent)
    assert(!progress.bookerRatedListing)
    assert(!progress.creatorRatedTutor)
    assert(!progress.creatorRatedStudent)
  }

  @Before
  fun setUp() {
    RatingRepositoryProvider.init(ApplicationProvider.getApplicationContext())
  }

  // ----- FAKES -----

  private val fakeBookingRepo =
      object : BookingRepository {
        override fun getNewUid() = "b1"

        override suspend fun getBooking(bookingId: String) =
            Booking(
                bookingId = bookingId,
                associatedListingId = "l1",
                listingCreatorId = "u1",
                price = 50.0,
                sessionStart = Date(1736546400000),
                sessionEnd = Date(1736550000000),
                status = BookingStatus.PENDING,
                bookerId = "asdf")

        override suspend fun getBookingsByUserId(userId: String) = emptyList<Booking>()

        override suspend fun getAllBookings() = emptyList<Booking>()

        override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

        override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

        override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

        override suspend fun addBooking(booking: Booking) {}

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

  private val fakeListingRepo =
      object : ListingRepository {
        override fun getNewUid() = "l1"

        override suspend fun getListing(listingId: String) =
            Proposal(
                listingId = listingId,
                description = "Cours de maths",
                skill = Skill(skill = "Algebra", mainSubject = MainSubject.ACADEMICS),
                location = Location(name = "Geneva"))

        override suspend fun getAllListings() = emptyList<Listing>()

        override suspend fun getProposals() = emptyList<Proposal>()

        override suspend fun getRequests() = emptyList<Request>()

        override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

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

  private val fakeProfileRepo =
      object : ProfileRepository {
        override fun getNewUid() = "u1"

        override fun getCurrentUserId() = "u1"

        override suspend fun getProfile(userId: String) =
            when (userId) {
              "u1" -> Profile(userId = userId, name = "John Doe", email = "john.doe@example.com")
              "asdf" ->
                  Profile(userId = userId, name = "Student Booker", email = "student@example.com")
              else -> Profile(userId = userId, name = "User $userId", email = "$userId@example.com")
            }

        override suspend fun getProfileById(userId: String) = getProfile(userId)

        override suspend fun addProfile(profile: Profile) {}

        override suspend fun updateProfile(userId: String, profile: Profile) {}

        override suspend fun deleteProfile(userId: String) {}

        override suspend fun getAllProfiles() = emptyList<Profile>()

        override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
            emptyList<Profile>()

        override suspend fun getSkillsForUser(userId: String) = emptyList<Skill>()

        override suspend fun updateTutorRatingFields(
            userId: String,
            averageRating: Double,
            totalRatings: Int
        ) {}

        override suspend fun updateStudentRatingFields(
            userId: String,
            averageRating: Double,
            totalRatings: Int
        ) {}
      }

  private fun fakeViewModel() =
      BookingDetailsViewModel(
          bookingRepository = fakeBookingRepo,
          listingRepository = fakeListingRepo,
          profileRepository = fakeProfileRepo)

  // ----- SCROLL + CLICK HELPERS (FOR Column + verticalScroll) -----

  private fun ComposeTestRule.swipeUpUntilDisplayed(
      matcher: SemanticsMatcher,
      timeoutMs: Long = 5_000L,
      maxSwipes: Int = 10
  ) {
    // Wait until it exists somewhere in the tree
    waitUntil(timeoutMs) {
      onAllNodes(matcher, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
    }

    repeat(maxSwipes) {
      val displayed =
          runCatching {
                onNode(matcher, useUnmergedTree = true).assertIsDisplayed()
                true
              }
              .getOrDefault(false)

      if (displayed) return

      onRoot().performTouchInput { swipeUp() }
      waitForIdle()
    }

    // Final assert gives a good error if still not visible
    onNode(matcher, useUnmergedTree = true).assertIsDisplayed()
  }

  /**
   * Click a specific star within a specific rating row.
   *
   * rowIndex:
   * - Creator rating: 0
   * - Booker rating: 0 for Tutor/Student row, 1 for Listing row
   */
  private fun ComposeTestRule.clickStarInRow(rowIndex: Int, star: Int) {
    val tag = "${RatingStarsInputTestTags.STAR_PREFIX}$star"

    waitUntil(5_000) {
      onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().size > rowIndex
    }

    // Make sure we scrolled some (stars are usually below the fold)
    onRoot().performTouchInput { swipeUp() }
    waitForIdle()

    val node = onAllNodesWithTag(tag, useUnmergedTree = true)[rowIndex]
    node.assertExists()
    node.assertIsDisplayed()
    node.performClick()
  }

  // ----- TESTS -----

  @Test
  fun bookingDetailsScreen_displaysAllSections() {
    val vm = fakeViewModel()
    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitUntil {
      composeTestRule.onAllNodesWithText("John Doe").fetchSemanticsNodes().isNotEmpty()
    }

    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.LISTING_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.SCHEDULE_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.DESCRIPTION_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.STATUS).assertExists()

    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_NAME).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_EMAIL).assertExists()
  }

  @Test
  fun bookingDetailsScreen_clickMoreInfo_callsCallback() {
    var clickedId: String? = null
    val vm = fakeViewModel()

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    associatedListingId = "list-123",
                ),
            listing = Proposal(),
            creatorProfile = Profile(userId = "u1", name = "Teacher"),
            bookerProfile = Profile(userId = "student", name = "Student"),
        ))

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = { clickedId = it })
    }

    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.MORE_INFO_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(clickedId == "u1")
  }

  @Test
  fun viewModel_submitBookerRatings_validatesStarRange() {
    val vm = fakeViewModel()
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.COMPLETED,
                ),
            listing = Proposal(listingId = "l1"),
            ratingProgress = RatingProgress(),
        ))

    vm.submitBookerRatings(userStars = 0, listingStars = 3)
    Thread.sleep(200)
    assert(vm.bookingUiState.value.loadError)

    // reset
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.COMPLETED,
                ),
            listing = Proposal(listingId = "l1"),
            ratingProgress = RatingProgress(),
        ))

    vm.submitBookerRatings(userStars = 3, listingStars = 6)
    Thread.sleep(200)
    assert(vm.bookingUiState.value.loadError)
  }

  @Test
  fun viewModel_submitBookerRatings_ignoresIfNotCompleted() {
    val vm = fakeViewModel()
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.PENDING,
                ),
            listing = Proposal(listingId = "l1"),
            ratingProgress = RatingProgress(),
        ))

    vm.submitBookerRatings(userStars = 5, listingStars = 5)
    Thread.sleep(200)

    assertNoRatings(vm.bookingUiState.value.ratingProgress)
  }

  @Test
  fun viewModel_submitBookerRatings_handlesRepositoryError_setsLoadError() {
    val explodingRatingRepo =
        object : RatingRepository by RatingRepositoryProvider.repository {
          override suspend fun addRating(rating: com.android.sample.model.rating.Rating) {
            throw Exception("rating repo failure")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = fakeBookingRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo,
            ratingRepository = explodingRatingRepo,
        )

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.COMPLETED,
                ),
            listing = Proposal(listingId = "l1"),
            ratingProgress = RatingProgress(),
        ))

    vm.submitBookerRatings(userStars = 5, listingStars = 5)
    Thread.sleep(250)

    assert(vm.bookingUiState.value.loadError)
  }

  private fun completedBookingUiState(): BookingUIState {
    val booking =
        Booking(
            bookingId = "booking-rating-completed",
            associatedListingId = "listing-rating",
            listingCreatorId = "creator-rating",
            bookerId = "student-rating",
            status = BookingStatus.COMPLETED,
        )

    val listing =
        Proposal(
            listingId = "listing-rating",
            description = "Some course",
            skill = Skill(skill = "Algebra", mainSubject = MainSubject.ACADEMICS),
            location = Location(name = "Geneva"),
        )

    return BookingUIState(
        booking = booking,
        listing = listing,
        creatorProfile = Profile(),
        loadError = false,
        ratingProgress = RatingProgress(),
    )
  }

  @Test
  fun bookingDetailsScreen_errorScreen() {
    val errorRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String): Booking {
            throw IllegalStateException("boom")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodesWithTag(BookingDetailsTestTag.ERROR, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.ERROR, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun markCompletedButton_isNotVisible_whenStatusNotConfirmed() {
    // given: a PENDING booking
    val booking =
        Booking(
            bookingId = "booking-2",
            associatedListingId = "listing-2",
            listingCreatorId = "creator-2",
            bookerId = "student-2",
            status = BookingStatus.PENDING,
        )

    val uiState =
        BookingUIState(
            booking = booking, listing = Proposal(), creatorProfile = Profile(), loadError = false)

    composeTestRule.setContent {
      BookingDetailsContent(
          uiState = uiState,
          onCreatorClick = {},
          onBookerClick = {},
          onMarkCompleted = {},
          onSubmitStudentRatings = { _, _ -> },
          onPaymentComplete = {},
          onPaymentReceived = {},
      )
    }

    // then: button should not exist in the tree
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun bookingDetailsScreen_displaysAllSections2() {
    val vm = fakeViewModel2()
    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    // Vérifie les sections visibles
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.LISTING_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.SCHEDULE_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.DESCRIPTION_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.STATUS).assertExists()
  }

  @Test
  fun studentRatingSection_exists_whenBookingCompleted_andUserIsBooker() {
    val uiState = completedBookingUiState().copy(isBooker = true, isCreator = false)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitBookerRatings = { _, _ -> },
            onSubmitCreatorRating = { _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {},
        )
      }
    }

    composeTestRule.onNodeWithTag(BookingDetailsTestTag.RATING_SECTION).assertExists()
  }

  @Test
  fun bookingDetailsScreen_errorState_doesNotRenderContent() {
    val errorRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String): Booking {
            throw IllegalStateException("boom")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "error-id", onCreatorClick = {})
    }

    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodesWithTag(BookingDetailsTestTag.ERROR, useUnmergedTree = true)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.ERROR, useUnmergedTree = true)
        .assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertDoesNotExist()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertDoesNotExist()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.LISTING_SECTION).assertDoesNotExist()
  }

  @Test
  fun bookingDetailsScreen_successState_wiresOnSubmitStudentRatingsCallback() {
    // Test lines 116-118: onSubmitStudentRatings callback wiring
    val customRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String) =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "l1",
                  listingCreatorId = "u1",
                  bookerId = "student",
                  sessionStart = Date(),
                  sessionEnd = Date(System.currentTimeMillis() + 3600000),
                  status = BookingStatus.COMPLETED, // Must be COMPLETED to show rating section
                  price = 50.0)
        }

    var receivedTutorStars = -1
    var receivedListingStars = -1

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitBookerRatings = { tutor, listing ->
              receivedTutorStars = tutor
              receivedListingStars = listing
            },
            onSubmitCreatorRating = { _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {},
        )
      }
    }

    composeTestRule.swipeUpUntilDisplayed(hasTestTag(BookingDetailsTestTag.RATING_SECTION))

    // rowIndex 0 = user row, rowIndex 1 = listing row
    composeTestRule.clickStarInRow(rowIndex = 0, star = 2)
    composeTestRule.clickStarInRow(rowIndex = 1, star = 5)

    composeTestRule.swipeUpUntilDisplayed(hasTestTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON))
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertIsEnabled()
        .performClick()

    composeTestRule.runOnIdle {
      assert(receivedTutorStars == 2)
      assert(receivedListingStars == 5)
    }
  }

  @Test
  fun paymentPending_student_canMarkPaymentComplete() {
    var clicked = false

    val base = completedBookingUiState()
    val uiState =
        base.copy(
            isBooker = true,
            isCreator = false,
            booking =
                base.booking.copy(
                    status = BookingStatus.CONFIRMED,
                    paymentStatus = PaymentStatus.PENDING_PAYMENT))

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitBookerRatings = { _, _ -> },
            onSubmitCreatorRating = { _ -> },
            onPaymentComplete = { clicked = true },
            onPaymentReceived = {},
        )
      }
    }

    composeTestRule.swipeUpUntilDisplayed(hasTestTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON))
    composeTestRule.onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON).performClick()
    assert(clicked)
  }

  @Test
  fun paymentPaid_tutor_canConfirmPaymentReceived() {
    var clicked = false

    val base = completedBookingUiState()
    val uiState =
        base.copy(
            isCreator = true,
            isBooker = false,
            booking =
                base.booking.copy(
                    status = BookingStatus.CONFIRMED, paymentStatus = PaymentStatus.PAID))

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitBookerRatings = { _, _ -> },
            onSubmitCreatorRating = { _ -> },
            onPaymentComplete = {},
            onPaymentReceived = { clicked = true },
        )
      }
    }

    composeTestRule.swipeUpUntilDisplayed(hasTestTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON))
    composeTestRule.onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON).performClick()
    assert(clicked)
  }

  @Test
  fun paymentConfirmed_showsConfirmationMessage() {
    val booking =
        Booking(
            bookingId = "b1",
            associatedListingId = "l1",
            listingCreatorId = TEST_TUTOR_ID,
            bookerId = "u2",
            status = BookingStatus.CONFIRMED,
            paymentStatus = PaymentStatus.CONFIRMED,
            price = 100.0)

    val uiState =
        BookingUIState(
            booking = booking,
            listing = Proposal(listingId = TEST_LISTING_ID, creatorUserId = TEST_TUTOR_ID),
            creatorProfile = Profile(userId = TEST_TUTOR_ID, name = "Tutor"),
            bookerProfile = Profile(userId = "u2", name = "Student"),
            isCreator = false,
            isBooker = false)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitBookerRatings = { _, _ -> },
            onSubmitCreatorRating = { _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    composeTestRule.swipeUpUntilDisplayed(
        SemanticsMatcher.expectValue(
            androidx.compose.ui.semantics.SemanticsProperties.Text,
            listOf(
                androidx.compose.ui.text.AnnotatedString(
                    "Payment has been successfully completed and confirmed!"))))

    composeTestRule
        .onNodeWithText("Payment has been successfully completed and confirmed!")
        .assertIsDisplayed()
  }

  // ============================================================
  // TESTS FOR LISTING TYPE SPECIFIC PAYMENT BUTTON BEHAVIOR
  // ============================================================

  /**
   * Helper to create a BookingUIState for PROPOSAL listing type. In PROPOSAL: Creator is tutor,
   * booker is student (student pays).
   */
  private fun proposalBookingState(
      isTutor: Boolean,
      paymentStatus: PaymentStatus = PaymentStatus.PENDING_PAYMENT
  ): BookingUIState {
    return BookingUIState(
        booking =
            Booking(
                bookingId = "booking-proposal",
                listingCreatorId = "tutor-id",
                bookerId = "student-id",
                associatedListingId = "listing-proposal",
                price = 50.0,
                sessionStart = Date(),
                sessionEnd = Date(System.currentTimeMillis() + 3600000),
                status = BookingStatus.CONFIRMED,
                paymentStatus = paymentStatus),
        listing =
            Proposal(
                listingId = "listing-proposal",
                description = "Teaching math",
                skill = Skill(skill = "Math", mainSubject = MainSubject.ACADEMICS),
                location = Location(name = "Geneva")),
        creatorProfile =
            Profile(userId = "tutor-id", name = "Tutor Name", email = "tutor@example.com"),
        bookerProfile =
            Profile(userId = "student-id", name = "Student Name", email = "student@example.com"),
        isTutor = isTutor,
        onAcceptBooking = {},
        onDenyBooking = {})
  }

  /**
   * Helper to create a BookingUIState for REQUEST listing type. In REQUEST: Creator is student
   * (looking for tutor), booker is tutor (student pays).
   */
  private fun requestBookingState(
      isTutor: Boolean,
      paymentStatus: PaymentStatus = PaymentStatus.PENDING_PAYMENT
  ): BookingUIState {
    return BookingUIState(
        booking =
            Booking(
                bookingId = "booking-request",
                listingCreatorId = "student-id", // Creator is student in REQUEST
                bookerId = "tutor-id", // Booker is tutor in REQUEST
                associatedListingId = "listing-request",
                price = 50.0,
                sessionStart = Date(),
                sessionEnd = Date(System.currentTimeMillis() + 3600000),
                status = BookingStatus.CONFIRMED,
                paymentStatus = paymentStatus),
        listing =
            Request(
                listingId = "listing-request",
                description = "Looking for math tutor",
                skill = Skill(skill = "Math", mainSubject = MainSubject.ACADEMICS),
                location = Location(name = "Geneva")),
        creatorProfile =
            Profile(userId = "student-id", name = "Student Name", email = "student@example.com"),
        bookerProfile =
            Profile(userId = "tutor-id", name = "Tutor Name", email = "tutor@example.com"),
        isTutor = isTutor, // isTutor means current user is creator (student in REQUEST)
        onAcceptBooking = {},
        onDenyBooking = {})
  }

  @Test
  fun paymentSection_proposal_showsPaymentCompleteButton_forStudent() {
    // PROPOSAL: Student (booker, isTutor=false) should see Payment Complete button
    val uiState =
        proposalBookingState(isTutor = false, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    var paymentCompleteCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = { paymentCompleteCalled = true },
            onPaymentReceived = {})
      }
    }

    // Student should see the Payment Complete button
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON)
        .assertExists()
        .performScrollTo()
        .performClick()

    assert(paymentCompleteCalled)
  }

  @Test
  fun paymentSection_proposal_hidesPaymentCompleteButton_forTutor() {
    // PROPOSAL: Tutor (creator, isTutor=true) should NOT see Payment Complete button
    val uiState =
        proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Tutor should NOT see the Payment Complete button
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun paymentSection_proposal_showsPaymentReceivedButton_forTutor() {
    // PROPOSAL: Tutor (creator, isTutor=true) should see Payment Received button when status is
    // PAID
    val uiState = proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.PAID)

    var paymentReceivedCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = { paymentReceivedCalled = true })
      }
    }

    // Tutor should see the Payment Received button
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON)
        .assertExists()
        .performScrollTo()
        .performClick()

    assert(paymentReceivedCalled)
  }

  @Test
  fun paymentSection_proposal_hidesPaymentReceivedButton_forStudent() {
    // PROPOSAL: Student (booker, isTutor=false) should NOT see Payment Received button
    val uiState = proposalBookingState(isTutor = false, paymentStatus = PaymentStatus.PAID)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Student should NOT see the Payment Received button
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun paymentSection_request_showsPaymentCompleteButton_forCreator() {
    // REQUEST: Creator (student, isTutor=true because creator) should see Payment Complete button
    val uiState = requestBookingState(isTutor = true, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    var paymentCompleteCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = { paymentCompleteCalled = true },
            onPaymentReceived = {})
      }
    }

    // Creator (student in REQUEST) should see the Payment Complete button
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON)
        .assertExists()
        .performScrollTo()
        .performClick()

    assert(paymentCompleteCalled)
  }

  @Test
  fun paymentSection_request_hidesPaymentCompleteButton_forBooker() {
    // REQUEST: Booker (tutor, isTutor=false because not creator) should NOT see Payment Complete
    // button
    val uiState =
        requestBookingState(isTutor = false, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Booker (tutor in REQUEST) should NOT see the Payment Complete button
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun paymentSection_request_showsPaymentReceivedButton_forBooker() {
    // REQUEST: Booker (tutor, isTutor=false) should see Payment Received button when status is PAID
    val uiState = requestBookingState(isTutor = false, paymentStatus = PaymentStatus.PAID)

    var paymentReceivedCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = { paymentReceivedCalled = true })
      }
    }

    // Booker (tutor in REQUEST) should see the Payment Received button
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON)
        .assertExists()
        .performScrollTo()
        .performClick()

    assert(paymentReceivedCalled)
  }

  @Test
  fun paymentSection_request_hidesPaymentReceivedButton_forCreator() {
    // REQUEST: Creator (student, isTutor=true) should NOT see Payment Received button
    val uiState = requestBookingState(isTutor = true, paymentStatus = PaymentStatus.PAID)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Creator (student in REQUEST) should NOT see the Payment Received button
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun paymentSection_showsConfirmedMessage_forBothUsers() {
    // When payment is CONFIRMED, both users should see confirmation message
    val uiState = proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.CONFIRMED)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // No payment action buttons should be visible
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_COMPLETE_BUTTON)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON)
        .assertDoesNotExist()

    // Confirmation message should be shown
    composeTestRule
        .onNodeWithText("Payment has been successfully completed and confirmed!")
        .assertExists()
  }

  // ============================================================
  // TESTS FOR PAYMENT WARNING DIALOG WHEN COMPLETING BOOKING
  // ============================================================

  @Test
  fun completeButton_proposal_showsWarningDialog_forTutor_whenPaymentNotConfirmed() {
    // PROPOSAL: Tutor (creator, isTutor=true) is payment receiver
    // Should see warning when payment is not confirmed
    val uiState =
        proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Click complete button
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .performScrollTo()
        .performClick()

    // Warning dialog should appear
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_DIALOG).assertIsDisplayed()

    // Dialog should contain warning text
    composeTestRule.onNodeWithText("Payment Not Confirmed").assertIsDisplayed()
  }

  @Test
  fun completeButton_proposal_noWarningDialog_forTutor_whenPaymentConfirmed() {
    // PROPOSAL: Tutor (creator, isTutor=true) but payment is already CONFIRMED
    // Should NOT see warning
    val uiState = proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.CONFIRMED)

    var markCompletedCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = { markCompletedCalled = true },
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Click complete button
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .performScrollTo()
        .performClick()

    // Warning dialog should NOT appear
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_DIALOG).assertDoesNotExist()

    // Callback should be called directly
    assert(markCompletedCalled)
  }

  @Test
  fun completeButton_request_showsWarningDialog_forBooker_whenPaymentNotConfirmed() {
    // REQUEST: Booker (tutor, isTutor=false) is payment receiver
    // Should see warning when payment is not confirmed
    val uiState =
        requestBookingState(isTutor = false, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Click complete button
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .performScrollTo()
        .performClick()

    // Warning dialog should appear
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_DIALOG).assertIsDisplayed()
  }

  @Test
  fun warningDialog_confirmButton_callsOnMarkCompleted() {
    // Test that clicking "Yes, Complete Anyway" in the warning dialog calls the callback
    val uiState =
        proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    var markCompletedCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = { markCompletedCalled = true },
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Click complete button to show dialog
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .performScrollTo()
        .performClick()

    // Verify dialog is shown
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_DIALOG).assertIsDisplayed()

    // Click confirm button
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_CONFIRM).performClick()

    // Dialog should be dismissed
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_DIALOG).assertDoesNotExist()

    // Callback should be called
    assert(markCompletedCalled)
  }

  @Test
  fun warningDialog_cancelButton_dismissesDialogWithoutCallback() {
    // Test that clicking "Cancel" in the warning dialog dismisses it without calling callback
    val uiState =
        proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    var markCompletedCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = { markCompletedCalled = true },
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Click complete button to show dialog
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .performScrollTo()
        .performClick()

    // Verify dialog is shown
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_DIALOG).assertIsDisplayed()

    // Click cancel button
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_CANCEL).performClick()

    // Dialog should be dismissed
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.PAYMENT_WARNING_DIALOG).assertDoesNotExist()

    // Callback should NOT be called
    assert(!markCompletedCalled)
  }

  @Test
  fun warningDialog_showsCorrectMessage() {
    // Test that the warning dialog shows the correct message
    val uiState = proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.PAID)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Click complete button to show dialog
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .performScrollTo()
        .performClick()

    // Verify dialog shows correct title
    composeTestRule.onNodeWithText("Payment Not Confirmed").assertIsDisplayed()

    // Verify dialog shows correct message
    composeTestRule
        .onNodeWithText(
            "The payment has not been confirmed yet. Are you sure you want to mark this booking as completed before the payment has been accepted?",
            substring = true)
        .assertExists()

    // Verify buttons have correct text
    composeTestRule.onNodeWithText("Yes, Complete Anyway").assertExists()
    composeTestRule.onNodeWithText("Cancel").assertExists()
  }

  // ============================================================
  // TESTS FOR PAYER CANNOT COMPLETE UNTIL PAYMENT CONFIRMED
  // ============================================================

  @Test
  fun completeButton_proposal_isDisabled_forStudent_whenPaymentNotConfirmed() {
    // PROPOSAL: Student (booker, isTutor=false) is the payer
    // Button should be disabled when payment is not confirmed
    val uiState =
        proposalBookingState(isTutor = false, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Button should be disabled
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON).assertIsNotEnabled()

    // Should show the payment required message
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.PAYMENT_REQUIRED_MESSAGE)
        .assertIsDisplayed()
  }

  @Test
  fun completeButton_proposal_isEnabled_forStudent_whenPaymentConfirmed() {
    // PROPOSAL: Student (booker, isTutor=false) is the payer
    // Button should be enabled when payment is confirmed
    val uiState = proposalBookingState(isTutor = false, paymentStatus = PaymentStatus.CONFIRMED)

    var markCompletedCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = { markCompletedCalled = true },
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Button should be enabled
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON).assertIsEnabled()

    // Should NOT show the payment required message
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.PAYMENT_REQUIRED_MESSAGE)
        .assertDoesNotExist()

    // Click should work
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .performScrollTo()
        .performClick()

    assert(markCompletedCalled)
  }

  @Test
  fun completeButton_proposal_isEnabled_forTutor_whenPaymentNotConfirmed() {
    // PROPOSAL: Tutor (creator, isTutor=true) is the payment receiver, NOT the payer
    // Button should be enabled (but will show warning dialog)
    val uiState =
        proposalBookingState(isTutor = true, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Button should be enabled for tutor
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON).assertIsEnabled()

    // Should NOT show the payment required message (that's only for payers)
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.PAYMENT_REQUIRED_MESSAGE)
        .assertDoesNotExist()
  }

  @Test
  fun completeButton_request_isDisabled_forCreator_whenPaymentNotConfirmed() {
    // REQUEST: Creator (student, isTutor=true) is the payer
    // Button should be disabled when payment is not confirmed
    val uiState = requestBookingState(isTutor = true, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Button should be disabled
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON).assertIsNotEnabled()

    // Should show the payment required message
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.PAYMENT_REQUIRED_MESSAGE)
        .assertIsDisplayed()
  }

  @Test
  fun completeButton_request_isEnabled_forCreator_whenPaymentConfirmed() {
    // REQUEST: Creator (student, isTutor=true) is the payer
    // Button should be enabled when payment is confirmed
    val uiState = requestBookingState(isTutor = true, paymentStatus = PaymentStatus.CONFIRMED)

    var markCompletedCalled = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = { markCompletedCalled = true },
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Button should be enabled
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON).assertIsEnabled()

    // Should NOT show the payment required message
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.PAYMENT_REQUIRED_MESSAGE)
        .assertDoesNotExist()

    // Click should work
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .performScrollTo()
        .performClick()

    assert(markCompletedCalled)
  }

  @Test
  fun completeButton_request_isEnabled_forBooker_whenPaymentNotConfirmed() {
    // REQUEST: Booker (tutor, isTutor=false) is the payment receiver, NOT the payer
    // Button should be enabled (but will show warning dialog)
    val uiState =
        requestBookingState(isTutor = false, paymentStatus = PaymentStatus.PENDING_PAYMENT)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Button should be enabled for tutor (booker in REQUEST)
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON).assertIsEnabled()

    // Should NOT show the payment required message (that's only for payers)
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.PAYMENT_REQUIRED_MESSAGE)
        .assertDoesNotExist()
  }

  @Test
  fun completeButton_showsCorrectMessage_whenDisabledForPayer() {
    // Test that the correct message is shown when button is disabled
    val uiState = proposalBookingState(isTutor = false, paymentStatus = PaymentStatus.PAID)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {})
      }
    }

    // Should show the correct message
    composeTestRule
        .onNodeWithText(
            "You cannot mark the booking as completed until the payment has been confirmed.")
        .assertIsDisplayed()
  }
}
