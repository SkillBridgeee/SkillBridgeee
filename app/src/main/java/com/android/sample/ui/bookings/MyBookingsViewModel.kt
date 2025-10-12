package com.android.sample.ui.bookings

import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * UI model for a single booking row in the "My Bookings" list.
 *
 * @property id Stable identifier used for list keys and diffing.
 * @property tutorName Display name of the tutor (first character used for the avatar chip).
 * @property subject Course / subject title shown under the name.
 * @property pricePerHourLabel Formatted price per hour (e.g., "$50/hr").
 * @property durationLabel Formatted duration (e.g., "2hrs").
 * @property dateLabel Booking date as a string in `dd/MM/yyyy`.
 * @property ratingStars Star count clamped to [0, 5] for rendering.
 * @property ratingCount Total number of ratings shown next to the stars.
 */
data class BookingCardUi(
    val id: String,
    val tutorName: String,
    val subject: String,
    val pricePerHourLabel: String, // e.g., "$50/hr"
    val durationLabel: String, // e.g., "2hrs"
    val dateLabel: String, // e.g., "06/10/2025"
    val ratingStars: Int, // 0..5
    val ratingCount: Int
)

/**
 * ViewModel for the **My Bookings** screen.
 *
 * Exposes a `StateFlow<List<BookingCardUi>>` that the UI collects to render the list of bookings.
 * The current implementation serves **demo data only** (for screens/tests); no repository or
 * persistence is wired yet.
 *
 * Public API
 * - [items]: hot `StateFlow` of the current list of [BookingCardUi]. List items are stable and
 *   keyed by [BookingCardUi.id].
 *
 * Guarantees
 * - `dateLabel` is formatted as `dd/MM/yyyy` (numerals follow the device locale).
 * - `ratingStars` is within 0..5.
 *
 * Next steps (not part of this PR)
 * - Replace demo generation with a repository-backed flow of domain `Booking` models.
 * - Map domain → UI using i18n-aware formatters for dates, price, and duration.
 */
class MyBookingsViewModel : ViewModel() {

  // Backing state; mutated only inside the VM.
  private val _items = MutableStateFlow<List<BookingCardUi>>(emptyList())

  /** Stream of bookings for the UI. */
  val items: StateFlow<List<BookingCardUi>> = _items

  init {
    _items.value = demo()
  }

  // --- Demo data generation (deterministic) -----------------------------------------------

  /**
   * Builds a deterministic list of demo bookings used for previews and tests.
   *
   * Dates are generated from "today" using [Calendar] so that:
   * - entry #1 is +1 day, 2 hours long
   * - entry #2 is +5 days, 1 hour long
   */
  private fun demo(): List<BookingCardUi> {
    val df = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun datePlus(daysFromNow: Int): String {
      val cal = Calendar.getInstance()
      cal.add(Calendar.DAY_OF_MONTH, daysFromNow)
      return df.format(cal.time)
    }

    return listOf(
        BookingCardUi(
            id = "b1",
            tutorName = "Liam P.",
            subject = "Piano Lessons",
            pricePerHourLabel = "$50/hr",
            durationLabel = "2hrs",
            dateLabel = datePlus(1),
            ratingStars = 5,
            ratingCount = 23),
        BookingCardUi(
            id = "b2",
            tutorName = "Maria G.",
            subject = "Calculus & Algebra",
            pricePerHourLabel = "$30/hr",
            durationLabel = "1hr",
            dateLabel = datePlus(5),
            ratingStars = 4,
            ratingCount = 41))
  }
}
