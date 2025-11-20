package com.android.sample.utils.fakeRepo.fakeProfile

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile

/**
 * A fake implementation of FakeProfileRepo that simulates consistent failures.
 *
 * Every method in this repository throws an IllegalStateException, making it useful for testing
 * error handling, failure states, and fallback UI behavior.
 *
 * No data is stored, returned, or processed — all calls result in mock errors.
 */
class FakeProfileError : FakeProfileRepo {

  override fun getCurrentUserId(): String {
    throw IllegalStateException("Failed to get current user ID (mock error).")
  }

  override fun getCurrentUserName(): String? {
    throw IllegalStateException("Failed to get current user name (mock error).")
  }

  override fun getNewUid(): String {
    throw IllegalStateException("Failed to generate UID (mock error).")
  }

  override suspend fun getProfile(userId: String): Profile? {
    throw IllegalStateException("Failed to load profile for user: $userId (mock error).")
  }

  override suspend fun addProfile(profile: Profile) {
    throw IllegalStateException("Failed to add profile for user: ${profile.userId} (mock error).")
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    throw IllegalStateException("Failed to update profile for user: $userId (mock error).")
  }

  override suspend fun deleteProfile(userId: String) {
    throw IllegalStateException("Failed to delete profile for user: $userId (mock error).")
  }

  override suspend fun getAllProfiles(): List<Profile> {
    throw IllegalStateException("Failed to load all profiles (mock error).")
  }

  override suspend fun searchProfilesByLocation(
      location: Location,
      radiusKm: Double
  ): List<Profile> {
    throw IllegalStateException(
        "Failed to search profiles by location $location with radius $radiusKm km (mock error).")
  }

  override suspend fun getProfileById(userId: String): Profile? {
    throw IllegalStateException("Failed to get profile by ID: $userId (mock error).")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    throw IllegalStateException("Failed to get skills for user: $userId (mock error).")
  }
}
