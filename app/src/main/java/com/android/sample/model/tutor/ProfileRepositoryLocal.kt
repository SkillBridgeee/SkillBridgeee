package com.android.sample.model.tutor

import com.android.sample.model.map.Location
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.MusicSkills
import com.android.sample.model.skill.Skill
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import java.util.UUID
import kotlin.math.*

class ProfileRepositoryLocal : ProfileRepository {

  private val profiles = mutableListOf<Profile>()
  private val userSkills = mutableMapOf<String, MutableList<Skill>>()


  override fun getNewUid(): String = UUID.randomUUID().toString()

  override suspend fun getAllProfiles(): List<Profile> = profiles.toList()


  override suspend fun getProfile(userId: String): Profile =
    profiles.find { it.userId == userId }
      ?: throw IllegalArgumentException("Profile not found for $userId")

  override suspend fun addProfile(profile: Profile) {
    // replace if same id exists, else add
    val idx = profiles.indexOfFirst { it.userId == profile.userId }
    if (idx >= 0) profiles[idx] = profile else profiles += profile
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    val idx = profiles.indexOfFirst { it.userId == userId }
    if (idx < 0) throw IllegalArgumentException("Profile not found for $userId")
    profiles[idx] = profile.copy(userId = userId)
  }

  override suspend fun deleteProfile(userId: String) {
    profiles.removeAll { it.userId == userId }
    userSkills.remove(userId)
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
  init {
    if (profiles.isEmpty()) {
      val id1 = getNewUid()
      val id2 = getNewUid()
      val id3 = getNewUid()

      profiles += Profile(
        userId = id1,
        name = "Liam P.",
        email = "liam@example.com",
        description = "Guitar lessons",
        tutorRating = RatingInfo(averageRating = 4.9, totalRatings = 23)
      )
      profiles += Profile(
        userId = id2,
        name = "David B.",
        email = "david@example.com",
        description = "Singing lessons",
        tutorRating = RatingInfo(averageRating = 4.6, totalRatings = 12)
      )
      profiles += Profile(
        userId = id3,
        name = "Stevie W.",
        email = "stevie@example.com",
        description = "Piano lessons",
        tutorRating = RatingInfo(averageRating = 4.7, totalRatings = 15)
      )

      userSkills[id1] = mutableListOf(
        Skill(
          userId = id1,
          mainSubject = MainSubject.MUSIC,
          skill = MusicSkills.GUITAR.name,
          skillTime = 5.0,
          expertise = ExpertiseLevel.EXPERT
        )
      )
      userSkills[id2] = mutableListOf(
        Skill(
          userId = id2,
          mainSubject = MainSubject.MUSIC,
          skill = MusicSkills.SINGING.name,
          skillTime = 3.0,
          expertise = ExpertiseLevel.ADVANCED
        )
      )
      userSkills[id3] = mutableListOf(
        Skill(
          userId = id3,
          mainSubject = MainSubject.MUSIC,
          skill = MusicSkills.PIANO.name,
          skillTime = 7.0,
          expertise = ExpertiseLevel.EXPERT
        )
      )
    }
  }
}
