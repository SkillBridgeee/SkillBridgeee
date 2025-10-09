package com.android.sample.ui.bookings

import androidx.lifecycle.ViewModel
import com.android.sample.model.booking.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** UI model that contains everything the card needs */
data class BookingCardUi(
    val id: String,
    val tutorName: String,
    val subject: String,
    val pricePerHourLabel: String,   // "$50/hr"
    val durationLabel: String,       // "2hrs"
    val dateLabel: String,           // "06/10/2025"
    val ratingStars: Int,            // 0..5
    val ratingCount: Int
)

class MyBookingsViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<BookingCardUi>>(emptyList())
    val items: StateFlow<List<BookingCardUi>> = _items

    init { _items.value = demo() }

    private fun demo(): List<BookingCardUi> {
        val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun startEnd(daysFromNow: Int, hours: Int): Pair<java.util.Date, java.util.Date> {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, daysFromNow)
            val start = cal.time
            cal.add(Calendar.HOUR_OF_DAY, hours)   // ensure end > start
            val end = cal.time
            return start to end
        }

        val (s1, e1) = startEnd(1, 2)
        val (s2, e2) = startEnd(5, 1)

        // If you insist on constructing Booking objects, pass end correctly
        val b1 = Booking("b1", "t1", "Liam P.", "u_you", "You", s1, e1)
        val b2 = Booking("b2", "t2", "Maria G.", "u_you", "You", s2, e2)

        return listOf(
            BookingCardUi(
                id = b1.bookingId,
                tutorName = b1.tutorName,
                subject = "Piano Lessons",
                pricePerHourLabel = "$50/hr",
                durationLabel = "2hrs",
                dateLabel = df.format(b1.sessionStart),
                ratingStars = 5,
                ratingCount = 23
            ),
            BookingCardUi(
                id = b2.bookingId,
                tutorName = b2.tutorName,
                subject = "Calculus & Algebra",
                pricePerHourLabel = "$30/hr",
                durationLabel = "1hr",
                dateLabel = df.format(b2.sessionStart),
                ratingStars = 4,
                ratingCount = 41
            )
        )
    }

}
