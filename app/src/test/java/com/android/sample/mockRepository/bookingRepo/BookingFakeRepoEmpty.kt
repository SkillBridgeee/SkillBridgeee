package com.android.sample.mockRepository.bookingRepo

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus

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
