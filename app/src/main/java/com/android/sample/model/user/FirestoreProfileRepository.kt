package com.android.sample.model.user

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

  private val currentUserId: String
    get() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getProfile(userId: String): Profile {
    return try {
      val document = db.collection(PROFILES_COLLECTION_PATH).document(userId).get().await()
      document.toObject(Profile::class.java)
          ?: throw Exception("Profile with ID $userId not found or failed to parse")
    } catch (e: Exception) {
      throw Exception("Failed to get profile for user $userId: ${e.message}")
    }
  }

  override suspend fun addProfile(profile: Profile) {
    try {
      if (profile.userId != currentUserId) {
        throw Exception("Access denied: You can only create a profile for yourself.")
      }
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

  override suspend fun getProfileById(userId: String): Profile {
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
}
