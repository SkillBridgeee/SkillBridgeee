package com.android.sample.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
    private const val TEST_BOOKING_ID = "b1"
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

    // Should not mark any rating progress flags
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
      BookingDetailsContent(
          uiState = uiState,
          onCreatorClick = {},
          onBookerClick = {},
          onMarkCompleted = { clicked = true },
          onSubmitBookerRatings = { _, _ -> },
          onSubmitCreatorRating = { _ -> },
          onPaymentComplete = {},
          onPaymentReceived = {},
      )
    }

    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assert(clicked)
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

    // Wait until the error indicator is actually in the tree
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
}
