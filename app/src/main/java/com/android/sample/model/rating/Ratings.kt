package com.android.sample.model.rating

/** Data class representing a rating given to a tutor */
data class Ratings(
    val rating: Int = 0, // Rating between 1-5 (should be validated before creation)
    val fromUserId: String = "", // UID of the user giving the rating
    val fromUserName: String = "", // Name of the user giving the rating
    val ratingId: String = "" // UID of the person who got the rating (tutor)
) {
  init {
    require(rating in 1..5) { "Rating must be between 1 and 5" }
  }
}
