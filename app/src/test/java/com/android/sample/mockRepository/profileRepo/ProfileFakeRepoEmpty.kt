package com.android.sample.mockRepository.profileRepo

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository

/**
 * A fake implementation of [ProfileRepository] that returns empty or null data for all queries.
 *
 * This mock repository is used for testing how the application behaves when there are no user
 * profiles available.
 *
 * Each method either returns null or an empty list, simulating the case where the data source
 * contains no profiles or skills.
 *
 * Typical use cases:
 * - Verifying ViewModel or UseCase logic when no profiles exist.
 * - Ensuring the UI correctly displays empty states (e.g., empty lists, messages).
 * - Testing fallback behavior or default states in the absence of profiles.
 */
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
