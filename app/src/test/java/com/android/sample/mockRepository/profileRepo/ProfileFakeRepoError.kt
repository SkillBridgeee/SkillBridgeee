package com.android.sample.mockRepository.profileRepo

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository

/**
 * A fake implementation of [ProfileRepository] that always throws exceptions.
 *
 * This mock repository is used to test how the application handles errors when interacting with
 * profile-related data sources.
 *
 * Each method intentionally throws a descriptive exception to simulate various failure scenarios,
 * such as:
 * - Network failures
 * - Database access issues
 * - Invalid input or missing profile data
 *
 * Typical use cases:
 * - Verifying ViewModel or UseCase error handling logic.
 * - Ensuring the UI reacts correctly to repository failures (e.g., showing error messages, retry
 *   prompts, or fallback states).
 * - Testing the robustness and error recovery flows of the app.
 */
class ProfileFakeRepoError : ProfileRepository {

  override fun getNewUid(): String {
    throw IllegalStateException("Failed to generate new profile UID")
  }

  override fun getCurrentUserId(): String {
    throw IllegalStateException("Failed to get current user ID")
  }

  override suspend fun getProfile(userId: String): Profile? {
    throw IllegalArgumentException("Error fetching profile for userId: $userId")
  }

  override suspend fun addProfile(profile: Profile) {
    throw UnsupportedOperationException("Error adding profile: ${profile.userId}")
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    throw IllegalStateException("Error updating profile for userId: $userId")
  }

  override suspend fun deleteProfile(userId: String) {
    throw IllegalStateException("Error deleting profile for userId: $userId")
  }

  override suspend fun getAllProfiles(): List<Profile> {
    throw RuntimeException("Error fetching all profiles")
  }

  override suspend fun searchProfilesByLocation(
      location: Location,
      radiusKm: Double
  ): List<Profile> {
    throw RuntimeException("Error searching profiles near $location within ${radiusKm}km")
  }

  override suspend fun getProfileById(userId: String): Profile? {
    throw IllegalArgumentException("Error fetching profile by ID: $userId")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    throw RuntimeException("Error fetching skills for userId: $userId")
  }

  override suspend fun updateTutorRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {
    throw IllegalStateException("Error updating tutor rating fields for userId: $userId")
  }

  override suspend fun updateStudentRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {
    throw IllegalStateException("Error updating student rating fields for userId: $userId")
  }
}
