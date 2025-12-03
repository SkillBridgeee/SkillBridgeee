package com.android.sample.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.android.sample.model.booking.*
import com.android.sample.model.listing.*
import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.*
import java.util.*
import kotlin.and
import kotlin.collections.get
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingDetailsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Initialize provider in the test process so calls to the provider won't crash.
    RatingRepositoryProvider.init(ApplicationProvider.getApplicationContext())

    // Alternatively, if you have a fake repo:
    // RatingRepositoryProvider.setForTests(FakeRatingRepository())

    // Now it's safe to call setContent / launch the screen.
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

        override suspend fun getRequests() = emptyList<com.android.sample.model.listing.Request>()

        override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

        override suspend fun addProposal(proposal: Proposal) {}

        override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

        override suspend fun updateListing(listingId: String, listing: Listing) {}

        override suspend fun deleteListing(listingId: String) {}

        override suspend fun deactivateListing(listingId: String) {}

        override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill) =
            emptyList<Listing>()

        override suspend fun searchByLocation(location: Location, radiusKm: Double) =
            emptyList<Listing>()
      }

  private val fakeProfileRepo =
      object : ProfileRepository {
        override fun getNewUid() = "u1"

        override fun getCurrentUserId() = "u1"

        override suspend fun getProfile(userId: String) =
            Profile(userId = userId, name = "John Doe", email = "john.doe@example.com")

        override suspend fun getProfileById(userId: String) = getProfile(userId)

        override suspend fun addProfile(profile: Profile) {}

        override suspend fun updateProfile(userId: String, profile: Profile) {}

        override suspend fun deleteProfile(userId: String) {}

        override suspend fun getAllProfiles() = emptyList<Profile>()

        override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
            emptyList<Profile>()

        override suspend fun getSkillsForUser(userId: String) =
            emptyList<com.android.sample.model.skill.Skill>()

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

    // Vérifie les sections visibles
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.LISTING_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.SCHEDULE_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.DESCRIPTION_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.STATUS).assertExists()

    // Vérifie le nom et email du créateur
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("john.doe@example.com").assertIsDisplayed()
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

  private val fakeProfileRepoError =
      object : ProfileRepository {
        override fun getNewUid() = "u1"

        override fun getCurrentUserId() = "u1"

        override suspend fun getProfile(userId: String) = throw error("test")

        override suspend fun getProfileById(userId: String) = getProfile(userId)

        override suspend fun addProfile(profile: Profile) {}

        override suspend fun updateProfile(userId: String, profile: Profile) {}

        override suspend fun deleteProfile(userId: String) {}

        override suspend fun getAllProfiles() = emptyList<Profile>()

        override suspend fun searchProfilesByLocation(location: Location, radiusKm: Double) =
            emptyList<Profile>()

        override suspend fun getSkillsForUser(userId: String) =
            emptyList<com.android.sample.model.skill.Skill>()

        override suspend fun updateTutorRatingFields(
            userId: String,
            averageRating: Double,
            totalRatings: Int
        ) {
          throw IllegalStateException("test")
        }

        override suspend fun updateStudentRatingFields(
            userId: String,
            averageRating: Double,
            totalRatings: Int
        ) {
          throw IllegalStateException("test")
        }
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
    )
  }

  private fun fakeViewModelError() =
      BookingDetailsViewModel(
          bookingRepository = fakeBookingRepo,
          listingRepository = fakeListingRepo,
          profileRepository = fakeProfileRepoError)

  @Test
  fun bookingDetailsScreen_errorScreen() {
    var clickedId: String? = null
    val vm = fakeViewModelError()
    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = { clickedId = it })
    }

    composeTestRule.onNodeWithTag(BookingDetailsTestTag.ERROR).assertIsDisplayed()
  }

  private val fakeBookingRepo2 =
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

        override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

        override suspend fun updatePaymentStatus(bookingId: String, paymentStatus: PaymentStatus) {}

        override suspend fun confirmBooking(bookingId: String) {}

        override suspend fun completeBooking(bookingId: String) {}

        override suspend fun cancelBooking(bookingId: String) {}
      }

  private val fakeListingRepo2 =
      object : ListingRepository {
        override fun getNewUid() = "l1"

        override suspend fun getListing(listingId: String) =
            Request(
                listingId = listingId,
                description = "Cours de maths",
                skill = Skill(skill = "Algebra", mainSubject = MainSubject.ACADEMICS),
                location = Location(name = "Geneva"))

        override suspend fun getAllListings() = emptyList<Listing>()

        override suspend fun getProposals() = emptyList<Proposal>()

        override suspend fun getRequests() = emptyList<com.android.sample.model.listing.Request>()

        override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

        override suspend fun addProposal(proposal: Proposal) {}

        override suspend fun addRequest(request: com.android.sample.model.listing.Request) {}

        override suspend fun updateListing(listingId: String, listing: Listing) {}

        override suspend fun deleteListing(listingId: String) {}

        override suspend fun deactivateListing(listingId: String) {}

        override suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill) =
            emptyList<Listing>()

        override suspend fun searchByLocation(location: Location, radiusKm: Double) =
            emptyList<Listing>()
      }

  private fun fakeViewModel2() =
      BookingDetailsViewModel(
          bookingRepository = fakeBookingRepo2,
          listingRepository = fakeListingRepo2,
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
  fun markCompletedButton_isVisible_whenStatusConfirmed_andCallsCallback() {
    // given: a CONFIRMED booking
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
            listing = Proposal(), // dummy listing is fine
            creatorProfile = Profile(),
            loadError = false)

    var clicked = false

    composeTestRule.setContent {
      BookingDetailsContent(
          uiState = uiState,
          onCreatorClick = {},
          onMarkCompleted = { clicked = true },
          onSubmitStudentRatings = { _, _ -> },
          onPaymentComplete = {},
          onPaymentReceived = {},
      )
    }

    // then: button is visible
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .assertIsDisplayed()
        .performClick()

    // and: callback was triggered
    assert(clicked)
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
  fun studentRatingSection_notVisible_whenBookingNotCompleted() {
    // given: a booking that is still PENDING
    val booking =
        Booking(
            bookingId = "booking-rating-pending",
            associatedListingId = "listing-rating",
            listingCreatorId = "creator-rating",
            bookerId = "student-rating",
            status = BookingStatus.PENDING,
        )

    val uiState =
        BookingUIState(
            booking = booking,
            listing = Proposal(),
            creatorProfile = Profile(),
            loadError = false,
        )

    composeTestRule.setContent {
      BookingDetailsContent(
          uiState = uiState,
          onCreatorClick = {},
          onMarkCompleted = {},
          onSubmitStudentRatings = { _, _ -> },
          onPaymentComplete = {},
          onPaymentReceived = {},
      )
    }

    // then: the rating section should not be in the tree
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.RATING_SECTION).assertDoesNotExist()
  }

  @Test
  fun studentRatingSection_exists_whenBookingCompleted() {
    val uiState = completedBookingUiState()

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { _, _ -> },
            onPaymentComplete = {},
            onPaymentReceived = {},
        )
      }
    }

    composeTestRule.onNodeWithTag(BookingDetailsTestTag.RATING_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.RATING_TUTOR).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.RATING_LISTING).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON).assertExists()
  }

  @Test
  fun studentRatingSection_submit_callsCallbackWithCurrentValues() {
    val uiState = completedBookingUiState()

    var callbackCalled = false
    var receivedTutorStars = -1
    var receivedListingStars = -1

    composeTestRule.setContent {
      MaterialTheme {
        BookingDetailsContent(
            uiState = uiState,
            onCreatorClick = {},
            onMarkCompleted = {},
            onSubmitStudentRatings = { tutorStars, listingStars ->
              callbackCalled = true
              receivedTutorStars = tutorStars
              receivedListingStars = listingStars
            },
            onPaymentComplete = {},
            onPaymentReceived = {},
        )
      }
    }

    // We only require the button to exist
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON)
        .assertExists()
        // Use semantics directly instead of performClick()
        .performSemanticsAction(SemanticsActions.OnClick)

    // Wait until Compose is idle and then check the callback
    composeTestRule.runOnIdle {
      assert(callbackCalled)
      // Default values since we didn't touch the stars
      assert(receivedTutorStars == 0)
      assert(receivedListingStars == 0)
    }
  }
}
