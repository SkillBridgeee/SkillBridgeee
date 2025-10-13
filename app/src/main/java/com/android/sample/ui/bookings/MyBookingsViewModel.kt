// kotlin
package com.android.sample.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Screen UI states (keep logic-side). */
sealed class MyBookingsUiState {
  object Loading : MyBookingsUiState()

  data class Success(val items: List<BookingCardUi>) : MyBookingsUiState()

  object Empty : MyBookingsUiState()

  data class Error(val message: String) : MyBookingsUiState()
}

/** Maps domain Booking -> UI model; stays with logic. */
class BookingToUiMapper(private val locale: Locale = Locale.getDefault()) {
  private val dateFormat = SimpleDateFormat("dd/MM/yyyy", locale)

  // 1) safeString
  private fun safeString(b: Booking, names: List<String>): String? {
    val v = findValue(b, names) ?: return null
    return when (v) {
      is String -> v
      is Number -> v.toString()
      is Date -> dateFormat.format(v)
      else -> v.toString()
    }
  }

  // 2) safeNestedString
  private fun safeNestedString(b: Booking, parents: List<String>, children: List<String>): String? {
    val parent = findValue(b, parents) ?: return null
    val v = findValueOn(parent, children) ?: return null
    return when (v) {
      is String -> v
      is Number -> v.toString()
      is Date -> dateFormat.format(v)
      else -> v.toString()
    }
  }

  // 3) findValue  (block body)
  private fun findValue(b: Booking, names: List<String>): Any? {
    return try {
      for (name in names) {
        val getter = "get" + name.replaceFirstChar { it.uppercaseChar() }

        val m =
            b.javaClass.methods.firstOrNull {
              it.parameterCount == 0 && (it.name.equals(getter, true) || it.name.equals(name, true))
            }
        if (m != null) {
          try {
            val v = m.invoke(b)
            if (v != null) return v
          } catch (_: Throwable) {
            /* ignore */
          }
        }

        val f = b.javaClass.declaredFields.firstOrNull { it.name.equals(name, true) }
        if (f != null) {
          try {
            f.isAccessible = true
            val v = f.get(b)
            if (v != null) return v
          } catch (_: Throwable) {
            /* ignore */
          }
        }
      }
      null
    } catch (_: Throwable) {
      null
    }
  }

  // 4) findValueOn (block body)
  private fun findValueOn(obj: Any, names: List<String>): Any? {
    try {
      if (obj is Map<*, *>) {
        for (name in names) {
          if (obj.containsKey(name)) {
            val v = obj[name]
            if (v != null) return v
          }
        }
      }

      val cls = obj.javaClass
      for (name in names) {
        val getter = "get" + name.replaceFirstChar { it.uppercaseChar() }

        val m =
            cls.methods.firstOrNull {
              it.parameterCount == 0 && (it.name.equals(getter, true) || it.name.equals(name, true))
            }
        if (m != null) {
          try {
            val v = m.invoke(obj)
            if (v != null) return v
          } catch (_: Throwable) {
            /* ignore */
          }
        }

        val f = cls.declaredFields.firstOrNull { it.name.equals(name, true) }
        if (f != null) {
          try {
            f.isAccessible = true
            val v = f.get(obj)
            if (v != null) return v
          } catch (_: Throwable) {
            /* ignore */
          }
        }
      }
    } catch (_: Throwable) {
      /* ignore */
    }
    return null
  }

  private fun safeDouble(b: Booking, names: List<String>): Double? {
    val v = findValue(b, names) ?: return null
    return when (v) {
      is Number -> v.toDouble()
      is String -> v.toDoubleOrNull()
      else -> null
    }
  }

  private fun safeInt(b: Booking, names: List<String>): Int {
    val v = findValue(b, names) ?: return 0
    return when (v) {
      is Number -> v.toInt()
      is String -> v.toIntOrNull() ?: 0
      else -> 0
    }
  }

  private fun safeDate(b: Booking, names: List<String>): Date? {
    val v = findValue(b, names) ?: return null
    return when (v) {
      is Date -> v
      is Long -> Date(v)
      is Number -> Date(v.toLong())
      is String -> v.toLongOrNull()?.let { Date(it) }
      else -> null
    }
  }

