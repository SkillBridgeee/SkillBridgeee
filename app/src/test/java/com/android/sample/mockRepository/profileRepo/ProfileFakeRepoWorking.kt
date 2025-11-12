package com.android.sample.mockRepository.profileRepo

import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import java.util.*

class ProfileFakeRepoWorking : ProfileRepository {

  // Profils correspondant aux listings/creators de FakeListingRepoForBookings
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
