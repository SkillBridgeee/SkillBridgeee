package com.android.sample.model.rating

/** Rating given to a listing after a booking is completed */
data class Rating(
    val ratingId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val starRating: StarRating = StarRating.ONE,
    val comment: String = "",
    val ratingType: RatingType
)

sealed class RatingType {
  data class Tutor(val listingId: String) : RatingType()

  data class Student(val studentId: String) : RatingType()

  data class Listing(val listingId: String) : RatingType()
}

data class RatingInfo(val averageRating: Double = 0.0, val totalRatings: Int = 0) {
  init {
    require(averageRating == 0.0 || averageRating in 1.0..5.0) {
      "Average rating must be 0.0 or between 1.0 and 5.0"
    }
    require(totalRatings >= 0) { "Total ratings must be non-negative" }
  }
}
