package com.android.sample.model.booking

import java.util.Date
import org.junit.Assert.*
import org.junit.Test

class BookingTest {

  @Test
  fun `test Booking creation with valid values`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000) // 1 hour later

    val booking =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "tutor789",
            bookerId = "user012",
            sessionStart = startTime,
            sessionEnd = endTime,
            status = BookingStatus.CONFIRMED,
            price = 50.0)

    // Also test that validate() passes for a valid booking
    booking.validate()

    assertEquals("booking123", booking.bookingId)
    assertEquals("listing456", booking.associatedListingId)
    assertEquals("tutor789", booking.listingCreatorId)
    assertEquals("user012", booking.bookerId)
    assertEquals(startTime, booking.sessionStart)
    assertEquals(endTime, booking.sessionEnd)
    assertEquals(BookingStatus.CONFIRMED, booking.status)
    assertEquals(50.0, booking.price, 0.01)
  }

  @Test
  fun `test Booking validation - session end before session start`() {
    val startTime = Date()
    val endTime = Date(startTime.time - 1000) // 1 second before start

    val booking =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "tutor789",
            bookerId = "user012",
            sessionStart = startTime,
            sessionEnd = endTime)

    assertThrows(IllegalArgumentException::class.java) { booking.validate() }
  }

  @Test
  fun `test Booking validation - session start equals session end`() {
    val time = Date()

    val booking =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "tutor789",
            bookerId = "user012",
            sessionStart = time,
            sessionEnd = time)

    assertThrows(IllegalArgumentException::class.java) { booking.validate() }
  }

  @Test
  fun `test Booking validation - tutor and user are same`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    val booking =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "user123",
            bookerId = "user123",
            sessionStart = startTime,
            sessionEnd = endTime)

    assertThrows(IllegalArgumentException::class.java) { booking.validate() }
  }

  @Test
  fun `test Booking validation - negative price`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    val booking =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "tutor789",
            bookerId = "user012",
            sessionStart = startTime,
            sessionEnd = endTime,
            price = -10.0)

    assertThrows(IllegalArgumentException::class.java) { booking.validate() }
  }

  @Test
  fun `test Booking with all valid statuses`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    BookingStatus.values().forEach { status ->
      val booking =
          Booking(
              bookingId = "booking123",
              associatedListingId = "listing456",
              listingCreatorId = "tutor789",
              bookerId = "user012",
              sessionStart = startTime,
              sessionEnd = endTime,
              status = status)

      assertEquals(status, booking.status)
    }
  }

  @Test
  fun `test Booking equality and hashCode`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    val booking1 =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "tutor789",
            bookerId = "user012",
            sessionStart = startTime,
            sessionEnd = endTime,
            status = BookingStatus.CONFIRMED,
            price = 75.0)

    val booking2 =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "tutor789",
            bookerId = "user012",
            sessionStart = startTime,
            sessionEnd = endTime,
            status = BookingStatus.CONFIRMED,
            price = 75.0)

    assertEquals(booking1, booking2)
    assertEquals(booking1.hashCode(), booking2.hashCode())
  }

  @Test
  fun `test Booking copy functionality`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    val originalBooking =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "tutor789",
            bookerId = "user012",
            sessionStart = startTime,
            sessionEnd = endTime,
            status = BookingStatus.PENDING,
            price = 50.0)

    val updatedBooking = originalBooking.copy(status = BookingStatus.COMPLETED, price = 60.0)

    assertEquals("booking123", updatedBooking.bookingId)
    assertEquals("listing456", updatedBooking.associatedListingId)
    assertEquals(BookingStatus.COMPLETED, updatedBooking.status)
    assertEquals(60.0, updatedBooking.price, 0.01)

    assertNotEquals(originalBooking, updatedBooking)
  }

  @Test
  fun `test BookingStatus enum values`() {
    assertEquals(4, BookingStatus.values().size)
    assertTrue(BookingStatus.values().contains(BookingStatus.PENDING))
    assertTrue(BookingStatus.values().contains(BookingStatus.CONFIRMED))
    assertTrue(BookingStatus.values().contains(BookingStatus.COMPLETED))
    assertTrue(BookingStatus.values().contains(BookingStatus.CANCELLED))
  }

  @Test
  fun `test Booking toString contains relevant information`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    val booking =
        Booking(
            bookingId = "booking123",
            associatedListingId = "listing456",
            listingCreatorId = "tutor789",
            bookerId = "user012",
            sessionStart = startTime,
            sessionEnd = endTime,
            status = BookingStatus.CONFIRMED,
            price = 50.0)

    val bookingString = booking.toString()
    assertTrue(bookingString.contains("booking123"))
    assertTrue(bookingString.contains("listing456"))
    assertTrue(bookingString.contains("tutor789"))
    assertTrue(bookingString.contains("user012"))
  }
}
