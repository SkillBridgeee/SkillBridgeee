package com.android.sample.model.rating

/** Rating given to a listing after a booking is completed */
data class Rating(
    val ratingId: String = "",
    val listingId: String = "", // The context listing being rated
    val fromUserId: String = "", // Who gave the rating
    val toUserId: String = "", // Who receives the rating (listing owner or student)
    val starRating: StarRating = StarRating.ONE,
    val comment: String = "",
    val ratingType: RatingType = RatingType.TUTOR
)

enum class RatingType {
  TUTOR, // Rating for the listing/tutor's performance
  STUDENT, // Rating for the student's performance
  LISTING //Rating for the listing
}


data class RatingInfo(val averageRating: Double = 0.0, val totalRatings: Int = 0) {
    init {
        require(averageRating == 0.0 || averageRating in 1.0..5.0) {
            "Average rating must be 0.0 or between 1.0 and 5.0"
        }
        require(totalRatings >= 0) { "Total ratings must be non-negative" }
    }
}
