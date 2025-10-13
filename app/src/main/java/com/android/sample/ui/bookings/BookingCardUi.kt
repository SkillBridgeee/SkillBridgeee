package com.android.sample.ui.bookings

/** UI model for a booking card rendered by MyBookingsScreen. */
data class BookingCardUi(
    val id: String,
    val tutorId: String,
    val tutorName: String,
    val subject: String,
    val pricePerHourLabel: String,
    val durationLabel: String,
    val dateLabel: String,
    val ratingStars: Int = 0,
    val ratingCount: Int = 0
)