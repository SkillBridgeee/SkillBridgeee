package com.android.sample.mockRepository.profileRepo

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository

class ProfileFakeRepoError : ProfileRepository {

  override fun getNewUid(): String {
    throw IllegalStateException("Failed to generate new profile UID")
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
}
