package com.android.sample.model.booking

import java.util.Date

/** Enhanced booking with listing association */
data class Booking(
    val bookingId: String = "",
    val listingId: String = "",
    val providerId: String = "",
    val receiverId: String = "",
    val sessionStart: Date = Date(),
    val sessionEnd: Date = Date(),
    val status: BookingStatus = BookingStatus.PENDING,
    val price: Double = 0.0
) {
  init {
    require(sessionStart.before(sessionEnd)) { "Session start must be before session end" }
    require(providerId != receiverId) { "Provider and receiver must be different users" }
    require(price >= 0) { "Price must be non-negative" }
  }
}

enum class BookingStatus {
  PENDING,
  CONFIRMED,
  COMPLETED,
  CANCELLED
}
