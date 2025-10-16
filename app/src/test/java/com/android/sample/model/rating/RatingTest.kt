package com.android.sample.model.rating

import org.junit.Test

class RatingTest {

  @Test
  fun `valid rating passes validation`() {
    val rating =
        Rating(
            ratingId = "rating1",
            fromUserId = "user1",
            toUserId = "user2",
            starRating = StarRating.FIVE,
            comment = "Excellent",
            ratingType = RatingType.TUTOR,
            targetObjectId = "listing1")
    rating.validate() // Should not throw
  }

  @Test(expected = IllegalArgumentException::class)
  fun `rating with blank fromUserId fails validation`() {
    val rating = Rating(fromUserId = "", toUserId = "user2", targetObjectId = "listing1")
    rating.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `rating with blank toUserId fails validation`() {
    val rating = Rating(fromUserId = "user1", toUserId = "", targetObjectId = "listing1")
    rating.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `rating with same from and to user fails validation`() {
    val rating = Rating(fromUserId = "user1", toUserId = "user1", targetObjectId = "listing1")
    rating.validate()
  }

  @Test(expected = IllegalArgumentException::class)
  fun `rating with blank targetObjectId fails validation`() {
    val rating = Rating(fromUserId = "user1", toUserId = "user2", targetObjectId = "")
    rating.validate()
  }

  @Test
  fun `valid RatingInfo passes validation`() {
    RatingInfo(averageRating = 4.5, totalRatings = 10) // Should not throw
    RatingInfo(averageRating = 1.0, totalRatings = 1) // Should not throw
    RatingInfo(averageRating = 5.0, totalRatings = 1) // Should not throw
    RatingInfo(averageRating = 0.0, totalRatings = 0) // Should not throw
  }

  @Test(expected = IllegalArgumentException::class)
  fun `RatingInfo with average below 1_0 fails validation`() {
    RatingInfo(averageRating = 0.9, totalRatings = 1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `RatingInfo with average above 5_0 fails validation`() {
    RatingInfo(averageRating = 5.1, totalRatings = 1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `RatingInfo with negative totalRatings fails validation`() {
    RatingInfo(averageRating = 4.0, totalRatings = -1)
  }
}
