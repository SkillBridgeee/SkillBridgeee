package com.android.sample.utils.fakeRepo.fakeProfile

import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import java.util.UUID

/**
 * A fake implementation of [com.android.sample.model.user.ProfileRepository] that provides a
 * predefined set of user profiles.
 *
 * This mock repository is used for testing and development purposes, simulating a repository with
 * actual profiles without requiring a real backend.
 *
 * Features:
 * - Contains two initial profiles: one tutor and one student.
 * - Supports retrieving profiles by ID or listing all profiles.
 * - Supports basic search by location (returns all profiles in this mock).
 * - Immutable mock: add, update, and delete operations do not persist changes.
 *
 * Typical use cases:
 * - Verifying ViewModel or UseCase logic when profiles exist.
 * - Testing UI rendering of tutors and students.
 * - Simulating user interactions such as profile lookup.
 */
class FakeProfileWorking : FakeProfileRepo {

  private val profiles =
      mutableListOf(
          Profile(
              userId = "creator_1",
              name = "Alice",
              email = "alice@example.com",
              levelOfEducation = "Master",
              location = Location(),
              hourlyRate = "30",
              description = "Experienced math tutor",
              tutorRating = RatingInfo()),
          Profile(
              userId = "creator_2",
              name = "Bob",
              email = "bob@example.com",
              levelOfEducation = "Bachelor",
              location = Location(),
              hourlyRate = "45",
              description = "Student looking for physics help",
              studentRating = RatingInfo()))

  override fun getNewUid(): String = "profile_${UUID.randomUUID()}"

  override suspend fun getProfile(userId: String): Profile? =
      profiles.first { profile -> profile.userId == userId }

  override suspend fun addProfile(profile: Profile) {
    profiles.add(profile)
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    val index = profiles.indexOfFirst { it.userId == userId }

    if (index == -1)
        throw IllegalStateException("Failed to update profile: user $userId not found.")

    profiles[index] = profile
  }

  override suspend fun deleteProfile(userId: String) {
    profiles.removeAll { profile -> profile.userId == userId }
  }

  override suspend fun getAllProfiles(): List<Profile> = profiles.toList()

  override suspend fun searchProfilesByLocation(
      location: Location,
      radiusKm: Double
  ): List<Profile> = TODO("Not yet implemented")

  override suspend fun getProfileById(userId: String): Profile? = TODO("Not yet implemented")

  override suspend fun getSkillsForUser(userId: String): List<Skill> = TODO("Not yet implemented")

  override fun getCurrentUserId(): String {
    return profiles[0].userId
  }

  override fun getCurrentUserName(): String? {
    return profiles[0].name
  }

  override suspend fun updateTutorRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {
    val index = profiles.indexOfFirst { it.userId == userId }
    if (index != -1) {
      val p = profiles[index]
      profiles[index] = p.copy(tutorRating = RatingInfo(averageRating, totalRatings))
    }
  }

  override suspend fun updateStudentRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {
    val index = profiles.indexOfFirst { it.userId == userId }
    if (index != -1) {
      val p = profiles[index]
      profiles[index] = p.copy(studentRating = RatingInfo(averageRating, totalRatings))
    }
  }

  override suspend fun deleteAccount(userId: String) {
    TODO("Not yet implemented")
  }
}
