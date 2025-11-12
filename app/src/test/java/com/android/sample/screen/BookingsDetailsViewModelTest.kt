package com.android.sample.screen

import com.android.sample.mockRepository.bookingRepo.BookingFakeRepoError
import com.android.sample.mockRepository.bookingRepo.BookingFakeRepoWorking
import com.android.sample.mockRepository.listingRepo.ListingFakeRepoError
import com.android.sample.mockRepository.listingRepo.ListingFakeRepoWorking
import com.android.sample.mockRepository.profileRepo.ProfileFakeRepoError
import com.android.sample.mockRepository.profileRepo.ProfileFakeRepoWorking
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

@OptIn(ExperimentalCoroutinesApi::class)
class BookingsDetailsViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var bookingRepoWorking: BookingFakeRepoWorking
  private lateinit var errorBookingRepo: BookingFakeRepoError

  private lateinit var listingRepoWorking: ListingFakeRepoWorking
  private lateinit var errorListingRepo: ListingFakeRepoError

  private lateinit var profileRepoWorking: ProfileFakeRepoWorking

  private lateinit var errorProfileRepo: ProfileFakeRepoError

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
}
