package com.android.sample.utils.fakeRepo.fakeBooking

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import java.io.IOException

/**
 * A fake implementation of FakeBookingRepo that intentionally simulates failures.
 *
 * Every method in this repository throws an exception, allowing developers to test error handling,
 * failure states, and UI resilience without interacting with real booking data or backend services.
 *
 * No bookings are stored, retrieved, or updated — all operations result in predictable mock errors
 * used for testing robustness.
 */
class FakeBookingError : FakeBookingRepo {
  override fun getNewUid(): String {
    throw IllegalStateException("Failed to generate UID (mock error).")
  }

  override suspend fun getAllBookings(): List<Booking> {
    throw IOException("Failed to load bookings (mock network error).")
  }

  override suspend fun getBooking(bookingId: String): Booking? {
    throw IOException("Booking not found (mock error) / Booking Id : $bookingId.")
  }

  override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
    throw IOException("Unable to fetch tutor bookings (mock error) / Tutor Id : $tutorId.")
  }

  override suspend fun getBookingsByUserId(userId: String): List<Booking> {
    throw IOException("Unable to fetch user bookings (mock error) / User Id : $userId.")
  }

  override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
    throw IOException("Unable to fetch student bookings (mock error) / Student Id : $studentId.")
  }

  override suspend fun getBookingsByListing(listingId: String): List<Booking> {
    throw IOException("Unable to fetch listing bookings (mock error) / Listing Id : $listingId.")
  }

  override suspend fun addBooking(booking: Booking) {
    throw IOException("Failed to add booking (mock error) / Booking Id : ${booking.bookingId}.")
  }

  override suspend fun updateBooking(bookingId: String, booking: Booking) {
    throw IOException("Failed to update booking (mock error).")
  }

  override suspend fun deleteBooking(bookingId: String) {
    throw IOException("Failed to delete booking (mock error).")
  }

  override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
    throw IOException("Failed to update booking status (mock error).")
  }

  override suspend fun confirmBooking(bookingId: String) {
    throw IOException("Failed to confirm booking (mock error).")
  }

  override suspend fun completeBooking(bookingId: String) {
    throw IOException("Failed to complete booking (mock error).")
  }

  override suspend fun cancelBooking(bookingId: String) {
    throw IOException("Failed to cancel booking (mock error).")
  }
}
