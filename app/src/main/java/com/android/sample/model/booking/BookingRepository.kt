package com.android.sample.model.booking

interface BookingRepository {
  fun getNewUid(): String

  suspend fun getAllBookings(): List<Booking>

  suspend fun getBooking(bookingId: String): Booking

  suspend fun getBookingsByProvider(providerId: String): List<Booking>

  suspend fun getBookingsByReceiver(receiverId: String): List<Booking>

  suspend fun getBookingsByListing(listingId: String): List<Booking>

  suspend fun addBooking(booking: Booking)

  suspend fun updateBooking(bookingId: String, booking: Booking)

  suspend fun deleteBooking(bookingId: String)

  /** Updates booking status */
  suspend fun updateBookingStatus(bookingId: String, status: BookingStatus)

  /** Confirms a pending booking */
  suspend fun confirmBooking(bookingId: String)

  /** Completes a booking */
  suspend fun completeBooking(bookingId: String)

  /** Cancels a booking */
  suspend fun cancelBooking(bookingId: String)
}
