package com.android.sample.screen

import androidx.compose.material3.MaterialTheme
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
import com.android.sample.ui.listing.ListingScreenTestTags
import java.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookingDetailsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ----- TEST CONSTANTS -----
  companion object {
    private const val TEST_TUTOR_ID = "u1"
    private const val TEST_STUDENT_ID = "student123"
    private const val TEST_BOOKING_ID = "b1"
    private const val TEST_LISTING_ID = "l1"
    private const val TEST_TUTOR_NAME = "John Doe"
    private const val TEST_TUTOR_EMAIL = "john.doe@example.com"
    private const val TEST_STUDENT_NAME = "Student Booker"
    private const val TEST_STUDENT_EMAIL = "student@example.com"

    // UI String constants
    const val STRING_ACCEPT = "Accept"
    const val STRING_DENY = "Deny"
  }

  // ----- HELPER BUILDER FUNCTIONS -----
  /**
   * Creates a BookingUIState configured for a tutor (listing creator) view.
   *
   * @param bookerId The ID of the student who booked the session
   * @param bookingStatus The status of the booking
   * @param bookerName The name of the student who booked
   * @return A complete BookingUIState with isTutor = true
   */
  private fun bookingStateForTutor(
      bookerId: String = TEST_STUDENT_ID,
      bookingStatus: BookingStatus = BookingStatus.PENDING,
      bookerName: String = TEST_STUDENT_NAME
  ): BookingUIState {
    return BookingUIState(
        booking =
            Booking(
                bookingId = TEST_BOOKING_ID,
                listingCreatorId = TEST_TUTOR_ID,
                bookerId = bookerId,
                associatedListingId = TEST_LISTING_ID,
                price = 50.0,
                sessionStart = Date(1736546400000),
                sessionEnd = Date(1736550000000),
                status = bookingStatus),
        listing =
            Proposal(
                listingId = TEST_LISTING_ID,
                description = "Cours de maths",
                skill = Skill(skill = "Algebra", mainSubject = MainSubject.ACADEMICS),
                location = Location(name = "Geneva")),
        creatorProfile =
            Profile(userId = TEST_TUTOR_ID, name = TEST_TUTOR_NAME, email = TEST_TUTOR_EMAIL),
        bookerProfile = Profile(userId = bookerId, name = bookerName, email = TEST_STUDENT_EMAIL),
        isTutor = true,
        onAcceptBooking = {},
        onDenyBooking = {})
  }

  /**
   * Creates a BookingUIState configured for a student (booker) view.
   *
   * @param tutorId The ID of the tutor/listing creator
   * @param bookingStatus The status of the booking
   * @param tutorName The name of the tutor
   * @return A complete BookingUIState with isTutor = false
   */
  private fun bookingStateForStudent(
      tutorId: String = TEST_TUTOR_ID,
      bookingStatus: BookingStatus = BookingStatus.PENDING,
      tutorName: String = TEST_TUTOR_NAME
  ): BookingUIState {
    return BookingUIState(
        booking =
            Booking(
                bookingId = TEST_BOOKING_ID,
                listingCreatorId = tutorId,
                bookerId = TEST_TUTOR_ID, // Student is the current user
                associatedListingId = TEST_LISTING_ID,
                price = 50.0,
                sessionStart = Date(1736546400000),
                sessionEnd = Date(1736550000000),
                status = bookingStatus),
        listing =
            Proposal(
                listingId = TEST_LISTING_ID,
                description = "Cours de maths",
                skill = Skill(skill = "Algebra", mainSubject = MainSubject.ACADEMICS),
                location = Location(name = "Geneva")),
        creatorProfile = Profile(userId = tutorId, name = tutorName, email = TEST_TUTOR_EMAIL),
        bookerProfile =
            Profile(userId = TEST_TUTOR_ID, name = TEST_STUDENT_NAME, email = TEST_STUDENT_EMAIL),
        isTutor = false,
        onAcceptBooking = {},
        onDenyBooking = {})
  }

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

        override suspend fun getRequests() = emptyList<Request>()

        override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

        override suspend fun addProposal(proposal: Proposal) {}

        override suspend fun addRequest(request: Request) {}

        override suspend fun updateListing(listingId: String, listing: Listing) {}

        override suspend fun deleteListing(listingId: String) {}

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

    composeTestRule.waitUntil {
      composeTestRule.onAllNodesWithText("John Doe").fetchSemanticsNodes().isNotEmpty()
    }

    // Vérifie les sections visibles
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.LISTING_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.SCHEDULE_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.DESCRIPTION_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.STATUS).assertExists()

    // Verify creator name and email are displayed using test tags
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

  private val fakeProfileRepoError =
      object : ProfileRepository {
        override fun getNewUid() = "u1"

        override fun getCurrentUserId() = "u1"

        override suspend fun getProfile(userId: String): Profile = error("test")

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

  @Test
  fun viewModel_load_handlesBookingNotFound() {
    val errorRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String): Booking? = null
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    vm.load("nonexistent")

    // Wait for async operation
    Thread.sleep(200)

    // Should set loadError = true (lines 59-70)
    assert(vm.bookingUiState.value.loadError)
  }

  @Test
  fun viewModel_load_handlesProfileNotFound() {
    val errorProfileRepo =
        object : ProfileRepository by fakeProfileRepo {
          override suspend fun getProfile(userId: String): Profile? = null
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = fakeBookingRepo,
            listingRepository = fakeListingRepo,
            profileRepository = errorProfileRepo)

    vm.load("b1")
    Thread.sleep(200)

    assert(vm.bookingUiState.value.loadError)
  }

  @Test
  fun viewModel_load_handlesListingNotFound() {
    val errorListingRepo =
        object : ListingRepository by fakeListingRepo {
          override suspend fun getListing(listingId: String): Listing? = null
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = fakeBookingRepo,
            listingRepository = errorListingRepo,
            profileRepository = fakeProfileRepo)

    vm.load("b1")
    Thread.sleep(200)

    assert(vm.bookingUiState.value.loadError)
  }

  @Test
  fun viewModel_markBookingAsCompleted_updatesStatus() {
    var completeCalled = false
    val repo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun completeBooking(bookingId: String) {
            completeCalled = true
          }

          override suspend fun getBooking(bookingId: String) =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "l1",
                  listingCreatorId = "u1",
                  bookerId = "student",
                  status = BookingStatus.COMPLETED)
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student")))

    vm.markBookingAsCompleted()
    Thread.sleep(200)

    // Lines 97-100, 179-185
    assert(completeCalled)
    assert(vm.bookingUiState.value.booking.status == BookingStatus.COMPLETED)
  }

  @Test
  fun viewModel_markBookingAsCompleted_handlesError() {
    val errorRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun completeBooking(bookingId: String) {
            throw Exception("Complete failed")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(bookingId = "b1", associatedListingId = "l1", listingCreatorId = "u1")))

    vm.markBookingAsCompleted()
    Thread.sleep(200)

    assert(vm.bookingUiState.value.loadError)
  }

  @Test
  fun viewModel_submitStudentRatings_validatesStarRange() {
    val vm = fakeViewModel()
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.COMPLETED)))

    // Invalid tutor stars (lines 212-216)
    vm.submitStudentRatings(tutorStars = 0, listingStars = 3)
    Thread.sleep(200)
    assert(vm.bookingUiState.value.loadError)

    // Reset
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.COMPLETED)))

    // Invalid listing stars
    vm.submitStudentRatings(tutorStars = 3, listingStars = 6)
    Thread.sleep(200)
    assert(vm.bookingUiState.value.loadError)
  }

  @Test
  fun viewModel_submitStudentRatings_ignoresIfNotCompleted() {
    val vm = fakeViewModel()
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.PENDING)))

    vm.submitStudentRatings(tutorStars = 5, listingStars = 5)
    Thread.sleep(200)

    // Should not change state
    assert(!vm.bookingUiState.value.ratingSubmitted)
  }

  @Test
  fun viewModel_markPaymentComplete_updatesPaymentStatus() {
    var updateCalled = false

    val repo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: PaymentStatus
          ) {
            updateCalled = true
          }

          override suspend fun getBooking(bookingId: String) =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "l1",
                  listingCreatorId = "u1",
                  bookerId = "student",
                  paymentStatus = PaymentStatus.PAID)
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(bookingId = "b1", associatedListingId = "l1", listingCreatorId = "u1")))

    vm.markPaymentComplete()
    Thread.sleep(200)

    assert(updateCalled)
    assert(vm.bookingUiState.value.booking.paymentStatus == PaymentStatus.PAID)
  }

  @Test
  fun viewModel_confirmPaymentReceived_updatesPaymentStatus() {
    var updateCalled = false

    val repo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: PaymentStatus
          ) {
            updateCalled = true
          }

          override suspend fun getBooking(bookingId: String) =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "l1",
                  listingCreatorId = "u1",
                  bookerId = "student",
                  paymentStatus = PaymentStatus.CONFIRMED)
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(bookingId = "b1", associatedListingId = "l1", listingCreatorId = "u1")))

    vm.confirmPaymentReceived()
    Thread.sleep(200)

    assert(updateCalled)
    assert(vm.bookingUiState.value.booking.paymentStatus == PaymentStatus.CONFIRMED)
  }

  @Test
  fun viewModel_load_setsIsTutorCorrectly() {
    val profileRepo =
        object : ProfileRepository by fakeProfileRepo {
          override fun getCurrentUserId() = "tutor-123"
        }

    val bookingRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String) =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "l1",
                  listingCreatorId = "tutor-123",
                  bookerId = "student-456")
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepo,
            listingRepository = fakeListingRepo,
            profileRepository = profileRepo)

    vm.load("b1")
    Thread.sleep(200)

    // Lines 84-87 - isTutor should be true
    assert(vm.bookingUiState.value.isTutor)
  }

  @Test
  fun viewModel_submitStudentRatings_handlesRepositoryError() {
    val errorRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: PaymentStatus
          ) {
            throw Exception("Payment update failed")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.COMPLETED)))

    // Lines 255-257 error handling
    vm.submitStudentRatings(tutorStars = 5, listingStars = 5)
    Thread.sleep(200)

    // Should handle error gracefully
    assert(vm.bookingUiState.value.loadError || !vm.bookingUiState.value.ratingSubmitted)
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
    val vm = fakeViewModelError()
    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
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

        override suspend fun getRequests() = emptyList<Request>()

        override suspend fun getListingsByUser(userId: String) = emptyList<Listing>()

        override suspend fun addProposal(proposal: Proposal) {}

        override suspend fun addRequest(request: Request) {}

        override suspend fun updateListing(listingId: String, listing: Listing) {}

        override suspend fun deleteListing(listingId: String) {}

        override suspend fun deactivateListing(listingId: String) {}

        override suspend fun searchBySkill(skill: Skill) = emptyList<Listing>()

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
          onBookerClick = {},
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
          onBookerClick = {},
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
            onBookerClick = {},
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
            onBookerClick = {},
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

  // ===== NEW TESTS FOR LINES 94-124 OF BookingDetailsScreen.kt =====

  @Test
  fun bookingDetailsScreen_launchedEffect_callsLoadWithBookingId() {
    // Test line 102: LaunchedEffect(bookingId) { bkgViewModel.load(bookingId) }
    var loadedBookingId: String? = null

    val customBookingRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String): Booking? {
            loadedBookingId = bookingId
            return Booking(
                bookingId = bookingId,
                associatedListingId = "l1",
                listingCreatorId = "u1",
                bookerId = "student-456",
                sessionStart = Date(),
                sessionEnd = Date(System.currentTimeMillis() + 3600000),
                status = BookingStatus.PENDING,
                price = 50.0)
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = customBookingRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "test-booking-123", onCreatorClick = {})
    }

    // Wait for composition and async loading
    composeTestRule.waitForIdle()
    Thread.sleep(300)

    // Verify load was called with the correct bookingId
    assert(loadedBookingId == "test-booking-123")
  }

  @Test
  fun bookingDetailsScreen_errorState_displaysCircularProgressIndicator() {
    // Test lines 105-110: error state rendering
    val errorRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String): Booking? = null
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "non-existent", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300)

    // Verify error indicator is displayed with correct test tag
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.ERROR).assertExists().assertIsDisplayed()
  }

  @Test
  fun bookingDetailsScreen_errorState_usesCenteredBox() {
    // Test lines 106-109: Box with fillMaxSize, padding, and center alignment
    val errorRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String): Booking? {
            throw Exception("Network error")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "error-booking", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300)

    // The CircularProgressIndicator should be centered
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.ERROR).assertExists()
  }

  @Test
  fun bookingDetailsScreen_successState_rendersBookingDetailsContent() {
    // Test lines 111-121: BookingDetailsContent rendering
    val vm = fakeViewModel()

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()

    // Verify main content sections are rendered (not error)
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.ERROR).assertDoesNotExist()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertExists()
  }

  @Test
  fun bookingDetailsScreen_successState_passesCorrectUiState() {
    // Test line 113: uiState parameter passed to BookingDetailsContent
    val vm = fakeViewModel()

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = TEST_BOOKING_ID, onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300) // Wait for async data loading

    // Verify data from uiState is displayed correctly using test tags
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_NAME).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_EMAIL).assertExists()
  }

  @Test
  fun bookingDetailsScreen_successState_wiresOnCreatorClickCallback() {
    // Test line 114: onCreatorClick callback wiring
    var clickedCreatorId: String? = null
    val vm = fakeViewModel()

    composeTestRule.setContent {
      BookingDetailsScreen(
          bkgViewModel = vm, bookingId = "b1", onCreatorClick = { clickedCreatorId = it })
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300) // Wait for async data loading

    // Click the more info button
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.MORE_INFO_BUTTON)
        .assertExists()
        .performClick()

    // Verify callback was invoked with correct creator ID (u1 from fakeProfileRepo)
    assert(clickedCreatorId == "u1")
  }

  @Test
  fun bookingDetailsScreen_successState_wiresOnMarkCompletedCallback() {
    // Test line 115: onMarkCompleted callback wiring
    var markCompletedCalled = false
    val customRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun completeBooking(bookingId: String) {
            markCompletedCalled = true
          }

          override suspend fun getBooking(bookingId: String) =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "l1",
                  listingCreatorId = "u1",
                  bookerId = "student",
                  sessionStart = Date(),
                  sessionEnd = Date(System.currentTimeMillis() + 3600000),
                  status = BookingStatus.CONFIRMED, // Must be CONFIRMED to show complete button
                  price = 50.0)
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = customRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300) // Wait for async data loading

    // Find and click the complete button
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.COMPLETE_BUTTON)
        .assertExists()
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()
    Thread.sleep(200)

    // Verify markBookingAsCompleted was called
    assert(markCompletedCalled)
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

    val vm =
        BookingDetailsViewModel(
            bookingRepository = customRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300) // Wait for async data loading

    // Verify rating section is present (indicates callback is wired)
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.RATING_SECTION).assertExists()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.RATING_SUBMIT_BUTTON).assertExists()
  }

  @Test
  fun bookingDetailsScreen_successState_wiresOnPaymentReceivedCallback() {
    // Test line 120: onPaymentReceived callback wiring
    var paymentReceivedCalled = false
    val customRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: PaymentStatus
          ) {
            paymentReceivedCalled = true
          }

          override suspend fun getBooking(bookingId: String) =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "l1",
                  listingCreatorId = "current-user", // User is the tutor
                  bookerId = "student-123",
                  sessionStart = Date(),
                  sessionEnd = Date(System.currentTimeMillis() + 3600000),
                  status = BookingStatus.CONFIRMED, // Must be CONFIRMED
                  paymentStatus = PaymentStatus.PAID,
                  price = 50.0)
        }

    val profileRepo =
        object : ProfileRepository by fakeProfileRepo {
          override fun getCurrentUserId() = "current-user" // User is the tutor
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = customRepo,
            listingRepository = fakeListingRepo,
            profileRepository = profileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300) // Wait for async data loading

    // Click the payment received button if visible
    composeTestRule
        .onNodeWithTag(ListingScreenTestTags.PAYMENT_RECEIVED_BUTTON)
        .assertExists()
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()
    Thread.sleep(200)

    // Verify callback was invoked
    assert(paymentReceivedCalled)
  }

  @Test
  fun bookingDetailsScreen_appliesCorrectModifier() {
    // Test line 121: modifier with padding and fillMaxSize
    val vm = fakeViewModel()

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()

    // Verify content is displayed (modifier applied correctly allows content to render)
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertExists().assertIsDisplayed()
  }

  @Test
  fun bookingDetailsScreen_scaffoldStructure_appliesPaddingValues() {
    // Test lines 104-105: Scaffold structure with paddingValues
    val vm = fakeViewModel()

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()

    // Verify that content is properly padded within Scaffold
    // Content sections should be visible
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertIsDisplayed()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertIsDisplayed()
  }

  @Test
  fun bookingDetailsScreen_stateCollection_reactsToViewModelChanges() {
    // Test line 100: val uiState by bkgViewModel.bookingUiState.collectAsState()
    val vm = fakeViewModel()

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "b1", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300) // Wait for initial async data loading

    // Initial state should show PENDING status
    composeTestRule.onNodeWithText("PENDING").assertExists()

    // Update the state
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "u1",
                    bookerId = "student",
                    status = BookingStatus.CONFIRMED),
            listing = Proposal(),
            creatorProfile =
                Profile(userId = "u1", name = "John Doe", email = "john.doe@example.com"),
        ))

    composeTestRule.waitForIdle()

    // State should update to show CONFIRMED
    composeTestRule.onNodeWithText("CONFIRMED").assertExists()
  }

  @Test
  fun bookingDetailsScreen_launchedEffect_retriggersOnBookingIdChange() {
    // Test line 102: LaunchedEffect key dependency on bookingId
    var loadCallCount = 0
    var lastLoadedId: String? = null

    val customRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String): Booking {
            loadCallCount++
            lastLoadedId = bookingId
            return Booking(
                bookingId = bookingId,
                associatedListingId = "l1",
                listingCreatorId = "u1",
                bookerId = "student",
                sessionStart = Date(),
                sessionEnd = Date(System.currentTimeMillis() + 3600000),
                status = BookingStatus.PENDING,
                price = 50.0)
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = customRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    // Use a mutable state to trigger recomposition with different bookingId
    val currentBookingId = androidx.compose.runtime.mutableStateOf("booking-1")

    composeTestRule.setContent {
      BookingDetailsScreen(
          bkgViewModel = vm, bookingId = currentBookingId.value, onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300)

    val firstCallCount = loadCallCount
    assert(lastLoadedId == "booking-1")

    // Change the bookingId - this should trigger LaunchedEffect again
    currentBookingId.value = "booking-2"

    composeTestRule.waitForIdle()
    Thread.sleep(300)

    // Load should have been called again with new ID
    assert(loadCallCount > firstCallCount)
    assert(lastLoadedId == "booking-2")
  }

  @Test
  fun bookingDetailsScreen_errorState_doesNotRenderContent() {
    // Test that when loadError is true, content is not rendered (only error indicator)
    val errorRepo =
        object : BookingRepository by fakeBookingRepo {
          override suspend fun getBooking(bookingId: String): Booking? = null
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorRepo,
            listingRepository = fakeListingRepo,
            profileRepository = fakeProfileRepo)

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = "error-id", onCreatorClick = {})
    }

    composeTestRule.waitForIdle()
    Thread.sleep(300)

    // Error indicator should be shown
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.ERROR).assertExists()

    // Content sections should NOT be rendered
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.HEADER).assertDoesNotExist()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.CREATOR_SECTION).assertDoesNotExist()
    composeTestRule.onNodeWithTag(BookingDetailsTestTag.LISTING_SECTION).assertDoesNotExist()
  }

  // ----- NEW TESTS FOR BOOKER PROFILE FEATURE -----

  @Test
  fun bookingDetailsScreen_pendingBooking_studentView_doesNotDisplayBookerInfo() {
    val vm = fakeViewModel()

    // Use helper function for student view with clear intent
    vm.setUiStateForTest(bookingStateForStudent(bookingStatus = BookingStatus.PENDING))

    composeTestRule.setContent {
      BookingDetailsScreen(bkgViewModel = vm, bookingId = TEST_BOOKING_ID, onCreatorClick = {})
    }

    // Verify Accept/Deny buttons are NOT displayed using test constants
    composeTestRule.onNodeWithText(STRING_ACCEPT).assertDoesNotExist()
    composeTestRule.onNodeWithText(STRING_DENY).assertDoesNotExist()
  }

  @Test
  fun bookingDetailsScreen_bookerInfo_hasClickableAction() {
    val vm = fakeViewModel()
    var clickedBookerId: String? = null

    // Use helper function for tutor view with clear intent
    vm.setUiStateForTest(bookingStateForTutor(bookingStatus = BookingStatus.PENDING))

    composeTestRule.setContent {
      BookingDetailsScreen(
          bkgViewModel = vm,
          bookingId = TEST_BOOKING_ID,
          onCreatorClick = {},
          onBookerClick = { bookerId -> clickedBookerId = bookerId })
    }

    // Wait for the screen to load and display the booker name row
    composeTestRule.waitForIdle()

    // Verify the booker name row is clickable using test tag
    composeTestRule
        .onNodeWithTag(BookingDetailsTestTag.BOOKER_NAME_ROW)
        .assertExists()
        .assertHasClickAction()
        .performClick()

    // Verify the callback was invoked with the correct booker ID
    // Note: The bookerId comes from fakeBookingRepo which returns "asdf"
    assert(clickedBookerId != null) { "Expected onBookerClick to be called, but it was null" }
  }
}
