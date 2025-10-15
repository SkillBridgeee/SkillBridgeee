// kotlin
package com.android.sample.model.booking

import java.util.Calendar
import java.util.Date
import java.util.UUID
import kotlin.collections.MutableList

class FakeBookingRepository : BookingRepository {
  private val bookings: MutableList<Booking> = mutableListOf()

  init {
    // seed two bookings for booker "s1" (listingCreatorId holds a display name for tests)
    fun datePlus(days: Int, hours: Int = 0): Date {
      val c = Calendar.getInstance()
      c.add(Calendar.DAY_OF_MONTH, days)
      c.add(Calendar.HOUR_OF_DAY, hours)
      return c.time
    }

    bookings.add(
        Booking(
            bookingId = "b1",
            associatedListingId = "l1",
            listingCreatorId = "Liam P.", // treated as display name in tests
            bookerId = "s1",
            sessionStart = datePlus(1, 10),
            sessionEnd = datePlus(1, 12),
            price = 50.0))

    bookings.add(
        Booking(
            bookingId = "b2",
            associatedListingId = "l2",
            listingCreatorId = "Maria G.",
            bookerId = "s1",
            sessionStart = datePlus(5, 14),
            sessionEnd = datePlus(5, 15),
            price = 30.0))
  }

  override fun getNewUid(): String = UUID.randomUUID().toString()

  override suspend fun getAllBookings(): List<Booking> = bookings.toList()

  override suspend fun getBooking(bookingId: String): Booking =
      bookings.first { it.bookingId == bookingId }

  override suspend fun getBookingsByTutor(tutorId: String): List<Booking> =
      bookings.filter { it.listingCreatorId == tutorId }

  override suspend fun getBookingsByUserId(userId: String): List<Booking> {
    return listOf(
        Booking(
            bookingId = "b-1",
            associatedListingId = "listing-1",
            listingCreatorId = "tutor-1",
            bookerId = userId,
            sessionStart = Date(),
            sessionEnd = Date(System.currentTimeMillis() + 60 * 60 * 1000),
            price = 30.0),
        Booking(
            bookingId = "b-2",
            associatedListingId = "listing-2",
            listingCreatorId = "tutor-2",
            bookerId = userId,
            sessionStart = Date(),
            sessionEnd = Date(System.currentTimeMillis() + 90 * 60 * 1000),
            price = 25.0))
  }

  //            val now = Date()
  //            return listOf(
  //                BookingCardUi(
  //                    id = "demo-1",
  //                    tutorId = "tutor-1",
  //                    tutorName = "Alice Martin",
  //                    subject = "Guitar - Beginner",
  //                    pricePerHourLabel = "$30.0/hr",
  //                    durationLabel = "1hr",
  //                    dateLabel = dateFmt.format(now),
  //                    ratingStars = 5,
  //                    ratingCount = 12),
  //                BookingCardUi(
  //                    id = "demo-2",
  //                    tutorId = "tutor-2",
  //                    tutorName = "Lucas Dupont",
  //                    subject = "French Conversation",
  //                    pricePerHourLabel = "$25.0/hr",
  //                    durationLabel = "1h 30m",
  //                    dateLabel = dateFmt.format(now),
  //                    ratingStars = 4,
  //                    ratingCount = 8))

  override suspend fun getBookingsByStudent(studentId: String): List<Booking> =
      bookings.filter { it.bookerId == studentId }

  override suspend fun getBookingsByListing(listingId: String): List<Booking> =
      bookings.filter { it.associatedListingId == listingId }

  override suspend fun addBooking(booking: Booking) {
    bookings.add(booking)
  }

  override suspend fun updateBooking(bookingId: String, booking: Booking) {
    val idx = bookings.indexOfFirst { it.bookingId == bookingId }
    if (idx >= 0) bookings[idx] = booking else throw NoSuchElementException("booking not found")
  }

  override suspend fun deleteBooking(bookingId: String) {
    bookings.removeAll { it.bookingId == bookingId }
  }

  override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
    val idx = bookings.indexOfFirst { it.bookingId == bookingId }
    if (idx >= 0) bookings[idx] = bookings[idx].copy(status = status)
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
