package com.android.sample.model.user

import com.android.sample.model.skill.Skill

interface ProfileRepository {
  fun getNewUid(): String

  suspend fun getProfile(userId: String): Profile?

  suspend fun addProfile(profile: Profile)

  suspend fun updateProfile(userId: String, profile: Profile)

  suspend fun deleteProfile(userId: String)

  suspend fun getAllProfiles(): List<Profile>

  suspend fun searchProfilesByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Profile>

  suspend fun getProfileById(userId: String): Profile?

  suspend fun getSkillsForUser(userId: String): List<Skill>
}
