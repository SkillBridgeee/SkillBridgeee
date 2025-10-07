package com.android.sample.model.user

/** Data class representing tutor information */
data class Tutor(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val location: String = "",
    val description: String = "",
    val skills: List<String> = emptyList(), // Will reference Skills data
    val starRating: Double = 0.0, // Average rating 1.0-5.0
    val ratingNumber: Int = 0 // Number of ratings received
) {
  init {
    require(starRating == 0.0 || starRating in 1.0..5.0) {
      "Star rating must be 0.0 (no rating) or between 1.0 and 5.0"
    }
    require(ratingNumber >= 0) { "Rating number must be non-negative" }
  }
}
