package com.android.sample.utils.fakeRepo.fakeRating

import com.android.sample.model.rating.Rating

// todo implementer ce file
class RatingFakeRepoWorking : FakeRatingRepo {
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
    return emptyList()
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
