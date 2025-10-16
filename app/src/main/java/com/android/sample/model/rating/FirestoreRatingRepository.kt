package com.android.sample.model.rating

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlinx.coroutines.tasks.await

const val RATINGS_COLLECTION_PATH = "ratings"

class FirestoreRatingRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : RatingRepository {

  private val currentUserId: String
    get() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getAllRatings(): List<Rating> {
    try {
      val snapshot =
          db.collection(RATINGS_COLLECTION_PATH)
              .whereEqualTo("fromUserId", currentUserId)
              .get()
              .await()
      return snapshot.toObjects(Rating::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch ratings: ${e.message}")
    }
  }

  override suspend fun getRating(ratingId: String): Rating? {
    try {
      val document = db.collection(RATINGS_COLLECTION_PATH).document(ratingId).get().await()
      if (!document.exists()) {
        return null
      }
      val rating =
          document.toObject(Rating::class.java)
              ?: throw Exception("Failed to parse Rating with ID $ratingId")

      if (rating.fromUserId != currentUserId && rating.toUserId != currentUserId) {
        throw Exception("Access denied: This rating is not related to the current user")
      }
      return rating
    } catch (e: Exception) {
      throw Exception("Failed to get rating: ${e.message}")
    }
  }

  override suspend fun getRatingsByFromUser(fromUserId: String): List<Rating> {
    try {
      val snapshot =
          db.collection(RATINGS_COLLECTION_PATH)
              .whereEqualTo("fromUserId", fromUserId)
              .get()
              .await()
      return snapshot.toObjects(Rating::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch ratings from user $fromUserId: ${e.message}")
    }
  }

  override suspend fun getRatingsByToUser(toUserId: String): List<Rating> {
    try {
      val snapshot =
          db.collection(RATINGS_COLLECTION_PATH).whereEqualTo("toUserId", toUserId).get().await()
      return snapshot.toObjects(Rating::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch ratings for user $toUserId: ${e.message}")
    }
  }

  override suspend fun getRatingsOfListing(listingId: String): List<Rating> {
    try {
      val snapshot =
          db.collection(RATINGS_COLLECTION_PATH)
              .whereEqualTo("ratingType", "LISTING")
              .whereEqualTo("targetObjectId", listingId)
              .get()
              .await()
      return snapshot.toObjects(Rating::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch ratings for listing $listingId: ${e.message}")
    }
  }

  override suspend fun addRating(rating: Rating) {
    try {
      if (rating.fromUserId != currentUserId) {
        throw Exception("Access denied: You can only add ratings behalf of yourself.")
      }
      if (rating.toUserId == currentUserId) {
        throw Exception("You cannot rate yourself.")
      }
      db.collection(RATINGS_COLLECTION_PATH).document(rating.ratingId).set(rating).await()
    } catch (e: Exception) {
      throw Exception("Failed to add rating: ${e.message}")
    }
  }

  override suspend fun updateRating(ratingId: String, rating: Rating) {
    try {
      val documentRef = db.collection(RATINGS_COLLECTION_PATH).document(ratingId)
      val existingRating = getRating(ratingId) // Leverages existing access check

      if (existingRating != null) {
        if (existingRating.fromUserId != currentUserId) {
          throw Exception("Access denied: You can only update ratings you have created.")
        }
      }

      documentRef.set(rating).await()
    } catch (e: Exception) {
      throw Exception("Failed to update rating: ${e.message}")
    }
  }

  override suspend fun deleteRating(ratingId: String) {
    try {
      val documentRef = db.collection(RATINGS_COLLECTION_PATH).document(ratingId)
      val rating = getRating(ratingId) // Leverages existing access check

      if (rating != null) {
        if (rating.fromUserId != currentUserId) {
          throw Exception("Access denied: You can only delete ratings you have created.")
        }
      }

      documentRef.delete().await()
    } catch (e: Exception) {
      throw Exception("Failed to delete rating: ${e.message}")
    }
  }

  override suspend fun getTutorRatingsOfUser(userId: String): List<Rating> {
    try {
      val snapshot =
          db.collection(RATINGS_COLLECTION_PATH)
              .whereEqualTo("toUserId", userId)
              .whereEqualTo("ratingType", "TUTOR")
              .get()
              .await()
      return snapshot.toObjects(Rating::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch tutor ratings for user $userId: ${e.message}")
    }
  }

  override suspend fun getStudentRatingsOfUser(userId: String): List<Rating> {
    try {
      val snapshot =
          db.collection(RATINGS_COLLECTION_PATH)
              .whereEqualTo("toUserId", userId)
              .whereEqualTo("ratingType", "STUDENT")
              .get()
              .await()
      return snapshot.toObjects(Rating::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch student ratings for user $userId: ${e.message}")
    }
  }
}
