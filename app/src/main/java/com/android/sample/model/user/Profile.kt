package com.android.sample.model.user

import com.android.sample.model.map.Location

/** Enhanced user profile with dual rating system */
data class Profile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val location: Location = Location(),
    val description: String = "",
    val tutorRating: RatingInfo = RatingInfo(),
    val studentRating: RatingInfo = RatingInfo()
)

/** Encapsulates rating information for a user */
data class RatingInfo(val averageRating: Double = 0.0, val totalRatings: Int = 0) {
  init {
    require(averageRating == 0.0 || averageRating in 1.0..5.0) {
      "Average rating must be 0.0 or between 1.0 and 5.0"
    }
    require(totalRatings >= 0) { "Total ratings must be non-negative" }
  }
}
