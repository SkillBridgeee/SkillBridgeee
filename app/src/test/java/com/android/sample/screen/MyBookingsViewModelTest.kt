package com.android.sample.ui.bookings

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.FakeBookingRepository
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MyBookingsViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun demo_items_are_mapped_correctly() {
    val vm = MyBookingsViewModel(FakeBookingRepository(), "s1")
    val items = vm.items.value
    assertEquals(2, items.size)

    val first = items[0]
    assertEquals("Liam P.", first.tutorName)
    assertEquals("Piano Lessons", first.subject)
    assertEquals("$50/hr", first.pricePerHourLabel)
    assertEquals("2hrs", first.durationLabel)
    assertEquals(5, first.ratingStars)
    assertEquals(23, first.ratingCount)

    val second = items[1]
    assertEquals("Maria G.", second.tutorName)
    assertEquals("Calculus & Algebra", second.subject)
    assertEquals("$30/hr", second.pricePerHourLabel)
    assertEquals("1hr", second.durationLabel)
    assertEquals(4, second.ratingStars)
    assertEquals(41, second.ratingCount)
  }

  @Test
  fun dates_are_ddMMyyyy() {
    val pattern = Regex("""\d{2}/\d{2}/\d{4}""")
    val items = MyBookingsViewModel(FakeBookingRepository(), "s1").items.value
    assert(pattern.matches(items[0].dateLabel))
    assert(pattern.matches(items[1].dateLabel))
  }

  @Test
  fun refresh_maps_bookings_correctly() = runTest {
    // small repo that returns a single valid booking
    val start = Date()
    val end = Date(start.time + 90 * 60 * 1000) // +90 minutes
    val booking =
        Booking(
            bookingId = "b123",
            associatedListingId = "l1",
            listingCreatorId = "tutor1",
            bookerId = "student1",
            sessionStart = start,
            sessionEnd = end,
            status = BookingStatus.CONFIRMED,
            price = 100.0)

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "u1"

          override suspend fun getAllBookings(): List<Booking> = listOf(booking)

          override suspend fun getBooking(bookingId: String): Booking = booking

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = listOf(booking)

          override suspend fun getBookingsByUserId(userId: String): List<Booking> = listOf(booking)

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> =
              listOf(booking)

          override suspend fun getBookingsByListing(listingId: String): List<Booking> =
              listOf(booking)

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }

    val vm = MyBookingsViewModel(repo, "student1")
    vm.refresh()
    // advance dispatched coroutines
    testScheduler.advanceUntilIdle()

    val items = vm.items.value
    assertEquals(1, items.size)
    val mapped = items[0]
    assertEquals("b123", mapped.id)
    assertEquals("tutor1", mapped.tutorId)
    assertEquals("$100.0/hr", mapped.pricePerHourLabel)
    assertEquals("1h 30m", mapped.durationLabel)
    assertTrue(mapped.dateLabel.matches(Regex("""\d{2}/\d{2}/\d{4}""")))
    assertEquals(0, mapped.ratingStars)
    assertEquals(0, mapped.ratingCount)
  }
}
