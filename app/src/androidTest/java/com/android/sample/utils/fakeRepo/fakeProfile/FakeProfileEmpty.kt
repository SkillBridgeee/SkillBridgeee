package com.android.sample.utils.fakeRepo.fakeProfile

import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import java.util.UUID

class FakeProfileEmpty : FakeProfileRepo {

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
              tutorRating = RatingInfo()))

  override fun getCurrentUserId(): String {
    return profiles[0].userId
  }

  override fun getCurrentUserName(): String? {
    return profiles[0].name
  }

  override fun getNewUid(): String {
    return "profile_${UUID.randomUUID()}"
  }

  override suspend fun getProfile(userId: String): Profile? =
      profiles.find { profile -> profile.userId == userId }

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
}
