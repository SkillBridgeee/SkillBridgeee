package com.android.sample.ui.bookings

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MyBookingsViewModelInitTest {

  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun aBooking(): Booking {
    val start = Date()
    val end = Date(start.time + 1) // ensure end > start to satisfy Booking invariant
    return Booking(
        bookingId = "b1",
        associatedListingId = "l1",
        listingCreatorId = "t1",
        bookerId = "s1",
        sessionStart = start,
        sessionEnd = end,
        status = BookingStatus.CONFIRMED,
        price = 10.0)
  }

  @Test
  fun init_async_success_populates_items() {
    val repo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = listOf(aBooking())

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = aBooking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }

    val vm = MyBookingsViewModel(repo, "s1")
    dispatcher.scheduler.advanceUntilIdle()
    assertEquals(1, vm.items.value.size)
  }

  @Test
  fun init_async_failure_sets_empty_list() {
    val repo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = throw RuntimeException("boom")

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = aBooking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }

    val vm = MyBookingsViewModel(repo, "s1")
    dispatcher.scheduler.advanceUntilIdle()
    assertEquals(0, vm.items.value.size)
  }

  @Test
  fun init_blocking_success_and_failure_paths() {
    // success
    val okRepo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = listOf(aBooking())

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = aBooking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }
    val okVm = MyBookingsViewModel(okRepo, "s1", initialLoadBlocking = true)
    assertEquals(1, okVm.items.value.size)

    // failure
    val badRepo =
        object : BookingRepository {
          override fun getNewUid() = "x"

          override suspend fun getBookingsByUserId(userId: String) = throw RuntimeException("boom")

          override suspend fun getAllBookings() = emptyList<Booking>()

          override suspend fun getBooking(bookingId: String) = aBooking()

          override suspend fun getBookingsByTutor(tutorId: String) = emptyList<Booking>()

          override suspend fun getBookingsByStudent(studentId: String) = emptyList<Booking>()

          override suspend fun getBookingsByListing(listingId: String) = emptyList<Booking>()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }
    val badVm = MyBookingsViewModel(badRepo, "s1", initialLoadBlocking = true)
    assertEquals(0, badVm.items.value.size)
  }
}
