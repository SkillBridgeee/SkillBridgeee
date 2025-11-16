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
import com.android.sample.ui.bookings.BookingDetailsViewModel
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
  }

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

  /** --- Scenario 3 : markBookingAsCompleted updates status to COMPLETED on success --- */
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markBookingAsCompleted_success_updatesStatusToCompleted() = runTest {
    // Local fake BookingRepository just for this test
    val bookingRepoForCompleteSuccess =
        object : BookingRepository {
          // Start with a CONFIRMED booking
          private var booking =
              Booking(
                  bookingId = "b-complete",
                  associatedListingId = "listing-1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED)

          override fun getNewUid(): String = "unused"

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBooking(bookingId: String): Booking? {
            return if (bookingId == booking.bookingId) booking else null
          }

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {
            // not needed in this test
          }

          override suspend fun updateBooking(bookingId: String, booking: Booking) {
            // not needed in this test
          }

          override suspend fun deleteBooking(bookingId: String) {
            // not needed in this test
          }

          override suspend fun updateBookingStatus(
              bookingId: String,
              status: BookingStatus,
          ) {
            if (bookingId == booking.bookingId) {
              booking = booking.copy(status = status)
            }
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
            bookingRepository = bookingRepoForCompleteSuccess,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    // Load existing booking
    vm.load("b-complete")
    testDispatcher.scheduler.advanceUntilIdle()

    // Sanity check: starts as CONFIRMED
    assertEquals(BookingStatus.CONFIRMED, vm.bookingUiState.value.booking.status)

    // When: student marks booking as completed
    vm.markBookingAsCompleted()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then: status is COMPLETED in the ViewModel state
    assertEquals(BookingStatus.COMPLETED, vm.bookingUiState.value.booking.status)
    assertFalse(vm.bookingUiState.value.loadError)
  }

  /** --- Scenario 4 : markBookingAsCompleted sets loadError on repository error --- */
  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun markBookingAsCompleted_error_setsLoadErrorTrue() = runTest {
    // Fake repo where getBooking works but completeBooking throws
    val bookingRepoForCompleteError =
        object : BookingRepository {
          private val booking =
              Booking(
                  bookingId = "b-error",
                  associatedListingId = "listing-1",
                  listingCreatorId = "creator_1",
                  bookerId = "student_1",
                  status = BookingStatus.CONFIRMED)

          override fun getNewUid(): String = "unused"

          override suspend fun getAllBookings(): List<Booking> = emptyList()

          override suspend fun getBooking(bookingId: String): Booking? {
            return if (bookingId == booking.bookingId) booking else null
          }

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = emptyList()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = emptyList()

          override suspend fun addBooking(booking: Booking) {
            // not needed
          }

          override suspend fun updateBooking(bookingId: String, booking: Booking) {
            // not needed
          }

          override suspend fun deleteBooking(bookingId: String) {
            // not needed
          }

          override suspend fun updateBookingStatus(
              bookingId: String,
              status: BookingStatus,
          ) {
            // not needed
          }

          override suspend fun confirmBooking(bookingId: String) {
            // not needed
          }

          override suspend fun completeBooking(bookingId: String) {
            throw RuntimeException("Simulated repository error in completeBooking")
          }

          override suspend fun cancelBooking(bookingId: String) {
            // not needed
          }
        }

    val vm =
        BookingDetailsViewModel(
            bookingRepository = bookingRepoForCompleteError,
            listingRepository = listingRepoWorking,
            profileRepository = profileRepoWorking)

    // First load succeeds
    vm.load("b-error")
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(vm.bookingUiState.value.loadError)
    assertEquals("b-error", vm.bookingUiState.value.booking.bookingId)

    // When: mark as completed (fake will throw)
    vm.markBookingAsCompleted()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then: ViewModel should report loadError = true
    assertTrue(vm.bookingUiState.value.loadError)
  }
}
