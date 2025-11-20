package com.android.sample.screen

import com.android.sample.mockRepository.bookingRepo.BookingFakeRepoError
import com.android.sample.mockRepository.bookingRepo.BookingFakeRepoWorking
import com.android.sample.mockRepository.listingRepo.ListingFakeRepoError
import com.android.sample.mockRepository.listingRepo.ListingFakeRepoWorking
import com.android.sample.mockRepository.profileRepo.ProfileFakeRepoError
import com.android.sample.mockRepository.profileRepo.ProfileFakeRepoWorking
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Proposal
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.rating.RatingType
import com.android.sample.model.rating.StarRating
import com.android.sample.model.user.Profile
import com.android.sample.ui.bookings.BookingDetailsViewModel
import com.android.sample.ui.bookings.BookingUIState
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BookingsDetailsViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var bookingRepoWorking: BookingFakeRepoWorking
  private lateinit var errorBookingRepo: BookingFakeRepoError

  private lateinit var listingRepoWorking: ListingFakeRepoWorking
  private lateinit var errorListingRepo: ListingFakeRepoError

  private lateinit var profileRepoWorking: ProfileFakeRepoWorking

  private lateinit var errorProfileRepo: ProfileFakeRepoError

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    bookingRepoWorking = BookingFakeRepoWorking()
    errorBookingRepo = BookingFakeRepoError()

    listingRepoWorking = ListingFakeRepoWorking()
    errorListingRepo = ListingFakeRepoError()

    profileRepoWorking = ProfileFakeRepoWorking()
    errorProfileRepo = ProfileFakeRepoError()

    RatingRepositoryProvider.setForTests(fakeRatingRepository())
  }

  class FakeRatingRepositoryImpl : RatingRepository {
    val addedRatings = mutableListOf<Rating>()
    private val store = ConcurrentHashMap<String, Rating>()

    override fun getNewUid(): String = UUID.randomUUID().toString()

    override suspend fun hasRating(
        fromUserId: String,
        toUserId: String,
        ratingType: RatingType,
        targetObjectId: String
    ): Boolean {
      // For these tests we can just say "no duplicate yet"
      // or actually check in the local store if you prefer.
      return store.values.any {
        it.fromUserId == fromUserId &&
            it.toUserId == toUserId &&
            it.ratingType == ratingType &&
            it.targetObjectId == targetObjectId
      }
    }

    override suspend fun getAllRatings(): List<Rating> = store.values.toList()

    override suspend fun getRating(ratingId: String): Rating? = store[ratingId]

    override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> =
        store.values.filter { it.fromUserId == fromUserId }

    override suspend fun getRatingsByToUser(toUserId: String): List<Rating> =
        store.values.filter { it.toUserId == toUserId }

    override suspend fun getRatingsOfListing(listingId: String): List<Rating> =
        store.values.filter { it.targetObjectId == listingId }

    override suspend fun addRating(rating: Rating) {
      store[rating.ratingId] = rating
      addedRatings.add(rating)
    }

    override suspend fun updateRating(ratingId: String, rating: Rating) {
      if (store.containsKey(ratingId)) store[ratingId] = rating
    }

    override suspend fun deleteRating(ratingId: String) {
      store.remove(ratingId)
      addedRatings.removeIf { it.ratingId == ratingId }
    }

    override suspend fun getTutorRatingsOfUser(userId: String): List<Rating> =
        store.values.filter { it.ratingType == RatingType.TUTOR && it.toUserId == userId }

    override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> =
        store.values.filter { it.ratingType == RatingType.STUDENT && it.toUserId == userId }
  }

  // Replace the previous factory with one that returns the concrete fake so setup can still call
  // it.
  fun fakeRatingRepository(): FakeRatingRepositoryImpl = FakeRatingRepositoryImpl()

  @OptIn(ExperimentalCoroutinesApi::class)
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  /** --- Scénario 1 : Chargement réussi --- * */
  @Test
  fun loadBooking_success_updatesUiStateCorrectly() = runTest {
    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepoWorking,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.bookingUiState.value
    assertFalse(state.loadError)
    assertEquals("b1", state.booking.bookingId)
    assertEquals("creator_1", state.creatorProfile.userId)
    assertEquals("Tutor proposal", state.listing.description)
  }

  /** --- Scénario 2 : Erreur pendant le chargement --- * */
  @Test
  fun loadBooking_error_booking_setsLoadErrorTrue() = runTest {
    val vm =
        BookingDetailsViewModel(
            bookingRepository = errorBookingRepo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.bookingUiState.value
    assertTrue(state.loadError)
  }

  @Test
  fun loadBooking_error_listing_setsLoadErrorTrue() = runTest {
    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepoWorking,
            listingRepository = errorListingRepo,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.bookingUiState.value
    assertTrue(state.loadError)
  }

  @Test
  fun loadBooking_error_profile_setsLoadErrorTrue() = runTest {
    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepoWorking,
            listingRepository = listingRepoWorking,
            profileRepository = errorProfileRepo)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val state = vm.bookingUiState.value
    assertTrue(state.loadError)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markBookingAsCompleted_updatesStatusToCompleted() = runTest {
    val repo =
        object : BookingRepository {
          var booking =
              Booking(
                  bookingId = "b1",
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED)

          override fun getNewUid(): String = "unused"

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBooking(bookingId: String): Booking? =
              booking.takeIf { it.bookingId == bookingId }

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(
              bookingId: String,
              status: BookingStatus,
          ) {
            if (bookingId == booking.bookingId) booking = booking.copy(status = status)
          }

          override suspend fun confirmBooking(bookingId: String) {
            updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
          }

          override suspend fun completeBooking(bookingId: String) {
            updateBookingStatus(bookingId, BookingStatus.COMPLETED)
          }

          override suspend fun cancelBooking(bookingId: String) {
            updateBookingStatus(bookingId, BookingStatus.CANCELLED)
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(BookingStatus.CONFIRMED, vm.bookingUiState.value.booking.status)

    vm.markBookingAsCompleted()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(BookingStatus.COMPLETED, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markBookingAsCompleted_whenRepoThrows_doesNotChangeStatus() = runTest {
    val repo =
        object : BookingRepository {
          val booking =
              Booking(
                  bookingId = "b1",
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED)

          override fun getNewUid(): String = "unused"

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBooking(bookingId: String): Booking? =
              booking.takeIf { it.bookingId == bookingId }

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(
              bookingId: String,
              status: BookingStatus,
          ) {
            /* not used */
          }

          override suspend fun confirmBooking(bookingId: String) {
            /* not used */
          }

          override suspend fun completeBooking(bookingId: String) {
            throw RuntimeException("boom")
          }

          override suspend fun cancelBooking(bookingId: String) {
            /* not used */
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()
    val before = vm.bookingUiState.value.booking.status
    assertEquals(BookingStatus.CONFIRMED, before)

    vm.markBookingAsCompleted()
    testDispatcher.scheduler.advanceUntilIdle()
    val after = vm.bookingUiState.value.booking.status

    assertEquals(before, after)
  }

  @Test
  fun submitStudentRatings_whenCompleted_sendsTwoRatings() = runTest {
    val fakeRatingRepo = FakeRatingRepositoryImpl()

    val booking =
        Booking(
            bookingId = "b1",
            associatedListingId = "l1",
            listingCreatorId = "tutor-1",
            bookerId = "student-1",
            status = BookingStatus.COMPLETED,
        )

    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepoWorking,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking,
            ratingRepository = fakeRatingRepo,
        )

    vm.setUiStateForTest(
        BookingUIState(
            booking = booking,
            listing = Proposal(),
            creatorProfile = Profile(),
            loadError = false,
        ))

    testDispatcher.scheduler.advanceUntilIdle()
    vm.submitStudentRatings(tutorStars = 4, listingStars = 2)
    testDispatcher.scheduler.advanceUntilIdle()

    assert(fakeRatingRepo.addedRatings.size == 2)

    val tutorRating = fakeRatingRepo.addedRatings.first { it.ratingType == RatingType.TUTOR }
    val listingRating = fakeRatingRepo.addedRatings.first { it.ratingType == RatingType.LISTING }

    assert(tutorRating.starRating == StarRating.FOUR)
    assert(listingRating.starRating == StarRating.TWO)

    assert(tutorRating.fromUserId == "student-1")
    assert(tutorRating.toUserId == "tutor-1")
    assert(tutorRating.targetObjectId == "tutor-1")

    assert(listingRating.fromUserId == "student-1")
    assert(listingRating.toUserId == "tutor-1")
    assert(listingRating.targetObjectId == "l1")
  }

  @Test
  fun submitStudentRatings_whenNotCompleted_doesNothing() = runTest {
    val fakeRatingRepo = FakeRatingRepositoryImpl()

    val booking =
        Booking(
            bookingId = "b2",
            associatedListingId = "l2",
            listingCreatorId = "tutor-2",
            bookerId = "student-2",
            status = BookingStatus.CONFIRMED,
        )

    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepoWorking,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking,
            ratingRepository = fakeRatingRepo,
        )

    vm.setUiStateForTest(
        BookingUIState(
            booking = booking,
            listing = Proposal(),
            creatorProfile = Profile(),
            loadError = false,
        ))

    testDispatcher.scheduler.advanceUntilIdle()
    vm.submitStudentRatings(5, 5)
    testDispatcher.scheduler.advanceUntilIdle()

    assert(fakeRatingRepo.addedRatings.isEmpty())
  }

  @Test
  fun submitStudentRatings_whenEmptyBookingId_doesNothing() = runTest {
    val fakeRatingRepo = FakeRatingRepositoryImpl()

    val booking =
        Booking(
            bookingId = "",
            associatedListingId = "l3",
            listingCreatorId = "tutor-3",
            bookerId = "student-3",
            status = BookingStatus.COMPLETED,
        )

    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepoWorking,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking,
            ratingRepository = fakeRatingRepo,
        )

    vm.setUiStateForTest(
        BookingUIState(
            booking = booking,
            listing = Proposal(),
            creatorProfile = Profile(),
            loadError = false,
        ))

    testDispatcher.scheduler.advanceUntilIdle()
    vm.submitStudentRatings(3, 3)
    testDispatcher.scheduler.advanceUntilIdle()

    assert(fakeRatingRepo.addedRatings.isEmpty())
  }
}
