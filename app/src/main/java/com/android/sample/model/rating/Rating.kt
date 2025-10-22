package com.android.sample.model.rating

import com.google.firebase.firestore.DocumentId

/**
 * Represents a rating given by one user to another, in a specific context (Tutor, Student, or
 * Listing).
 *
 * @property ratingId The unique identifier for the rating.
 * @property fromUserId The ID of the user who gave the rating.
 * @property toUserId The ID of the user who received the rating.
 * @property starRating The star rating value.
 * @property comment An optional comment with the rating.
 * @property ratingType The type of the rating (e.g., Tutor, Student).
 * @property targetObjectId The ID of the object being rated (e.g., a listing ID or user ID).
 */
data class Rating(
    @DocumentId val ratingId: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val starRating: StarRating = StarRating.ONE,
    val comment: String = "",
    val ratingType: RatingType = RatingType.TUTOR,
    val targetObjectId: String = "",
) {
  /** Default constructor for Firestore deserialization. */
  constructor() :
      this(
          ratingId = "",
          fromUserId = "",
          toUserId = "",
          starRating = StarRating.ONE,
          comment = "",
          ratingType = RatingType.TUTOR,
          targetObjectId = "")

  /** Validates the rating data. Throws an [IllegalArgumentException] if the data is invalid. */
  fun validate() {
    require(fromUserId.isNotBlank()) { "From user ID must not be blank" }
    require(toUserId.isNotBlank()) { "To user ID must not be blank" }
    require(fromUserId != toUserId) { "From user and to user must be different" }
    require(targetObjectId.isNotBlank()) { "Target object ID must not be blank" }
  }
}

/** Represents the type of a rating. */
enum class RatingType {
  TUTOR,
  STUDENT,
  LISTING
}

/**
 * Holds aggregated rating information, such as the average rating and total number of ratings.
 *
 * @property averageRating The calculated average rating. Must be 0.0 or between 1.0 and 5.0.
 * @property totalRatings The total count of ratings. Must be non-negative.
 */
data class RatingInfo(val averageRating: Double = 0.0, val totalRatings: Int = 0) {
  init {
    require(averageRating == 0.0 || averageRating in 1.0..5.0) {
      "Average rating must be 0.0 or between 1.0 and 5.0"
    }
    require(totalRatings >= 0) { "Total ratings must be non-negative" }
  }
}
