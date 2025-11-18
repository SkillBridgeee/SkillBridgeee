package com.android.sample.utils.fakeRepo.fakeProfile

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile

class FakeProfileEmpty : FakeProfileRepo {
  override fun getCurrentUserId(): String {
    TODO("Not yet implemented")
  }

  override fun getCurrentUserName(): String? {
    TODO("Not yet implemented")
  }

  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun getProfile(userId: String): Profile? {
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

  override suspend fun getProfileById(userId: String): Profile? {
    TODO("Not yet implemented")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    TODO("Not yet implemented")
  }
}
