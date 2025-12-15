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
import com.android.sample.model.listing.ListingType
import com.android.sample.model.listing.Proposal
import com.android.sample.model.map.Location
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.rating.RatingType
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.ui.bookings.BookingDetailsViewModel
import com.android.sample.ui.bookings.BookingUIState
import com.android.sample.ui.bookings.RatingProgress
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

    override suspend fun deleteAllRatingOfUser(userId: String) {
      TODO("Not yet implemented")
    }
  }

  private class CapturingProfileRepo : ProfileRepository {

    override fun getNewUid() = "u1"

    override fun getCurrentUserId() = "u1"

    override suspend fun getProfile(userId: String): Profile =
        Profile(userId = userId, name = "User", email = "x@y.com")

    override suspend fun getProfileById(userId: String): Profile = getProfile(userId)

    override suspend fun addProfile(profile: Profile) {}

    override suspend fun updateProfile(userId: String, profile: Profile) {}

    override suspend fun deleteProfile(userId: String) {}

    override suspend fun getAllProfiles(): List<Profile> = emptyList()

    override suspend fun searchProfilesByLocation(
        location: Location,
        radiusKm: Double
    ): List<Profile> = emptyList()

    override suspend fun getSkillsForUser(userId: String): List<Skill> = emptyList()

    var lastTutorUserId: String? = null
    var lastTutorAverage: Double? = null
    var lastTutorTotal: Int? = null

    var lastStudentUserId: String? = null
    var lastStudentAverage: Double? = null
    var lastStudentTotal: Int? = null

    override suspend fun updateTutorRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      lastTutorUserId = userId
      lastTutorAverage = averageRating
      lastTutorTotal = totalRatings
    }

    override suspend fun updateStudentRatingFields(
        userId: String,
        averageRating: Double,
        totalRatings: Int
    ) {
      lastStudentUserId = userId
      lastStudentAverage = averageRating
      lastStudentTotal = totalRatings
    }
  }

  private fun newVmForCreatorRating(
      ratingRepo: RatingRepository,
      profileRepo: ProfileRepository = profileRepoWorking,
      bookingRepo: BookingRepository = bookingRepoWorking,
      listingRepo: com.android.sample.model.listing.ListingRepository = listingRepoWorking,
  ): BookingDetailsViewModel {
    return BookingDetailsViewModel(
        bookingRepository = bookingRepo,
        listingRepository = listingRepo,
        profileRepository = profileRepo,
        ratingRepository = ratingRepo,
    )
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

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(
              bookingId: String,
              status: BookingStatus,
          ) {
            if (bookingId == booking.bookingId) booking = booking.copy(status = status)
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            // Not used in this test
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

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
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

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(
              bookingId: String,
              status: BookingStatus,
          ) {
            /* not used */
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
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

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
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
  fun submitBookerRatings_whenCompleted_andProposal_sendsTutorAndListingRatings() = runTest {
    val fakeRatingRepo = FakeRatingRepositoryImpl()

    val booking =
        Booking(
            bookingId = "b1",
            associatedListingId = "l1",
            listingCreatorId = "tutor-1",
            bookerId = "student-1",
            status = BookingStatus.COMPLETED,
        )

    val listing =
        Proposal(
            listingId = "l1",
            creatorUserId = "tutor-1",
            // if Proposal defaults type already, fine; otherwise:
            type = ListingType.PROPOSAL,
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
            listing = listing,
            creatorProfile = Profile(userId = "tutor-1"),
            bookerProfile = Profile(userId = "student-1"),
            loadError = false,
            ratingProgress = RatingProgress(),
        ))

    // Run the launched coroutine in submitBookerRatings
    vm.submitBookerRatings(userStars = 4, listingStars = 2, userComment = "", listingComment = "")

    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(2, fakeRatingRepo.addedRatings.size)

    val userRating = fakeRatingRepo.addedRatings.first { it.ratingType == RatingType.TUTOR }
    val listingRating = fakeRatingRepo.addedRatings.first { it.ratingType == RatingType.LISTING }

    assertEquals("student-1", userRating.fromUserId)
    assertEquals("tutor-1", userRating.toUserId)
    assertEquals("b1", userRating.targetObjectId) // bookingId

    assertEquals("student-1", listingRating.fromUserId)
    assertEquals("tutor-1", listingRating.toUserId) // listing.creatorUserId
    assertEquals("l1", listingRating.targetObjectId) // listingId (IMPORTANT)
  }

  @Test
  fun submitBookerRatings_whenCompleted_andRequest_sendsStudentAndListingRatings() = runTest {
    val fakeRatingRepo = FakeRatingRepositoryImpl()

    val booking =
        Booking(
            bookingId = "b1",
            associatedListingId = "l1",
            listingCreatorId = "student-1", // creator is student for REQUEST
            bookerId = "tutor-1", // booker is tutor for REQUEST
            status = BookingStatus.COMPLETED,
        )

    val listing =
        Proposal(
            listingId = "l1",
            creatorUserId = "student-1",
            type = ListingType.REQUEST,
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
            listing = listing,
            creatorProfile = Profile(userId = "student-1"),
            bookerProfile = Profile(userId = "tutor-1"),
            loadError = false,
            ratingProgress = RatingProgress(),
        ))

    vm.submitBookerRatings(userStars = 4, listingStars = 2, userComment = "", listingComment = "")

    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(2, fakeRatingRepo.addedRatings.size)

    val userRating = fakeRatingRepo.addedRatings.first { it.ratingType == RatingType.STUDENT }
    val listingRating = fakeRatingRepo.addedRatings.first { it.ratingType == RatingType.LISTING }

    // booker is tutor-1
    assertEquals("tutor-1", userRating.fromUserId)
    // in REQUEST, booker rates the STUDENT (student-1)
    assertEquals("student-1", userRating.toUserId)
    assertEquals("b1", userRating.targetObjectId)

    assertEquals("tutor-1", listingRating.fromUserId)
    assertEquals("student-1", listingRating.toUserId)
    assertEquals("l1", listingRating.targetObjectId)
  }

  @Test
  fun submitBookerRatings_whenNotCompleted_doesNothing() = runTest {
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
            ratingProgress = RatingProgress(),
        ))

    testDispatcher.scheduler.advanceUntilIdle()
    vm.submitBookerRatings(userStars = 5, listingStars = 5, userComment = "", listingComment = "")
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(fakeRatingRepo.addedRatings.isEmpty())
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
    vm.submitBookerRatings(userStars = 3, listingStars = 3, userComment = "", listingComment = "")
    testDispatcher.scheduler.advanceUntilIdle()

    assert(fakeRatingRepo.addedRatings.isEmpty())
  }

  // ==================== PAYMENT STATUS TESTS ====================

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markPaymentComplete_updatesPaymentStatusToPayed() = runTest {
    val repo =
        object : BookingRepository {
          var booking =
              Booking(
                  bookingId = "b1",
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED,
                  paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT)

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

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            if (bookingId == booking.bookingId)
                booking = booking.copy(paymentStatus = paymentStatus)
          }

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(
        com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT,
        vm.bookingUiState.value.booking.paymentStatus)

    vm.markPaymentComplete()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(
        com.android.sample.model.booking.PaymentStatus.PAID,
        vm.bookingUiState.value.booking.paymentStatus)
    assertFalse(vm.bookingUiState.value.loadError)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markPaymentComplete_whenRepoThrows_setsLoadError() = runTest {
    val repo =
        object : BookingRepository {
          val booking =
              Booking(
                  bookingId = "b1",
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED,
                  paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT)

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

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            throw RuntimeException("Payment update failed")
          }

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()
    val beforeStatus = vm.bookingUiState.value.booking.paymentStatus

    vm.markPaymentComplete()
    testDispatcher.scheduler.advanceUntilIdle()

    // Payment status should not change
    assertEquals(beforeStatus, vm.bookingUiState.value.booking.paymentStatus)
    assertTrue(vm.bookingUiState.value.loadError)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markPaymentComplete_whenEmptyBookingId_doesNothing() = runTest {
    var updatePaymentCalled = false
    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "unused"

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBooking(bookingId: String): Booking? = null

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            updatePaymentCalled = true
          }

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    // Set UI state with empty booking ID
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "",
                    associatedListingId = "listing_1",
                    listingCreatorId = "creator_1",
                    bookerId = "student_1")))

    vm.markPaymentComplete()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify that updatePaymentStatus was never called since booking ID is empty
    assertFalse(updatePaymentCalled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun confirmPaymentReceived_updatesPaymentStatusToConfirmed() = runTest {
    val repo =
        object : BookingRepository {
          var booking =
              Booking(
                  bookingId = "b1",
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED,
                  paymentStatus = com.android.sample.model.booking.PaymentStatus.PAID)

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

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            if (bookingId == booking.bookingId)
                booking = booking.copy(paymentStatus = paymentStatus)
          }

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()
    assertEquals(
        com.android.sample.model.booking.PaymentStatus.PAID,
        vm.bookingUiState.value.booking.paymentStatus)

    vm.confirmPaymentReceived()
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(
        com.android.sample.model.booking.PaymentStatus.CONFIRMED,
        vm.bookingUiState.value.booking.paymentStatus)
    assertFalse(vm.bookingUiState.value.loadError)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun confirmPaymentReceived_whenRepoThrows_setsLoadError() = runTest {
    val repo =
        object : BookingRepository {
          val booking =
              Booking(
                  bookingId = "b1",
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED,
                  paymentStatus = com.android.sample.model.booking.PaymentStatus.PAID)

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

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            throw RuntimeException("Confirm payment failed")
          }

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()
    val beforeStatus = vm.bookingUiState.value.booking.paymentStatus

    vm.confirmPaymentReceived()
    testDispatcher.scheduler.advanceUntilIdle()

    // Payment status should not change
    assertEquals(beforeStatus, vm.bookingUiState.value.booking.paymentStatus)
    assertTrue(vm.bookingUiState.value.loadError)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun confirmPaymentReceived_whenEmptyBookingId_doesNothing() = runTest {
    var updatePaymentCalled = false
    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "unused"

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBooking(bookingId: String): Booking? = null

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            updatePaymentCalled = true
          }

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    // Set UI state with empty booking ID
    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "",
                    associatedListingId = "listing_1",
                    listingCreatorId = "creator_1",
                    bookerId = "student_1")))

    vm.confirmPaymentReceived()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify that updatePaymentStatus was never called since booking ID is empty
    assertFalse(updatePaymentCalled)
  }

  // ===== TESTS FOR LINES 282-308 OF BookingDetailsViewModel.kt =====

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun acceptBooking_updatesStatusToConfirmed() = runTest {
    // Test lines 282-292: acceptBooking success case
    var updateStatusCalled = false
    var statusUsed: BookingStatus? = null

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED)

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
            updateStatusCalled = true
            statusUsed = status
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    // Trigger acceptBooking via onAcceptBooking callback
    vm.bookingUiState.value.onAcceptBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify updateBookingStatus was called with CONFIRMED
    assertTrue(updateStatusCalled)
    assertEquals(BookingStatus.CONFIRMED, statusUsed)
    assertEquals(BookingStatus.CONFIRMED, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun acceptBooking_handlesError() = runTest {
    // Test lines 289-291: acceptBooking error handling
    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.PENDING)

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
            throw RuntimeException("Network error")
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val initialStatus = vm.bookingUiState.value.booking.status

    // Trigger acceptBooking - should catch exception
    vm.bookingUiState.value.onAcceptBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // State should not be updated on error (remains PENDING)
    assertEquals(initialStatus, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun acceptBooking_handlesNullBookingResponse() = runTest {
    // Test lines 286-288: acceptBooking when getBooking returns null
    var callCount = 0

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking? {
            callCount++
            return if (callCount == 1) {
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.PENDING)
            } else {
              null // Return null on second call
            }
          }

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val originalStatus = vm.bookingUiState.value.booking.status

    // Trigger accept
    vm.bookingUiState.value.onAcceptBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // State should not be updated when getBooking returns null
    assertEquals(originalStatus, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun denyBooking_updatesStatusToCancelled() = runTest {
    // Test lines 294-304: denyBooking success case
    var updateStatusCalled = false
    var statusUsed: BookingStatus? = null

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CANCELLED)

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
            updateStatusCalled = true
            statusUsed = status
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    // Trigger denyBooking via onDenyBooking callback
    vm.bookingUiState.value.onDenyBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify updateBookingStatus was called with CANCELLED
    assertTrue(updateStatusCalled)
    assertEquals(BookingStatus.CANCELLED, statusUsed)
    assertEquals(BookingStatus.CANCELLED, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun denyBooking_handlesError() = runTest {
    // Test lines 301-303: denyBooking error handling
    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.PENDING)

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
            throw RuntimeException("Database error")
          }

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val initialStatus = vm.bookingUiState.value.booking.status

    // Trigger denyBooking - should catch exception
    vm.bookingUiState.value.onDenyBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // State should not be updated on error
    assertEquals(initialStatus, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun denyBooking_handlesNullBookingResponse() = runTest {
    // Test lines 298-300: denyBooking when getBooking returns null
    var callCount = 0

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking? {
            callCount++
            return if (callCount == 1) {
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.PENDING)
            } else {
              null // Return null on second call
            }
          }

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val originalStatus = vm.bookingUiState.value.booking.status

    // Trigger deny
    vm.bookingUiState.value.onDenyBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // State should not be updated when getBooking returns null
    assertEquals(originalStatus, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun acceptBooking_fetchesUpdatedBookingFromRepository() = runTest {
    // Test line 286: Verify acceptBooking fetches updated booking after status change
    var getBookingCallCount = 0

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking {
            getBookingCallCount++
            return Booking(
                bookingId = bookingId,
                associatedListingId = "listing_1",
                listingCreatorId = "creator_1",
                bookerId = "student_1",
                status =
                    if (getBookingCallCount > 1) BookingStatus.CONFIRMED else BookingStatus.PENDING)
          }

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val initialCallCount = getBookingCallCount
    assertEquals(BookingStatus.PENDING, vm.bookingUiState.value.booking.status)

    // Trigger accept
    vm.bookingUiState.value.onAcceptBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // getBooking should have been called again
    assertTrue(getBookingCallCount > initialCallCount)
    assertEquals(BookingStatus.CONFIRMED, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun denyBooking_fetchesUpdatedBookingFromRepository() = runTest {
    // Test line 298: Verify denyBooking fetches updated booking after status change
    var getBookingCallCount = 0

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking {
            getBookingCallCount++
            return Booking(
                bookingId = bookingId,
                associatedListingId = "listing_1",
                listingCreatorId = "creator_1",
                bookerId = "student_1",
                status =
                    if (getBookingCallCount > 1) BookingStatus.CANCELLED else BookingStatus.PENDING)
          }

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val initialCallCount = getBookingCallCount
    assertEquals(BookingStatus.PENDING, vm.bookingUiState.value.booking.status)

    // Trigger deny
    vm.bookingUiState.value.onDenyBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // getBooking should have been called again
    assertTrue(getBookingCallCount > initialCallCount)
    assertEquals(BookingStatus.CANCELLED, vm.bookingUiState.value.booking.status)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markPaymentComplete_ignoresBlankBookingId() = runTest {
    // Test lines 306-308: markPaymentComplete early return when bookingId is blank
    var updateCalled = false

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking? = null

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            updateCalled = true
          }

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    // Set state with blank bookingId
    vm.setUiStateForTest(
        BookingUIState(
            booking = Booking(bookingId = ""), // blank ID
            listing = Proposal(),
            creatorProfile = Profile()))

    // Try to mark payment complete
    vm.markPaymentComplete()
    testDispatcher.scheduler.advanceUntilIdle()

    // Repository method should NOT have been called
    assertFalse(updateCalled)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markPaymentComplete_handlesError() = runTest {
    // Test error handling in markPaymentComplete
    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1")

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {
            throw RuntimeException("Payment processing error")
          }

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(bookingId = "b1", associatedListingId = "l1", listingCreatorId = "u1")))

    vm.markPaymentComplete()
    testDispatcher.scheduler.advanceUntilIdle()

    // Should set loadError to true on exception
    assertTrue(vm.bookingUiState.value.loadError)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markPaymentComplete_handlesNullBookingResponse() = runTest {
    // Test markPaymentComplete when getBooking returns null after payment update
    var callCount = 0

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking? {
            callCount++
            return if (callCount == 1) {
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "l1",
                  listingCreatorId = "u1",
                  bookerId = "student",
                  paymentStatus = com.android.sample.model.booking.PaymentStatus.PENDING_PAYMENT)
            } else {
              null // Return null after payment update
            }
          }

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    val originalPaymentStatus = vm.bookingUiState.value.booking.paymentStatus

    vm.markPaymentComplete()
    testDispatcher.scheduler.advanceUntilIdle()

    // State should not be updated when getBooking returns null
    assertEquals(originalPaymentStatus, vm.bookingUiState.value.booking.paymentStatus)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun acceptBooking_updatesUiStateWithCorrectBookingCopy() = runTest {
    // Test line 288: Verify UI state is updated with correct booking data
    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED,
                  price = 100.0)

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    // Trigger accept
    vm.bookingUiState.value.onAcceptBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify state was updated with complete booking data
    assertEquals(BookingStatus.CONFIRMED, vm.bookingUiState.value.booking.status)
    assertEquals("b1", vm.bookingUiState.value.booking.bookingId)
    assertEquals("listing_1", vm.bookingUiState.value.booking.associatedListingId)
    assertEquals(100.0, vm.bookingUiState.value.booking.price, 0.01)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun denyBooking_updatesUiStateWithCorrectBookingCopy() = runTest {
    // Test line 300: Verify UI state is updated with correct booking data
    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "b1"

          override suspend fun getBooking(bookingId: String): Booking =
              Booking(
                  bookingId = bookingId,
                  associatedListingId = "listing_1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CANCELLED,
                  price = 100.0)

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun deleteAllBookingOfUser(userId: String) {
            TODO("Not yet implemented")
          }

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun updatePaymentStatus(
              bookingId: String,
              paymentStatus: com.android.sample.model.booking.PaymentStatus
          ) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}

          override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
            TODO("Not yet implemented")
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = repo,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    vm.load("b1")
    testDispatcher.scheduler.advanceUntilIdle()

    // Trigger deny
    vm.bookingUiState.value.onDenyBooking()
    testDispatcher.scheduler.advanceUntilIdle()

    // Verify state was updated with complete booking data
    assertEquals(BookingStatus.CANCELLED, vm.bookingUiState.value.booking.status)
    assertEquals("b1", vm.bookingUiState.value.booking.bookingId)
    assertEquals("listing_1", vm.bookingUiState.value.booking.associatedListingId)
    assertEquals(100.0, vm.bookingUiState.value.booking.price, 0.01)
  }

  @Test
  fun submitCreatorRating_ignoresIfNotCompleted() = runTest {
    val ratingRepo = FakeRatingRepositoryImpl()
    val vm = newVmForCreatorRating(ratingRepo)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "creator_1",
                    bookerId = "student_1",
                    status = BookingStatus.CONFIRMED, // not COMPLETED
                ),
            listing =
                Proposal(
                    listingId = "l1", creatorUserId = "creator_1", type = ListingType.PROPOSAL),
            ratingProgress = RatingProgress(),
        ))

    vm.submitCreatorRating(4, "")
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(ratingRepo.addedRatings.isEmpty())
  }

  @Test
  fun submitCreatorRating_invalidStars_setsLoadError() = runTest {
    val ratingRepo = FakeRatingRepositoryImpl()
    val vm = newVmForCreatorRating(ratingRepo)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "creator_1",
                    bookerId = "student_1",
                    status = BookingStatus.COMPLETED,
                ),
            listing =
                Proposal(
                    listingId = "l1", creatorUserId = "creator_1", type = ListingType.PROPOSAL),
            ratingProgress = RatingProgress(),
            isCreator = true, // <<< REQUIRED
        ))

    vm.submitCreatorRating(0, "")
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.bookingUiState.value.loadError)
    assertTrue(ratingRepo.addedRatings.isEmpty())
  }

  @Test
  fun submitCreatorRating_proposal_createsStudentRating() = runTest {
    val ratingRepo = FakeRatingRepositoryImpl()
    val vm = newVmForCreatorRating(ratingRepo)

    val booking =
        Booking(
            bookingId = "b1",
            associatedListingId = "l1",
            listingCreatorId = "tutor-1",
            bookerId = "student-1",
            status = BookingStatus.COMPLETED,
        )

    vm.setUiStateForTest(
        BookingUIState(
            booking = booking,
            listing =
                Proposal(listingId = "l1", creatorUserId = "tutor-1", type = ListingType.PROPOSAL),
            ratingProgress = RatingProgress(),
            isCreator = true, // <<< REQUIRED
        ))

    vm.submitCreatorRating(5, "")
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(1, ratingRepo.addedRatings.size)
    val r = ratingRepo.addedRatings.single()

    assertEquals("tutor-1", r.fromUserId)
    assertEquals("student-1", r.toUserId)
    assertEquals(RatingType.STUDENT, r.ratingType)
    assertEquals("b1", r.targetObjectId)
  }

  @Test
  fun submitCreatorRating_whenRatingRepoThrows_setsLoadError() = runTest {
    val explodingRatingRepo =
        object : RatingRepository by FakeRatingRepositoryImpl() {
          override suspend fun addRating(rating: Rating) {
            throw IllegalStateException("boom")
          }
        }

    val vm = newVmForCreatorRating(explodingRatingRepo)

    vm.setUiStateForTest(
        BookingUIState(
            booking =
                Booking(
                    bookingId = "b1",
                    associatedListingId = "l1",
                    listingCreatorId = "tutor-1",
                    bookerId = "student-1",
                    status = BookingStatus.COMPLETED,
                ),
            listing =
                Proposal(listingId = "l1", creatorUserId = "tutor-1", type = ListingType.PROPOSAL),
            ratingProgress = RatingProgress(),
            isCreator = true, // <<< REQUIRED
        ))

    vm.submitCreatorRating(5, "")
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(vm.bookingUiState.value.loadError)
  }

  @Test
  fun submitBookerRatings_withComments_savesCommentsCorrectly() = runTest {
    val fakeRatingRepo = FakeRatingRepositoryImpl()
    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepoWorking,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking,
            ratingRepository = fakeRatingRepo,
        )

    val booking =
        Booking(
            bookingId = "b1",
            associatedListingId = "l1",
            listingCreatorId = "tutor-1",
            bookerId = "student-1",
            status = BookingStatus.COMPLETED,
        )

    val listing = Proposal(listingId = "l1", creatorUserId = "tutor-1")

    vm.setUiStateForTest(
        BookingUIState(
            booking = booking,
            listing = listing,
            creatorProfile = Profile(userId = "tutor-1"),
            bookerProfile = Profile(userId = "student-1"),
            loadError = false,
            ratingProgress = RatingProgress(),
        ))

    testDispatcher.scheduler.advanceUntilIdle()

    vm.submitBookerRatings(
        userStars = 4,
        listingStars = 5,
        userComment = "Excellent tutor",
        listingComment = "Great listing")

    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(2, fakeRatingRepo.addedRatings.size)

    val userRating = fakeRatingRepo.addedRatings.find { it.ratingType == RatingType.TUTOR }
    assertEquals("Excellent tutor", userRating?.comment)

    val listingRating = fakeRatingRepo.addedRatings.find { it.ratingType == RatingType.LISTING }
    assertEquals("Great listing", listingRating?.comment)
  }
}