  fun map(b: Booking): BookingCardUi {
    val start = safeDate(b, listOf("sessionStart", "start", "startDate")) ?: Date()
    val end = safeDate(b, listOf("sessionEnd", "end", "endDate")) ?: start

    val durationMs = (end.time - start.time).coerceAtLeast(0L)
    val hours = durationMs / (60 * 60 * 1000)
    val mins = (durationMs / (60 * 1000)) % 60
    val durationLabel =
        if (mins == 0L) {
          val plural = if (hours > 1L) "s" else ""
          "${hours}hr$plural"
        } else {
          "${hours}h ${mins}m"
        }

    val priceDouble = safeDouble(b, listOf("price", "hourlyRate", "pricePerHour", "rate"))
    val pricePerHourLabel =
        priceDouble?.let { String.format(Locale.US, "$%.1f/hr", it) }
            ?: (safeString(b, listOf("priceLabel", "price_per_hour", "priceText")) ?: "—")

    val tutorName =
        safeString(b, listOf("tutorName", "tutor", "listingCreatorName", "creatorName"))
            ?: safeNestedString(
                b,
                listOf("tutor", "listingCreator", "creator"),
                listOf("name", "fullName", "displayName", "tutorName", "listingCreatorName"))
            ?: safeNestedString(
                b,
                listOf("associatedListing", "listing", "listingData"),
                listOf("creatorName", "listingCreatorName", "creator", "ownerName"))
            ?: safeString(b, listOf("listingCreatorId", "creatorId"))
            ?: "—"

    val subject =
        safeString(b, listOf("subject", "title", "lessonSubject", "course"))
            ?: safeNestedString(
                b,
                listOf("associatedListing", "listing", "listingData"),
                listOf("subject", "title", "lessonSubject", "course"))
            ?: safeNestedString(b, listOf("details", "meta"), listOf("subject", "title"))
            ?: "—"

    val ratingStars = safeInt(b, listOf("rating", "ratingValue", "score")).coerceIn(0, 5)
    val ratingCount = safeInt(b, listOf("ratingCount", "ratingsCount", "reviews"))

    val id = safeString(b, listOf("bookingId", "id", "booking_id")) ?: ""
    val tutorId = safeString(b, listOf("listingCreatorId", "creatorId", "tutorId")) ?: ""
    val dateLabel =
        try {
          SimpleDateFormat("dd/MM/yyyy", locale).format(start)
        } catch (_: Throwable) {
          ""
        }

    return BookingCardUi(
        id = id,
        tutorId = tutorId,
        tutorName = tutorName,
        subject = subject,
        pricePerHourLabel = pricePerHourLabel,
        durationLabel = durationLabel,
        dateLabel = dateLabel,
        ratingStars = ratingStars,
        ratingCount = ratingCount)
  }
}

/** ViewModel: owns loading + mapping, exposes list to UI. */
class MyBookingsViewModel(
    private val repo: BookingRepository,
    private val userId: String,
    private val mapper: BookingToUiMapper = BookingToUiMapper(),
    private val initialLoadBlocking: Boolean = false
) : ViewModel() {

  // existing items flow (kept for backward compatibility with your screen/tests)
  private val _items = MutableStateFlow<List<BookingCardUi>>(emptyList())
  val items: StateFlow<List<BookingCardUi>> = _items

  // NEW: UI state flow that callers/screens can observe for loading/empty/error
  private val _uiState = MutableStateFlow<MyBookingsUiState>(MyBookingsUiState.Loading)
  val uiState: StateFlow<MyBookingsUiState> = _uiState

  init {
    if (initialLoadBlocking) {
      // used in tests to synchronously populate items/state
      runBlocking { load() }
    } else {
      // normal async load
      viewModelScope.launch { load() }
    }
  }

  /** Public refresh: re-runs the same loading pipeline and updates both flows. */
  fun refresh() {
    viewModelScope.launch { load() }
  }

  /** Shared loader for init/refresh. Updates both items + uiState consistently. */
  private suspend fun load() {
    _uiState.value = MyBookingsUiState.Loading
    try {
      val bookings = repo.getBookingsByUserId(userId)
      val mapped = if (bookings.isEmpty()) emptyList() else bookings.map { mapper.map(it) }
      _items.value = mapped
      _uiState.value =
          if (mapped.isEmpty()) MyBookingsUiState.Empty else MyBookingsUiState.Success(mapped)
    } catch (t: Throwable) {
      _items.value = emptyList()
      _uiState.value = MyBookingsUiState.Error(t.message ?: "Something went wrong")
    }
  }
}
