package com.android.sample.mockRepository.profileRepo

import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import java.util.*

/**
 * A fake implementation of [ProfileRepository] that provides a predefined set of user profiles.
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
class ProfileFakeRepoWorking : ProfileRepository {

  private val profiles: Map<String, Profile> =
      mapOf(
          "creator_1" to
              Profile(
                  userId = "creator_1",
                  name = "Alice",
                  email = "alice@example.com",
                  levelOfEducation = "Master",
                  location = Location(),
                  hourlyRate = "30",
                  description = "Experienced math tutor",
                  tutorRating = RatingInfo()),
          "creator_2" to
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

  override suspend fun getProfile(userId: String): Profile? = profiles[userId]

  override suspend fun addProfile(profile: Profile) {
    // immutable mock → pas de persistance
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    // immutable mock → pas de persistance
  }

  override suspend fun deleteProfile(userId: String) {
    // immutable mock → pas de persistance
  }

  override suspend fun getAllProfiles(): List<Profile> = profiles.values.toList()

  override suspend fun searchProfilesByLocation(
      location: Location,
      radiusKm: Double
  ): List<Profile> = profiles.values.toList()

  override suspend fun getProfileById(userId: String): Profile? = profiles[userId]

  override suspend fun getSkillsForUser(userId: String): List<Skill> = emptyList()
}
