package com.android.sample.model.booking

interface BookingRepository {
  fun getNewUid(): String

  suspend fun getAllBookings(): List<Booking>

  suspend fun getBooking(bookingId: String): Booking?

  suspend fun getBookingsByTutor(tutorId: String): List<Booking>

  suspend fun getBookingsByUserId(userId: String): List<Booking>

  suspend fun getBookingsByStudent(studentId: String): List<Booking>

  suspend fun getBookingsByListing(listingId: String): List<Booking>

  suspend fun addBooking(booking: Booking)

  suspend fun updateBooking(bookingId: String, booking: Booking)

  suspend fun deleteBooking(bookingId: String)

  suspend fun deleteAllBookingOfUser(userId: String)

  /** Updates booking status */
  suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)

  /** Updates payment status */
  suspend fun updatePaymentStatus(bookingId: String, paymentStatus: PaymentStatus)

  /** Confirms a pending booking */
  suspend fun confirmBooking(bookingId: String)

  /** Completes a booking */
  suspend fun completeBooking(bookingId: String)

  /** Cancels a booking */
  suspend fun cancelBooking(bookingId: String)

  /** Checks if there is an ongoing booking between two users */
  suspend fun hasOngoingBookingBetween(userA: String, userB: String): Boolean
}
