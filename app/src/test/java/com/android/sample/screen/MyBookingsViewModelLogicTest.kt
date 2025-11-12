package com.android.sample.screen

import com.android.sample.mockRepository.bookingRepo.BookingFakeRepoEmpty
import com.android.sample.mockRepository.bookingRepo.BookingFakeRepoError
import com.android.sample.mockRepository.bookingRepo.BookingFakeRepoWorking
import com.android.sample.mockRepository.listingRepo.ListingFakeRepoError
import com.android.sample.mockRepository.listingRepo.ListingFakeRepoWorking
import com.android.sample.mockRepository.profileRepo.ProfileFakeRepoError
import com.android.sample.mockRepository.profileRepo.ProfileFakeRepoWorking
import com.android.sample.ui.bookings.MyBookingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyBookingsViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private lateinit var bookingRepoWorking: BookingFakeRepoWorking
  private lateinit var bookingRepoEmpty: BookingFakeRepoEmpty
  private lateinit var errorBookingRepo: BookingFakeRepoError

  private lateinit var listingRepoWorking: ListingFakeRepoWorking
  private lateinit var errorListingRepo: ListingFakeRepoError

  private lateinit var profileRepoWorking: ProfileFakeRepoWorking

  private lateinit var errorProfileRepo: ProfileFakeRepoError

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    bookingRepoWorking = BookingFakeRepoWorking()
    bookingRepoEmpty = BookingFakeRepoEmpty()
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

  // region --- Tests ---

  @Test
  fun `load() sets empty bookings when user has none`() = runTest {
    val viewModel =
        MyBookingsViewModel(
            bookingRepo = bookingRepoEmpty,
            listingRepo = listingRepoWorking,
            profileRepo = profileRepoWorking)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value

    assertFalse(state.isLoading)
    assertFalse(state.hasError)
    assertTrue(state.bookings.isEmpty())
  }

  @Test
  fun `load() builds correct BookingCardUI list`() = runTest {
    val viewModel =
        MyBookingsViewModel(
            bookingRepo = bookingRepoWorking,
            listingRepo = listingRepoWorking,
            profileRepo = profileRepoWorking)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value

    assertFalse(state.isLoading)
    assertFalse(state.hasError)
    assertEquals(bookingRepoWorking.initialNumBooking, state.bookings.size)

    // Vérification cohérente avec les données mockées
    val firstCard = state.bookings.first()
    val lastCard = state.bookings.last()

    assertNotNull(firstCard.listing)
    assertNotNull(firstCard.creatorProfile)
    assertTrue(
        firstCard.listing.description.contains("Tutor") ||
            firstCard.listing.description.contains("Student"))

    assertEquals("creator_1", firstCard.creatorProfile.userId)
    assertEquals("creator_2", lastCard.creatorProfile.userId)
  }

  @Test
  fun `load() sets error when booking repository throws exception`() = runTest {
    val viewModel =
        MyBookingsViewModel(
            bookingRepo = errorBookingRepo,
            listingRepo = listingRepoWorking,
            profileRepo = profileRepoWorking)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.hasError)
    assertFalse(state.isLoading)
    assertTrue(state.bookings.isEmpty())
  }

  @Test
  fun `load() sets error when listing repository throws exception`() = runTest {
    val viewModel =
        MyBookingsViewModel(
            bookingRepo = bookingRepoWorking,
            listingRepo = errorListingRepo,
            profileRepo = profileRepoWorking)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.hasError)
    assertFalse(state.isLoading)
    assertTrue(state.bookings.isEmpty())
  }

  @Test
  fun `load() sets error when profile repository throws exception`() = runTest {
    val viewModel =
        MyBookingsViewModel(
            bookingRepo = bookingRepoWorking,
            listingRepo = listingRepoWorking,
            profileRepo = errorProfileRepo)

    viewModel.load()
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state.hasError)
    assertFalse(state.isLoading)
    assertTrue(state.bookings.isEmpty())
  }

  // endregion
}
