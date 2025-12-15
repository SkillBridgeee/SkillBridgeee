package com.android.sample.utils.fakeRepo.fakeBooking

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.PaymentStatus
import java.util.Date
import java.util.UUID

/**
 * A fake implementation of [com.android.sample.model.booking.BookingRepository] that provides a
 * predefined set of bookings.
 *
 * This mock repository is used for testing and development purposes, simulating a repository with
 * actual booking data without requiring a real backend.
 *
 * Features:
 * - Contains two initial bookings with different statuses (CONFIRMED and PENDING).
 * - Supports all repository operations such as add, update, delete, and status changes.
 * - Returns copies of the internal list to prevent external mutation.
 *
 * Typical use cases:
 * - Verifying ViewModel or UseCase logic when bookings exist.
 * - Testing UI rendering of booking lists with different statuses.
 * - Simulating user actions like confirming, completing, or cancelling bookings.
 */
class FakeBookingWorking : FakeBookingRepo {

  val initialNumBooking = 3

  private val bookings =
      mutableListOf(
          Booking(
              bookingId = "b1",
              associatedListingId = "listing_1",
              listingCreatorId = "creator_1",
              bookerId = "creator_2",
              sessionStart = Date(System.currentTimeMillis() + 3600000L),
              sessionEnd = Date(System.currentTimeMillis() + 7200000L),
              status = BookingStatus.CONFIRMED,
              price = 30.0),
          Booking(
              bookingId = "b2",
              associatedListingId = "listing_2",
              listingCreatorId = "creator_2",
              bookerId = "creator_1",
              sessionStart = Date(System.currentTimeMillis() + 10800000L),
              sessionEnd = Date(System.currentTimeMillis() + 14400000L),
              status = BookingStatus.PENDING,
              price = 45.0),
          Booking(
              bookingId = "b3",
              associatedListingId = "listing_1",
              listingCreatorId = "creator_1",
              bookerId = "creator_2",
              sessionStart = Date(System.currentTimeMillis() + 18000000L),
              sessionEnd = Date(System.currentTimeMillis() + 21600000L),
              status = BookingStatus.PENDING,
              price = 30.0))

  // --- Génération simple d'ID ---
  override fun getNewUid(): String {
    return "booking_${UUID.randomUUID()}"
  }

  override suspend fun getAllBookings(): List<Booking> {
    return bookings.toList()
  }

  override suspend fun getBooking(bookingId: String): Booking? {
    return bookings.find { booking -> booking.bookingId == bookingId }
  }

  override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
    return bookings.filter { booking -> booking.listingCreatorId == tutorId }
  }

  override suspend fun getBookingsByUserId(userId: String): List<Booking> {
    return bookings.filter { booking ->
      booking.bookerId == userId || booking.listingCreatorId == userId
    }
  }

  override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
    return bookings.filter { booking -> booking.listingCreatorId == studentId }
  }

  override suspend fun getBookingsByListing(listingId: String): List<Booking> {
    return bookings.filter { booking -> booking.associatedListingId == listingId }
  }

  override suspend fun addBooking(booking: Booking) {
    bookings.add(booking)
  }

  override suspend fun updateBooking(bookingId: String, booking: Booking) {
    val index = bookings.indexOfFirst { it.bookingId == bookingId }
    if (index != -1) {
      bookings[index] = booking.copy(bookingId = bookingId)
    }
  }

  override suspend fun deleteBooking(bookingId: String) {
    bookings.removeAll { it.bookingId == bookingId }
  }

  override suspend fun deleteAllBookingOfUser(userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
    val booking = bookings.find { it.bookingId == bookingId } ?: return
    val updated = booking.copy(status = status)
    updateBooking(bookingId, updated)
  }

  override suspend fun updatePaymentStatus(bookingId: String, paymentStatus: PaymentStatus) {
    val booking = bookings.find { it.bookingId == bookingId } ?: return
    val updated = booking.copy(paymentStatus = paymentStatus)
    updateBooking(bookingId, updated)
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
    if (userA.isBlank() || userB.isBlank()) return false

    return bookings.any { booking ->
      val participantsMatch =
          (booking.bookerId == userA && booking.listingCreatorId == userB) ||
              (booking.bookerId == userB && booking.listingCreatorId == userA)

      participantsMatch && booking.status == BookingStatus.CONFIRMED
    }
  }
}
