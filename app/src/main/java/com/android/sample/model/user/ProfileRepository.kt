package com.android.sample.model.user

interface ProfileRepository {
  fun getNewUid(): String

  suspend fun getProfile(userId: String): Profile

  suspend fun addProfile(profile: Profile)

  suspend fun updateProfile(userId: String, profile: Profile)

  suspend fun deleteProfile(userId: String)

  suspend fun getAllProfiles(): List<Profile>

  /** Recalculates and updates tutor rating based on all their listing ratings */
  suspend fun recalculateTutorRating(
      userId: String,
      listingRepository: com.android.sample.model.listing.ListingRepository,
      ratingRepository: com.android.sample.model.rating.RatingRepository
  )

  /** Recalculates and updates student rating based on all bookings they've taken */
  suspend fun recalculateStudentRating(
      userId: String,
      ratingRepository: com.android.sample.model.rating.RatingRepository
  )

  suspend fun searchProfilesByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Profile>
}
