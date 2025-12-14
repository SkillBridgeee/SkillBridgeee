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

    vm.submitBookerRatings(userStars = 0, listingStars = 3, userComment = "", listingComment = "")
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

    vm.submitBookerRatings(userStars = 3, listingStars = 6, userComment = "", listingComment = "")
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

    vm.submitBookerRatings(userStars = 5, listingStars = 5, userComment = "", listingComment = "")
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

    vm.submitBookerRatings(userStars = 5, listingStars = 5, userComment = "", listingComment = "")
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
  fun markCompletedButton_isVisible_whenStatusConfirmed_andCallsCallback() {
    val booking =
        Booking(
            bookingId = "booking-1",
            associatedListingId = "listing-1",
            listingCreatorId = "creator-1",
            bookerId = "student-1",
            status = BookingStatus.CONFIRMED,
        )

    val uiState =
        BookingUIState(
            booking = booking,
            listing = Proposal(),
            creatorProfile = Profile(),
            loadError = false,
            ratingProgress = RatingProgress(),
        )

    var clicked = false

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = { clicked = true },
            onSubmitBookerRatings = { _, _, _, _ -> },
            onSubmitCreatorRating = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {},
        )
      }
    }

    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.runOnIdle { assert(clicked) }
  }

  private fun fakeViewModel2() =
      BookingDetailsViewModel(
          bookingRepository =
              object : BookingRepository by fakeBookingRepo {
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
              },
          listingRepository = fakeListingRepo,
          profileRepository = fakeProfileRepo)

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
            onSubmitBookerRatings = { _, _, _, _ -> },
            onSubmitCreatorRating = { _, _ -> },
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

  // ---------- NEW COVERAGE TESTS ----------

  @Test
  fun creatorRatingSection_submit_callsCallback() {
    var receivedStars: Int? = null
    var receivedComment: String? = null

    val uiState = completedBookingUiState().copy(isCreator = true, isBooker = false)

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitBookerRatings = { _, _, _, _ -> },
            onSubmitCreatorRating = { stars, comment ->
              receivedStars = stars
              receivedComment = comment
            },
            onPaymentComplete = {},
            onPaymentReceived = {},
        )
      }
    }

    composeTestRule.swipeUpUntilDisplayed(hasTestTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON))
    composeTestRule.clickStarInRow(rowIndex = 0, star = 4)

    // If your UI has a comment TextField with a testTag, set it here.
    // Example:
    // composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_RATING_COMMENT)
    //   .performTextInput("Great session")

    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertIsEnabled()
        .performClick()

    composeTestRule.runOnIdle {
      assert(receivedStars == 4)
      // If you did not input text, this should remain "" (or whatever default you used in UI)
      assert(receivedComment != null)
    }
  }

  @Test
  fun bookerRatingSection_submit_callsCallback() {
    val uiState = completedBookingUiState().copy(isBooker = true, isCreator = false)

    var receivedTutorStars = -1
    var receivedListingStars = -1
    var receivedUserComment: String? = null
    var receivedListingComment: String? = null

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onBookerClick = {},
            onMarkCompleted = {},
            onSubmitBookerRatings = { tutor, listing, userComment, listingComment ->
              receivedTutorStars = tutor
              receivedListingStars = listing
              receivedUserComment = userComment
              receivedListingComment = listingComment
            },
            onSubmitCreatorRating = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {},
        )
      }
    }

    composeTestRule.swipeUpUntilDisplayed(hasTestTag(BookingDetailsTestTag.RATING_SECTION))

    // rowIndex 0 = user row, rowIndex 1 = listing row
    composeTestRule.clickStarInRow(rowIndex = 0, star = 2)
    composeTestRule.clickStarInRow(rowIndex = 1, star = 5)

    // If your UI has comment TextFields with testTags, set them here.
    // Example:
    // composeTestRule.onNodeWithTag(BookingDetailsTestTag.BOOKER_USER_RATING_COMMENT)
    //   .performTextInput("Nice teacher")
    // composeTestRule.onNodeWithTag(BookingDetailsTestTag.BOOKER_LISTING_RATING_COMMENT)
    //   .performTextInput("Accurate description")

    composeTestRule.swipeUpUntilDisplayed(hasTestTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON))
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()
        .assertIsEnabled()
        .performClick()

    composeTestRule.runOnIdle {
      assert(receivedTutorStars == 2)
      assert(receivedListingStars == 5)
      assert(receivedUserComment != null)
      assert(receivedListingComment != null)
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
            onSubmitBookerRatings = { _, _, _, _ -> },
            onSubmitCreatorRating = { _, _ -> },
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
            onSubmitBookerRatings = { _, _, _, _ -> },
            onSubmitCreatorRating = { _, _ -> },
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
            onSubmitBookerRatings = { _, _, _, _ -> },
            onSubmitCreatorRating = { _, _ -> },
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
}
