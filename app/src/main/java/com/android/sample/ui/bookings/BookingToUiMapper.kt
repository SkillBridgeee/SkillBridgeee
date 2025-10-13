package com.android.sample.ui.bookings

import com.android.sample.model.booking.Booking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingToUiMapper(private val locale: Locale = Locale.getDefault()) {

  private val dateFormat = SimpleDateFormat("dd/MM/yyyy", locale)

  private fun findValue(b: Booking, possibleNames: List<String>): Any? {
    return try {
      possibleNames.forEach { name ->
        val getter = "get" + name.replaceFirstChar { it.uppercaseChar() }
        val method =
            b.javaClass.methods.firstOrNull { m ->
              m.parameterCount == 0 &&
                  (m.name.equals(getter, ignoreCase = true) ||
                      m.name.equals(name, ignoreCase = true))
            }
        if (method != null) {
          try {
            val v = method.invoke(b)
            if (v != null) return v
          } catch (_: Throwable) {
            /* ignore */
          }
        }
        val field =
            b.javaClass.declaredFields.firstOrNull { f -> f.name.equals(name, ignoreCase = true) }
        if (field != null) {
          try {
            field.isAccessible = true
            val v = field.get(b)
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

  private fun findValueOn(obj: Any, possibleNames: List<String>): Any? {
    try {
      if (obj is Map<*, *>) {
        possibleNames.forEach { name ->
          if (obj.containsKey(name)) {
            val v = obj[name]
            if (v != null) return v
          }
        }
      }
      val cls = obj.javaClass
      possibleNames.forEach { name ->
        val getter = "get" + name.replaceFirstChar { it.uppercaseChar() }
        val method =
            cls.methods.firstOrNull { m ->
              m.parameterCount == 0 &&
                  (m.name.equals(getter, ignoreCase = true) ||
                      m.name.equals(name, ignoreCase = true))
            }
        if (method != null) {
          try {
            val v = method.invoke(obj)
            if (v != null) return v
          } catch (_: Throwable) {
            /* ignore */
          }
        }
        val field = cls.declaredFields.firstOrNull { f -> f.name.equals(name, ignoreCase = true) }
        if (field != null) {
          try {
            field.isAccessible = true
            val v = field.get(obj)
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

  private fun safeStringProperty(b: Booking, names: List<String>): String? {
    val v = findValue(b, names) ?: return null
    return when (v) {
      is String -> v
      is Number -> v.toString()
      is Date -> dateFormat.format(v)
      else -> v.toString()
    }
  }

  private fun safeNestedStringProperty(
      b: Booking,
      parentNames: List<String>,
      childNames: List<String>
  ): String? {
    val parent = findValue(b, parentNames) ?: return null
    val v = findValueOn(parent, childNames) ?: return null
    return when (v) {
      is String -> v
      is Number -> v.toString()
      is Date -> dateFormat.format(v)
      else -> v.toString()
    }
  }

  private fun safeDoubleProperty(b: Booking, names: List<String>): Double? {
    val v = findValue(b, names) ?: return null
    return when (v) {
      is Number -> v.toDouble()
      is String -> v.toDoubleOrNull()
      else -> null
    }
  }

  private fun safeIntProperty(b: Booking, names: List<String>): Int {
    val v = findValue(b, names) ?: return 0
    return when (v) {
      is Number -> v.toInt()
      is String -> v.toIntOrNull() ?: 0
      else -> 0
    }
  }

  private fun safeDateProperty(b: Booking, names: List<String>): Date? {
    val v = findValue(b, names) ?: return null
    return when (v) {
      is Date -> v
      is Long -> Date(v)
      is Number -> Date(v.toLong())
      is String -> {
        try {
          Date(v.toLong())
        } catch (_: Throwable) {
          null
        }
      }
      else -> null
    }
  }

  fun map(b: Booking): BookingCardUi {
    val start = safeDateProperty(b, listOf("sessionStart", "start", "startDate")) ?: Date()
    val end = safeDateProperty(b, listOf("sessionEnd", "end", "endDate")) ?: start

    val durationMs = (end.time - start.time).coerceAtLeast(0L)
    val hours = durationMs / (60 * 60 * 1000)
    val mins = (durationMs / (60 * 1000)) % 60

    val durationLabel =
        if (mins == 0L) {
          "${hours}hr" + if (hours == 1L) "" else "s"
        } else {
          "${hours}h ${mins}m"
        }

    val priceDouble = safeDoubleProperty(b, listOf("price", "hourlyRate", "pricePerHour", "rate"))
    val pricePerHourLabel =
        when {
          priceDouble != null -> String.format(Locale.US, "$%.1f/hr", priceDouble)
          else -> safeStringProperty(b, listOf("priceLabel", "price_per_hour", "priceText")) ?: "—"
        }

    val tutorName =
        safeStringProperty(b, listOf("tutorName", "tutor", "listingCreatorName", "creatorName"))
            ?: safeNestedStringProperty(
                b,
                listOf("tutor", "listingCreator", "creator"),
                listOf("name", "fullName", "displayName", "tutorName", "listingCreatorName"))
            ?: safeNestedStringProperty(
                b,
                listOf("associatedListing", "listing", "listingData"),
                listOf("creatorName", "listingCreatorName", "creator", "ownerName"))
            ?: safeStringProperty(b, listOf("listingCreatorId", "creatorId"))
            ?: "—"

    val subject =
        safeStringProperty(b, listOf("subject", "title", "lessonSubject", "course"))
            ?: safeNestedStringProperty(
                b,
                listOf("associatedListing", "listing", "listingData"),
                listOf("subject", "title", "lessonSubject", "course"))
            ?: safeNestedStringProperty(b, listOf("details", "meta"), listOf("subject", "title"))
            ?: "—"

    val rawRating = safeIntProperty(b, listOf("rating", "ratingValue", "score"))
    val ratingStars = rawRating.coerceIn(0, 5)
    val ratingCount = safeIntProperty(b, listOf("ratingCount", "ratingsCount", "reviews"))

    val dateLabel =
        try {
          dateFormat.format(start)
        } catch (_: Throwable) {
          ""
        }

    val id = safeStringProperty(b, listOf("bookingId", "id", "booking_id")) ?: ""
    val tutorId = safeStringProperty(b, listOf("listingCreatorId", "creatorId", "tutorId")) ?: ""

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
