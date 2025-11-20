package com.android.sample.utils.fakeRepo.fakeBooking

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import java.util.UUID

/**
 * A lightweight in-memory implementation of FakeBookingRepo.
 *
 * This repository keeps bookings in a simple mutable list and provides minimal CRUD operations
 * without any persistence, networking, or validation logic.
 *
 * It contains no predefined booking data—only what is added at runtime—making it suitable for UI
 * previews, isolated component tests, or local development scenarios.
 */
class FakeBookingEmpty : FakeBookingRepo {

  private val bookings = mutableListOf<Booking>()

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
    return bookings.filter { booking -> booking.bookerId == userId }
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

  override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
    val booking = bookings.find { it.bookingId == bookingId } ?: return
    val updated = booking.copy(status = status)
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
}
