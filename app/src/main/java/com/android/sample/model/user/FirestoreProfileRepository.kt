package com.android.sample.model.user

import com.android.sample.model.ValidationUtils
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlinx.coroutines.tasks.await

const val PROFILES_COLLECTION_PATH = "profiles"

class FirestoreProfileRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ProfileRepository {

  private companion object {
    private const val NAME_MAX = 80
    private const val EMAIL_MAX = 254
    private const val EDUCATION_MAX = 300
    private const val DESC_MAX = 1200
    private const val RATE_MIN = 0.0
    private const val RATE_MAX = 200.0
    private val EMAIL_RE =
        Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
  }

  private val currentUserId: String
    get() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getProfile(userId: String): Profile? {
    return try {
      val document = db.collection(PROFILES_COLLECTION_PATH).document(userId).get().await()
      if (!document.exists()) {
        return null
      }
      document.toObject(Profile::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to get profile for user $userId: ${e.message}")
    }
  }

  override suspend fun addProfile(profile: Profile) {
    try {
      if (profile.userId != currentUserId) {
        throw Exception("Access denied: You can only create a profile for yourself.")
      }

      val cleaned = validateAndClean(profile)

      db.collection(PROFILES_COLLECTION_PATH).document(profile.userId).set(profile).await()
    } catch (e: Exception) {
      throw Exception("Failed to add profile: ${e.message}")
    }
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    try {
      if (userId != currentUserId) {
        throw Exception("Access denied: You can only update your own profile.")
      }
      ValidationUtils.requireId(userId, "userId")
      val cleaned = validateAndClean(profile.copy(userId = userId))
      db.collection(PROFILES_COLLECTION_PATH).document(userId).set(profile).await()
    } catch (e: Exception) {
      throw Exception("Failed to update profile for user $userId: ${e.message}")
    }
  }

  override suspend fun deleteProfile(userId: String) {
    try {
      if (userId != currentUserId) {
        throw Exception("Access denied: You can only delete your own profile.")
      }
      db.collection(PROFILES_COLLECTION_PATH).document(userId).delete().await()
    } catch (e: Exception) {
      throw Exception("Failed to delete profile for user $userId: ${e.message}")
    }
  }

  override suspend fun getAllProfiles(): List<Profile> {
    try {
      val snapshot = db.collection(PROFILES_COLLECTION_PATH).get().await()
      return snapshot.toObjects(Profile::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch all profiles: ${e.message}")
    }
  }

  override suspend fun searchProfilesByLocation(
      location: Location,
      radiusKm: Double
  ): List<Profile> {
    // Note: Firestore does not support complex geo-queries out of the box.
    // This would require a more complex setup with geohashing or a third-party service like
    // Algolia.
    throw NotImplementedError("Geo-search is not implemented.")
  }

  override suspend fun getProfileById(userId: String): Profile? {
    return getProfile(userId)
  }

  override suspend fun getSkillsForUser(userId: String): List<Skill> {
    // This assumes skills are stored in a sub-collection named 'skills' under each profile.
    try {
      val snapshot =
          db.collection(PROFILES_COLLECTION_PATH)
              .document(userId)
              .collection("skills")
              .get()
              .await()
      return snapshot.toObjects(Skill::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to get skills for user $userId: ${e.message}")
    }
  }

  private fun validateAndClean(p: Profile): Profile {
    // userId
    ValidationUtils.requireId(p.userId, "userId")

    // name (optional but bounded)
    p.name?.let {
      val t = it.trim()
      ValidationUtils.requireMaxLength(t, "name", NAME_MAX)
    }

    // email (required, reasonable max + format)
    val email = p.email.trim()
    ValidationUtils.requireNonBlank(email, "email")
    ValidationUtils.requireMaxLength(email, "email", EMAIL_MAX)
    require(EMAIL_RE.matches(email)) { "email format is invalid." }

    // levelOfEducation (optional, bounded)
    val edu = p.levelOfEducation.trim()
    ValidationUtils.requireMaxLength(edu, "levelOfEducation", EDUCATION_MAX)

    // description (optional, bounded)
    val desc = p.description.trim()
    ValidationUtils.requireMaxLength(desc, "description", DESC_MAX)

    // hourlyRate is a String in your model — coerce & bound
    val rateStr = p.hourlyRate.trim()
    ValidationUtils.requireNonBlank(rateStr, "hourlyRate")
    val rate =
        rateStr.toDoubleOrNull() ?: throw IllegalArgumentException("hourlyRate must be a number.")
    require(rate in RATE_MIN..RATE_MAX) { "hourlyRate must be between $RATE_MIN and $RATE_MAX." }

    // return a sanitized copy (trimmed fields, normalized rate back to String)
    return p.copy(
        name = p.name?.trim(),
        email = email,
        levelOfEducation = edu,
        description = desc,
        hourlyRate = rate.toString() // normalized (e.g., "50.0")
        )
  }
}
