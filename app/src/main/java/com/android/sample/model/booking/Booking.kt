package com.android.sample.model.booking

import androidx.compose.ui.graphics.Color
import com.android.sample.ui.theme.bkgCancelledColor
import com.android.sample.ui.theme.bkgCompletedColor
import com.android.sample.ui.theme.bkgConfirmedColor
import com.android.sample.ui.theme.bkgPendingColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Enhanced booking with listing association */
data class Booking(
    val bookingId: String = "",
    val associatedListingId: String = "",
    val listingCreatorId: String = "",
    val bookerId: String = "",
    val sessionStart: Date = Date(),
    val sessionEnd: Date = Date(),
    val status: BookingStatus = BookingStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING_PAYMENT,
    val price: Double = 0.0
) {
  // No-argument constructor for Firestore deserialization
  constructor() :
      this(
          "",
          "",
          "",
          "",
          Date(),
          Date(System.currentTimeMillis() + 1),
          BookingStatus.PENDING,
          PaymentStatus.PENDING_PAYMENT,
          0.0)

  /** Validates the booking data. Throws an [IllegalArgumentException] if the data is invalid. */
  fun validate() {
    require(sessionStart.before(sessionEnd)) { "Session start must be before session end" }
    require(listingCreatorId != bookerId) { "Provider and receiver must be different users" }
    require(price >= 0) { "Price must be non-negative" }
  }
}

enum class BookingStatus {
  PENDING,
  CONFIRMED,
  COMPLETED,
  CANCELLED
}

enum class PaymentStatus {
  PENDING_PAYMENT,
  PAID,
  CONFIRMED
}

fun Booking.dateString(): String {
  val formatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
  return formatter.format(this.sessionStart)
}

fun BookingStatus.color(): Color {
  return when (this) {
    BookingStatus.PENDING -> bkgPendingColor
    BookingStatus.CONFIRMED -> bkgConfirmedColor
    BookingStatus.COMPLETED -> bkgCompletedColor
    BookingStatus.CANCELLED -> bkgCancelledColor
  }
}

fun PaymentStatus.color(): Color {
  return when (this) {
    PaymentStatus.PENDING_PAYMENT -> Color.Gray // Placeholder color
    PaymentStatus.PAID -> Color.Green // Placeholder color
    PaymentStatus.CONFIRMED -> bkgCompletedColor
  }
}

fun BookingStatus.name(): String {
  return when (this) {
    BookingStatus.PENDING -> "PENDING"
    BookingStatus.CONFIRMED -> "CONFIRMED"
    BookingStatus.COMPLETED -> "COMPLETED"
    BookingStatus.CANCELLED -> "CANCELLED"
  }
}

fun PaymentStatus.name(): String {
  return when (this) {
    PaymentStatus.PENDING_PAYMENT -> "Pending Payment"
    PaymentStatus.PAID -> "Payment Sent"
    PaymentStatus.CONFIRMED -> "Payment Confirmed"
  }
}
