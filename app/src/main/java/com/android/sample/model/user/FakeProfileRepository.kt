package com.android.sample.model.user

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import kotlin.math.*

// Simple in-memory fake repository for tests / previews.
class FakeProfileRepository : ProfileRepository {
  private val data = mutableMapOf<String, Profile>()
  private var counter = 0

  override fun getNewUid(): String =
      synchronized(this) {
        counter += 1
        "u$counter"
      }

  override suspend fun getProfile(userId: String): Profile =
      data[userId] ?: throw NoSuchElementException("Profile not found: $userId")

  override suspend fun addProfile(profile: Profile) {
    val id = if (profile.userId.isBlank()) getNewUid() else profile.userId
    synchronized(this) { data[id] = profile.copy(userId = id) }
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    synchronized(this) { data[userId] = profile.copy(userId = userId) }
  }

  override suspend fun deleteProfile(userId: String) {
    synchronized(this) { data.remove(userId) }
  }

  override suspend fun getAllProfiles(): List<Profile> = synchronized(this) { data.values.toList() }

  override suspend fun searchProfilesByLocation(
      location: Location,
      radiusKm: Double
  ): List<Profile> {
    if (radiusKm <= 0.0) return getAllProfiles()
    return synchronized(this) {
      data.values.filter { distanceKm(it.location, location) <= radiusKm }
    }
  }

  override suspend fun getProfileById(userId: String): Profile {
    TODO("Not yet implemented")
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    TODO("Not yet implemented")
  }

  override suspend fun updateTutorRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {
    val p = data[userId] ?: return
    val updated =
        p.copy(
            tutorRating =
                p.tutorRating.copy(averageRating = averageRating, totalRatings = totalRatings))
    data[userId] = updated
  }

  override suspend fun updateStudentRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {
    val p = data[userId] ?: return
    val updated =
        p.copy(
            studentRating =
                p.studentRating.copy(averageRating = averageRating, totalRatings = totalRatings))
    data[userId] = updated
  }

  private fun distanceKm(a: Location, b: Location): Double {
    // Use the actual coordinate property names on Location (latitude / longitude)
    val R = 6371.0
    val dLat = Math.toRadians(a.latitude - b.latitude)
    val dLon = Math.toRadians(a.longitude - b.longitude)
    val lat1 = Math.toRadians(a.latitude)
    val lat2 = Math.toRadians(b.latitude)
    val hav = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
    return 2 * R * asin(sqrt(hav))
  }
}
