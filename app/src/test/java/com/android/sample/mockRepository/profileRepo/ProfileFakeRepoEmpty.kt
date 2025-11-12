package com.android.sample.mockRepository.profileRepo

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository

class ProfileFakeRepoEmpty : ProfileRepository {
  override fun getNewUid(): String {
    return ""
  }

  override suspend fun getProfile(userId: String): Profile? {
    return null
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

  override suspend fun getProfileById(userId: String): Profile? {
    TODO("Not yet implemented")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    TODO("Not yet implemented")
  }
}
