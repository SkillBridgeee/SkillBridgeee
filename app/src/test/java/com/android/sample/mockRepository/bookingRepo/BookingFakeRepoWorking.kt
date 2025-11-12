package com.android.sample.mockRepository.bookingRepo

import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import java.util.*

class BookingFakeRepoWorking : BookingRepository {

  private val bookings =
      mutableListOf(
          Booking(
              bookingId = "b1",
              associatedListingId = "listing_1",
              listingCreatorId = "tutor_1",
              bookerId = "student_1",
              sessionStart = Date(System.currentTimeMillis() + 3600000L),
              sessionEnd = Date(System.currentTimeMillis() + 7200000L),
              status = BookingStatus.CONFIRMED,
              price = 30.0),
          Booking(
              bookingId = "b2",
              associatedListingId = "listing_2",
              listingCreatorId = "tutor_2",
              bookerId = "student_2",
              sessionStart = Date(System.currentTimeMillis() + 10800000L),
              sessionEnd = Date(System.currentTimeMillis() + 14400000L),
              status = BookingStatus.PENDING,
              price = 45.0))

  // --- Génération simple d'ID ---
  override fun getNewUid(): String {
    return "booking_${UUID.randomUUID()}"
  }

  // --- Récupérations ---
  override suspend fun getAllBookings(): List<Booking> {
    return bookings.toList()
  }

  override suspend fun getBooking(bookingId: String): Booking? {
    return bookings.find { it.bookingId == bookingId }
  }

  override suspend fun getBookingsByTutor(tutorId: String): List<Booking> {
    return bookings.filter { it.listingCreatorId == tutorId }
  }

  override suspend fun getBookingsByUserId(userId: String): List<Booking> {
    // Si un user peut être soit tuteur soit étudiant
    return bookings.filter { it.listingCreatorId == userId || it.bookerId == userId }
  }

  override suspend fun getBookingsByStudent(studentId: String): List<Booking> {
    return bookings.filter { it.bookerId == studentId }
  }

  override suspend fun getBookingsByListing(listingId: String): List<Booking> {
    return bookings.filter { it.associatedListingId == listingId }
  }

  // --- Mutations ---
  override suspend fun addBooking(booking: Booking) {
    bookings.add(booking.copy(bookingId = getNewUid()))
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
