package com.android.sample.utils.fakeRepo.fakeBooking

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingStatus
import java.util.UUID

class FakeBookingEmpty : FakeBookingRepo {

  private val bookings = mutableListOf<Booking>()

  override fun getNewUid(): String {
    return "booking_${UUID.randomUUID()}"
  }

  override suspend fun getAllBookings(): List<Booking> {
    return bookings
  }

  override suspend fun getBooking(bookingId: String): Booking? {
    return bookings.find { booking -> booking.bookingId == bookingId }
  }

  override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
    TODO("Not yet implemented")
  }

  override suspend fun getBookingsByUserId(userId: String): List<Booking> {
    return bookings.filter { booking -> booking.bookerId == userId }
  }

  override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
    TODO("Not yet implemented")
  }

  override suspend fun getBookingsByListing(listingId: String): List<Booking> {
    return bookings.filter { booking -> booking.associatedListingId == listingId }
  }

  override suspend fun addBooking(booking: Booking) {
    bookings.add(booking)
  }

  override suspend fun updateBooking(bookingId: String, booking: Booking) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteBooking(bookingId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) {
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
}
