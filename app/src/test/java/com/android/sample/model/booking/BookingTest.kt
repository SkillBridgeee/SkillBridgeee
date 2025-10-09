package com.android.sample.model.booking

import java.util.Date
import org.junit.Assert.*
import org.junit.Test

class BookingTest {

  @Test
  fun `test Booking creation with default values`() {
    // This will fail validation because sessionStart equals sessionEnd
    try {
      val booking = Booking()
      fail("Should have thrown IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertTrue(e.message!!.contains("Session start time must be before session end time"))
    }
  }

  @Test
  fun `test Booking creation with valid values`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000) // 1 hour later

    val booking =
        Booking(
            bookingId = "booking123",
            tutorId = "tutor456",
            tutorName = "Dr. Smith",
            bookerId = "user789",
            bookerName = "John Doe",
            sessionStart = startTime,
            sessionEnd = endTime)

    assertEquals("booking123", booking.bookingId)
    assertEquals("tutor456", booking.tutorId)
    assertEquals("Dr. Smith", booking.tutorName)
    assertEquals("user789", booking.bookerId)
    assertEquals("John Doe", booking.bookerName)
    assertEquals(startTime, booking.sessionStart)
    assertEquals(endTime, booking.sessionEnd)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Booking validation - session end before session start`() {
    val startTime = Date()
    val endTime = Date(startTime.time - 1000) // 1 second before start

    Booking(
        bookingId = "booking123",
        tutorId = "tutor456",
        tutorName = "Dr. Smith",
        bookerId = "user789",
        bookerName = "John Doe",
        sessionStart = startTime,
        sessionEnd = endTime)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `test Booking validation - session start equals session end`() {
    val time = Date()

    Booking(
        bookingId = "booking123",
        tutorId = "tutor456",
        tutorName = "Dr. Smith",
        bookerId = "user789",
        bookerName = "John Doe",
        sessionStart = time,
        sessionEnd = time)
  }

  @Test
  fun `test Booking with valid time difference`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 1800000) // 30 minutes later

    val booking = Booking(sessionStart = startTime, sessionEnd = endTime)

    assertTrue(booking.sessionStart.before(booking.sessionEnd))
    assertEquals(1800000, booking.sessionEnd.time - booking.sessionStart.time)
  }

  @Test
  fun `test Booking equality and hashCode`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    val booking1 =
        Booking(
            bookingId = "booking123",
            tutorId = "tutor456",
            sessionStart = startTime,
            sessionEnd = endTime)

    val booking2 =
        Booking(
            bookingId = "booking123",
            tutorId = "tutor456",
            sessionStart = startTime,
            sessionEnd = endTime)

    assertEquals(booking1, booking2)
    assertEquals(booking1.hashCode(), booking2.hashCode())
  }

  @Test
  fun `test Booking copy functionality`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)
    val newEndTime = Date(startTime.time + 7200000) // 2 hours later

    val originalBooking =
        Booking(
            bookingId = "booking123",
            tutorId = "tutor456",
            tutorName = "Dr. Smith",
            sessionStart = startTime,
            sessionEnd = endTime)

    val updatedBooking = originalBooking.copy(tutorName = "Dr. Johnson", sessionEnd = newEndTime)

    assertEquals("booking123", updatedBooking.bookingId)
    assertEquals("tutor456", updatedBooking.tutorId)
    assertEquals("Dr. Johnson", updatedBooking.tutorName)
    assertEquals(startTime, updatedBooking.sessionStart)
    assertEquals(newEndTime, updatedBooking.sessionEnd)

    assertNotEquals(originalBooking, updatedBooking)
  }

  @Test
  fun `test Booking with empty string fields`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    val booking =
        Booking(
            bookingId = "",
            tutorId = "",
            tutorName = "",
            bookerId = "",
            bookerName = "",
            sessionStart = startTime,
            sessionEnd = endTime)

    assertEquals("", booking.bookingId)
    assertEquals("", booking.tutorId)
    assertEquals("", booking.tutorName)
    assertEquals("", booking.bookerId)
    assertEquals("", booking.bookerName)
  }

  @Test
  fun `test Booking toString contains relevant information`() {
    val startTime = Date()
    val endTime = Date(startTime.time + 3600000)

    val booking =
        Booking(
            bookingId = "booking123",
            tutorId = "tutor456",
            tutorName = "Dr. Smith",
            bookerId = "user789",
            bookerName = "John Doe",
            sessionStart = startTime,
            sessionEnd = endTime)

    val bookingString = booking.toString()
    assertTrue(bookingString.contains("booking123"))
    assertTrue(bookingString.contains("tutor456"))
    assertTrue(bookingString.contains("Dr. Smith"))
  }
}
