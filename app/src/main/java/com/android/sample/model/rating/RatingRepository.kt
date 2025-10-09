package com.android.sample.model.rating

interface RatingRepository {
  fun getNewUid(): String

  suspend fun getAllRatings(): List<Rating>

  suspend fun getRating(ratingId: String): Rating

  suspend fun getRatingsByFromUser(fromUserId: String): List<Rating>

  suspend fun getRatingsByToUser(toUserId: String): List<Rating>

  suspend fun getRatingsByListing(listingId: String): List<Rating>

  suspend fun getRatingsByBooking(bookingId: String): Rating?

  suspend fun addRating(rating: Rating)

  suspend fun updateRating(ratingId: String, rating: Rating)

  suspend fun deleteRating(ratingId: String)

  /** Gets all tutor ratings for listings owned by this user */
  suspend fun getTutorRatingsForUser(
      userId: String,
      listingRepository: com.android.sample.model.listing.ListingRepository
  ): List<Rating>

  /** Gets all student ratings received by this user */
  suspend fun getStudentRatingsForUser(userId: String): List<Rating>

  /** Adds rating and updates the corresponding user's profile rating */
  suspend fun addRatingAndUpdateProfile(
      rating: Rating,
      profileRepository: com.android.sample.model.user.ProfileRepository,
      listingRepository: com.android.sample.model.listing.ListingRepository
  )
}
