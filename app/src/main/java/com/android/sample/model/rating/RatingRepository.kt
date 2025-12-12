package com.android.sample.model.rating

interface RatingRepository {
  fun getNewUid(): String

  suspend fun getAllRatings(): List<Rating>

  suspend fun getRating(ratingId: String): Rating?

  suspend fun getRatingsByFromUser(fromUserId: String): List<Rating>

  suspend fun getRatingsByToUser(toUserId: String): List<Rating>

  suspend fun getRatingsOfListing(listingId: String): List<Rating>

  suspend fun addRating(rating: Rating)

  suspend fun updateRating(ratingId: String, rating: Rating)

  suspend fun deleteRating(ratingId: String)

  /** Gets all tutor ratings for listings owned by this user */
  suspend fun getTutorRatingsOfUser(userId: String): List<Rating>

  /** Gets all student ratings received by this user */
  suspend fun getStudentRatingsOfUser(userId: String): List<Rating>

  suspend fun deleteAllRatingOfUser(userId: String)

  suspend fun hasRating(
      fromUserId: String,
      toUserId: String,
      ratingType: RatingType,
      targetObjectId: String
  ): Boolean
}
