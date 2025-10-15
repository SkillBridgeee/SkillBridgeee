package com.android.sample.model.listing

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID
import kotlinx.coroutines.tasks.await

const val LISTINGS_COLLECTION_PATH = "listings"

class FirestoreListingRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ListingRepository {

  private val currentUserId: String
    get() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getAllListings(): List<Listing> {
    return try {
      val snapshot =
          db.collection(LISTINGS_COLLECTION_PATH)
              .orderBy("createdAt", Query.Direction.DESCENDING)
              .get()
              .await()
      snapshot.documents.mapNotNull { it.toListing() }
    } catch (e: Exception) {
      throw Exception("Failed to fetch all listings: ${e.message}")
    }
  }

  override suspend fun getProposals(): List<Proposal> {
    return try {
      val snapshot =
          db.collection(LISTINGS_COLLECTION_PATH)
              .whereEqualTo("type", ListingType.PROPOSAL)
              .orderBy("createdAt", Query.Direction.DESCENDING)
              .get()
              .await()
      snapshot.toObjects(Proposal::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch proposals: ${e.message}")
    }
  }

  override suspend fun getRequests(): List<Request> {
    return try {
      val snapshot =
          db.collection(LISTINGS_COLLECTION_PATH)
              .whereEqualTo("type", ListingType.REQUEST)
              .orderBy("createdAt", Query.Direction.DESCENDING)
              .get()
              .await()
      snapshot.toObjects(Request::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch requests: ${e.message}")
    }
  }

  override suspend fun getListing(listingId: String): Listing {
    return try {
      val document = db.collection(LISTINGS_COLLECTION_PATH).document(listingId).get().await()
      document.toListing() ?: throw Exception("Listing with ID $listingId not found")
    } catch (e: Exception) {
      throw Exception("Failed to get listing: ${e.message}")
    }
  }

  override suspend fun getListingsByUser(userId: String): List<Listing> {
    return try {
      val snapshot =
          db.collection(LISTINGS_COLLECTION_PATH)
              .whereEqualTo("creatorUserId", userId)
              .orderBy("createdAt", Query.Direction.DESCENDING)
              .get()
              .await()
      snapshot.documents.mapNotNull { it.toListing() }
    } catch (e: Exception) {
      throw Exception("Failed to fetch listings for user $userId: ${e.message}")
    }
  }

  override suspend fun addProposal(proposal: Proposal) {
    addListing(proposal)
  }

  override suspend fun addRequest(request: Request) {
    addListing(request)
  }

  private suspend fun addListing(listing: Listing) {
    try {
      if (listing.creatorUserId != currentUserId) {
        throw Exception("Access denied: You can only create listings for yourself.")
      }
      db.collection(LISTINGS_COLLECTION_PATH).document(listing.listingId).set(listing).await()
    } catch (e: Exception) {
      throw Exception("Failed to add listing: ${e.message}")
    }
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    try {
      val docRef = db.collection(LISTINGS_COLLECTION_PATH).document(listingId)
      val existingListing = getListing(listingId)

      if (existingListing.creatorUserId != currentUserId) {
        throw Exception("Access denied: You can only update your own listings.")
      }
      docRef.set(listing).await()
    } catch (e: Exception) {
      throw Exception("Failed to update listing: ${e.message}")
    }
  }

  override suspend fun deleteListing(listingId: String) {
    try {
      val docRef = db.collection(LISTINGS_COLLECTION_PATH).document(listingId)
      val existingListing = getListing(listingId)

      if (existingListing.creatorUserId != currentUserId) {
        throw Exception("Access denied: You can only delete your own listings.")
      }
      docRef.delete().await()
    } catch (e: Exception) {
      throw Exception("Failed to delete listing: ${e.message}")
    }
  }

  override suspend fun deactivateListing(listingId: String) {
    try {
      val docRef = db.collection(LISTINGS_COLLECTION_PATH).document(listingId)
      val existingListing = getListing(listingId)

      if (existingListing.creatorUserId != currentUserId) {
        throw Exception("Access denied: You can only deactivate your own listings.")
      }
      docRef.update("active", false).await()
    } catch (e: Exception) {
      throw Exception("Failed to deactivate listing: ${e.message}")
    }
  }

  override suspend fun searchBySkill(skill: Skill): List<Listing> {
    return try {
      val snapshot =
          db.collection(LISTINGS_COLLECTION_PATH)
              .whereEqualTo("skill.skill", skill.skill) // Simple search by skill name
              .whereEqualTo("active", true)
              .get()
              .await()
      snapshot.documents.mapNotNull { it.toListing() }
    } catch (e: Exception) {
      throw Exception("Failed to search by skill: ${e.message}")
    }
  }

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
    // Firestore does not support native geo-queries.
    // This requires a third-party service like Algolia or a complex implementation with Geohashes.
    throw NotImplementedError("Geo-search is not implemented.")
  }

  private fun DocumentSnapshot.toListing(): Listing? {
    if (!exists()) return null
    return try {
      when (getString("type")?.let { ListingType.valueOf(it) }) {
        ListingType.PROPOSAL -> toObject(Proposal::class.java)
        ListingType.REQUEST -> toObject(Request::class.java)
        null -> null // Or throw an exception for unknown types
      }
    } catch (e: IllegalArgumentException) {
      null // Handle cases where the string in DB is not a valid enum
    }
  }
}
