package com.android.sample.model.tutor

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository

class TutorProfileRepositoryLocal : ProfileRepository {

  private val profiles = mutableListOf<Profile>()

  private val userSkills = mutableMapOf<String, MutableList<Skill>>()

  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun getProfile(userId: String): Profile {
    TODO("Not yet implemented")
  }

  override suspend fun addProfile(profile: Profile) {
    TODO("Not yet implemented")
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteProfile(userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun getAllProfiles(): List<Profile> {
    TODO("Not yet implemented")
  }

  override suspend fun searchProfilesByLocation(
      location: Location,
      radiusKm: Double
  ): List<Profile> {
    TODO("Not yet implemented")
  }

  override suspend fun getProfileById(userId: String): Profile {
    return profiles.find { it.userId == userId }
        ?: throw IllegalArgumentException("TutorRepositoryLocal: Profile not found for $userId")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    return userSkills[userId]?.toList() ?: emptyList()
  }
}
