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

  private val authenticatedUserId: String
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
      if (profile.userId != authenticatedUserId) {
        throw Exception("Access denied: You can only create a profile for yourself.")
      }

      val cleaned = validateAndClean(profile)

      db.collection(PROFILES_COLLECTION_PATH).document(cleaned.userId).set(cleaned).await()
    } catch (e: Exception) {
      throw Exception("Failed to add profile: ${e.message}")
    }
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    try {
      if (userId != authenticatedUserId) {
        throw Exception("Access denied: You can only update your own profile.")
      }
      ValidationUtils.requireId(userId, "userId")
      val cleaned = validateAndClean(profile.copy(userId = userId))
      db.collection(PROFILES_COLLECTION_PATH).document(userId).set(cleaned).await()
    } catch (e: Exception) {
      throw Exception("Failed to update profile for user $userId: ${e.message}")
    }
  }

  override suspend fun deleteProfile(userId: String) {
    try {
      if (userId != authenticatedUserId) {
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

  /**
   * Soft validation:
   * - Allow blanks initially.
   * - If a field is non-blank, validate content & bounds.
   * - Always trim strings; write the cleaned copy.
   */
  private fun validateAndClean(p: Profile): Profile {
    // required id
    ValidationUtils.requireId(p.userId, "userId")

    // name (nullable + optional)
    val name = p.name?.trim()
    name?.let { ValidationUtils.requireMaxLength(it, "name", NAME_MAX) }

    // email (non-null String, optional until provided)
    val email = p.email.trim()
    if (email.isNotEmpty()) {
      ValidationUtils.requireMaxLength(email, "email", EMAIL_MAX)
      require(EMAIL_RE.matches(email)) { "email format is invalid." }
    }

    // levelOfEducation (non-null String, optional)
    val edu = p.levelOfEducation.trim()
    if (edu.isNotEmpty()) {
      ValidationUtils.requireMaxLength(edu, "levelOfEducation", EDUCATION_MAX)
    }

    // description (non-null String, optional)
    val desc = p.description.trim()
    if (desc.isNotEmpty()) {
      ValidationUtils.requireMaxLength(desc, "description", DESC_MAX)
    }

    // hourlyRate (non-null String, optional until provided)
    val rateStr = p.hourlyRate.trim()
    val normalizedRate =
        if (rateStr.isEmpty()) ""
        else {
          val rate =
              rateStr.toDoubleOrNull()
                  ?: throw IllegalArgumentException("hourlyRate must be a number.")
          require(rate in RATE_MIN..RATE_MAX) {
            "hourlyRate must be between $RATE_MIN and $RATE_MAX."
          }
          rate.toString() // normalize
        }

    return p.copy(
        name = name,
        email = email, // trimmed (may be empty)
        levelOfEducation = edu, // trimmed (may be empty)
        description = desc, // trimmed (may be empty)
        hourlyRate = normalizedRate // "" or normalized number
        )
  }

  override suspend fun updateTutorRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {
    val updates =
        mapOf(
            "tutorRating.averageRating" to averageRating,
            "tutorRating.totalRatings" to totalRatings)

    db.collection(PROFILES_COLLECTION_PATH).document(userId).update(updates).await()
  }

  override suspend fun updateStudentRatingFields(
      userId: String,
      averageRating: Double,
      totalRatings: Int
  ) {
    try {
      val updates =
          mapOf(
              "studentRating.averageRating" to averageRating,
              "studentRating.totalRatings" to totalRatings)

      db.collection(PROFILES_COLLECTION_PATH).document(userId).update(updates).await()
    } catch (e: Exception) {
      throw Exception("Failed to update student rating for user $userId: ${e.message}")
    }
  }

  override fun getCurrentUserId(): String {
    return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
  }
}
