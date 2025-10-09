package com.android.sample.model.booking

import java.util.Calendar
import org.junit.Test

class BookingModelTest {
  @Test(expected = IllegalArgumentException::class)
  fun start_after_end_throws() {
    val cal = Calendar.getInstance()
    val end = cal.time
    cal.add(Calendar.HOUR_OF_DAY, 1)
    val start = cal.time // start > end

    Booking(
        bookingId = "x",
        tutorId = "t",
        tutorName = "Tutor",
        bookerId = "u",
        bookerName = "You",
        sessionStart = start,
        sessionEnd = end)
  }
}
