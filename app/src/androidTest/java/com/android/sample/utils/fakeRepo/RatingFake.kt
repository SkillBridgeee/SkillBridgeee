package com.android.sample.utils.fakeRepo

import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository

class RatingFake : RatingRepository {
  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun getAllRatings(): List<Rating> {
    TODO("Not yet implemented")
  }

  override suspend fun getRating(ratingId: String): Rating? {
    TODO("Not yet implemented")
  }

  override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> {
    TODO("Not yet implemented")
  }

  override suspend fun getRatingsByToUser(toUserId: String): List<Rating> {
    TODO("Not yet implemented")
  }

  override suspend fun getRatingsOfListing(listingId: String): List<Rating> {
    TODO("Not yet implemented")
  }

  override suspend fun addRating(rating: Rating) {
    TODO("Not yet implemented")
  }

  override suspend fun updateRating(ratingId: String, rating: Rating) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteRating(ratingId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun getTutorRatingsOfUser(userId: String): List<Rating> {
    TODO("Not yet implemented")
  }

  override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> {
    TODO("Not yet implemented")
  }
}
