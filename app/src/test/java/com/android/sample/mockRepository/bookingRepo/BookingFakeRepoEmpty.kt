package com.android.sample.mockRepository.bookingRepo

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.PaymentStatus

/**
 * A fake implementation of [BookingRepository] that always returns empty data.
 *
 * This mock repository is used for testing scenarios where the user has no bookings or when the
 * backend/database contains no data.
 *
 * All "get" methods return empty lists or `null`. "write" operations such as add, update, or delete
 * are not implemented, as this repository is only meant for read-only empty state testing.
 *
 * Example use case:
 * - Verifying that the UI correctly displays an "empty bookings" message.
 * - Testing ViewModel logic when there are no bookings available.
 */
class BookingFakeRepoEmpty : BookingRepository {

  override fun getNewUid(): String {
    return ""
  }

  override suspend fun getAllBookings(): List<Booking> {
    return emptyList()
  }

  override suspend fun getBooking(bookingId: String): Booking? {
    return null
  }

  override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
    return emptyList()
  }

  override suspend fun getBookingsByUserId(userId: String): List<Booking> {
    return emptyList()
  }

  override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
    return emptyList()
  }

  override suspend fun getBookingsByListing(listingId: String): List<Booking> {
    return emptyList()
  }

  override suspend fun addBooking(booking: Booking) {
    TODO("Not yet implemented")
  }

  override suspend fun updateBooking(bookingId: String, booking: Booking) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteBooking(bookingId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteAllBookingOfUser(userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
    TODO("Not yet implemented")
  }

  override suspend fun updatePaymentStatus(bookingId: String, paymentStatus: PaymentStatus) {
    TODO("Not yet implemented")
  }

  override suspend fun confirmBooking(bookingId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun completeBooking(bookingId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun cancelBooking(bookingId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean {
    TODO("Not yet implemented")
  }
}
