package com.android.sample.model.rating

/** Rating given to a listing after a booking is completed */
data class Rating(
    val ratingId: String = "",
    val bookingId: String = "",
    val listingId: String = "", // The listing being rated
    val fromUserId: String = "", // Who gave the rating
    val toUserId: String = "", // Who receives the rating (listing owner or student)
    val starRating: StarRating = StarRating.ONE,
    val comment: String = "",
    val ratingType: RatingType = RatingType.TUTOR
)

enum class RatingType {
  TUTOR, // Rating for the listing/tutor's performance
  STUDENT // Rating for the student's performance
}
