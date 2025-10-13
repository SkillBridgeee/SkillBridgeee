package com.android.sample.ui.bookings

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.FakeBookingRepository
import java.util.Date
import kotlin.collections.get
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
  fun dates_are_ddMMyyyy() {
    val pattern = Regex("""\d{2}/\d{2}/\d{4}""")
    val items = MyBookingsViewModel(FakeBookingRepository(), "s1", initialLoadBlocking = true).items.value
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

  // kotlin
  @Test
  fun refresh_produces_correct_duration_labels_for_various_lengths() = runTest {
    val now = java.util.Date()
    fun bookingWith(msOffset: Long, id: String, tutorId: String) =
        Booking(
            bookingId = id,
            associatedListingId = "l",
            listingCreatorId = tutorId,
            bookerId = "u1",
            sessionStart = now,
            sessionEnd = java.util.Date(now.time + msOffset),
            status = BookingStatus.CONFIRMED,
            price = 10.0)

    val oneHour = bookingWith(60 * 60 * 1000, "b1", "t1")
    val twoHours = bookingWith(2 * 60 * 60 * 1000, "b2", "t2")
    val oneHourThirty = bookingWith(90 * 60 * 1000, "b3", "t3")

    val repo =
        object : BookingRepository {
          override fun getNewUid(): String = "u"

          override suspend fun getAllBookings(): List<Booking> = listOf()

          override suspend fun getBooking(bookingId: String): Booking = oneHour

          override suspend fun getBookingsByTutor(tutorId: String): List<Booking> = listOf()

          override suspend fun getBookingsByUserId(userId: String): List<Booking> =
              listOf(oneHour, twoHours, oneHourThirty)

          override suspend fun getBookingsByStudent(studentId: String): List<Booking> = listOf()

          override suspend fun getBookingsByListing(listingId: String): List<Booking> = listOf()

          override suspend fun addBooking(booking: Booking) {}

          override suspend fun updateBooking(bookingId: String, booking: Booking) {}

          override suspend fun deleteBooking(bookingId: String) {}

          override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {}

          override suspend fun confirmBooking(bookingId: String) {}

          override suspend fun completeBooking(bookingId: String) {}

          override suspend fun cancelBooking(bookingId: String) {}
        }

    val vm = MyBookingsViewModel(repo, "u1")
    vm.refresh()
    testDispatcher.scheduler.advanceUntilIdle()

    val items = vm.items.value
    assertEquals(3, items.size)
    assertEquals("1hr", items[0].durationLabel)
    assertEquals("2hrs", items[1].durationLabel)
    assertEquals("1h 30m", items[2].durationLabel)
  }
}
