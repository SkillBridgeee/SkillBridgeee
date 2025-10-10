package com.android.sample.model.booking

import java.util.Date

/** Data class representing a booking session */
data class Booking(
    val bookingId: String = "",
    val tutorId: String = "", // UID of the tutor
    val tutorName: String = "",
    val bookerId: String = "", // UID of the person booking
    val bookerName: String = "",
    val sessionStart: Date = Date(), // Date and time when session starts
    val sessionEnd: Date = Date() // Date and time when session ends
) {
  init {
    require(sessionStart.before(sessionEnd)) {
      "Session start time must be before session end time"
    }
  }
}
