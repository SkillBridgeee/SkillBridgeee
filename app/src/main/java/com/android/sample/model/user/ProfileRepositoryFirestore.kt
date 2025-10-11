package com.android.sample.model.user

import android.util.Log
import com.android.sample.model.map.Location
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

const val PROFILES_COLLECTION_PATH = "profiles"

class ProfileRepositoryFirestore(private val db: FirebaseFirestore) : ProfileRepository {

  override fun getNewUid(): String {
    return db.collection(PROFILES_COLLECTION_PATH).document().id
  }

  override suspend fun getProfile(userId: String): Profile {
    val document = db.collection(PROFILES_COLLECTION_PATH).document(userId).get().await()
    return documentToProfile(document)
        ?: throw Exception("ProfileRepositoryFirestore: Profile not found")
  }

  override suspend fun getAllProfiles(): List<Profile> {
    val snapshot = db.collection(PROFILES_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { documentToProfile(it) }
  }

  override suspend fun addProfile(profile: Profile) {
    db.collection(PROFILES_COLLECTION_PATH).document(profile.userId).set(profile).await()
  }

  override suspend fun updateProfile(userId: String, profile: Profile) {
    db.collection(PROFILES_COLLECTION_PATH).document(userId).set(profile).await()
  }

  override suspend fun deleteProfile(userId: String) {
    db.collection(PROFILES_COLLECTION_PATH).document(userId).delete().await()
  }

  override suspend fun recalculateTutorRating(
      userId: String,
      listingRepository: com.android.sample.model.listing.ListingRepository,
      ratingRepository: com.android.sample.model.rating.RatingRepository
  ) {
    val tutorRatings = ratingRepository.getTutorRatingsForUser(userId, listingRepository)

    val ratingInfo =
        if (tutorRatings.isEmpty()) {
          RatingInfo(averageRating = 0.0, totalRatings = 0)
        } else {
          val average = tutorRatings.map { it.starRating.value }.average()
          RatingInfo(averageRating = average, totalRatings = tutorRatings.size)
        }

    val profile = getProfile(userId)
    val updatedProfile = profile.copy(tutorRating = ratingInfo)
    updateProfile(userId, updatedProfile)
  }

  override suspend fun recalculateStudentRating(
      userId: String,
      ratingRepository: com.android.sample.model.rating.RatingRepository
  ) {
    val studentRatings = ratingRepository.getStudentRatingsForUser(userId)

    val ratingInfo =
        if (studentRatings.isEmpty()) {
          RatingInfo(averageRating = 0.0, totalRatings = 0)
        } else {
          val average = studentRatings.map { it.starRating.value }.average()
          RatingInfo(averageRating = average, totalRatings = studentRatings.size)
        }

    val profile = getProfile(userId)
    val updatedProfile = profile.copy(studentRating = ratingInfo)
    updateProfile(userId, updatedProfile)
  }

  override suspend fun searchProfilesByLocation(
      location: Location,
      radiusKm: Double
  ): List<Profile> {
    val snapshot = db.collection(PROFILES_COLLECTION_PATH).get().await()
    return snapshot
        .mapNotNull { documentToProfile(it) }
        .filter { profile -> calculateDistance(location, profile.location) <= radiusKm }
  }

  private fun documentToProfile(document: DocumentSnapshot): Profile? {
    return try {
      val userId = document.id
      val name = document.getString("name") ?: return null
      val email = document.getString("email") ?: return null
      val locationData = document.get("location") as? Map<*, *>
      val location =
          locationData?.let {
            Location(
                latitude = it["latitude"] as? Double ?: 0.0,
                longitude = it["longitude"] as? Double ?: 0.0,
                name = it["name"] as? String ?: "")
          } ?: Location()
      val description = document.getString("description") ?: ""

      val tutorRatingData = document.get("tutorRating") as? Map<*, *>
      val tutorRating =
          tutorRatingData?.let {
            RatingInfo(
                averageRating = it["averageRating"] as? Double ?: 0.0,
                totalRatings = (it["totalRatings"] as? Long)?.toInt() ?: 0)
          } ?: RatingInfo()

      val studentRatingData = document.get("studentRating") as? Map<*, *>
      val studentRating =
          studentRatingData?.let {
            RatingInfo(
                averageRating = it["averageRating"] as? Double ?: 0.0,
                totalRatings = (it["totalRatings"] as? Long)?.toInt() ?: 0)
          } ?: RatingInfo()

      Profile(
          userId = userId,
          name = name,
          email = email,
          location = location,
          description = description,
          tutorRating = tutorRating,
          studentRating = studentRating)
    } catch (e: Exception) {
      Log.e("ProfileRepositoryFirestore", "Error converting document to Profile", e)
      null
    }
  }

  private fun calculateDistance(loc1: Location, loc2: Location): Double {
    val earthRadius = 6371.0
    val dLat = Math.toRadians(loc2.latitude - loc1.latitude)
    val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
    val a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(loc1.latitude)) *
                Math.cos(Math.toRadians(loc2.latitude)) *
                Math.sin(dLon / 2) *
                Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
  }
}
