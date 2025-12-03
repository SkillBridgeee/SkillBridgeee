package com.android.sample.model.listing

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.android.sample.model.ValidationUtils
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID
import kotlinx.coroutines.tasks.await

const val LISTINGS_COLLECTION_PATH = "listings"

class FirestoreListingRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val context: Context
) : ListingRepository {

  private companion object {
    private const val DESC_MAX = 2000
    private const val HOURLY_RATE_MIN = 0.0
    private const val HOURLY_RATE_MAX = 200.0
  }

  private val currentUserId: String
    get() = auth.currentUser?.uid ?: throw Exception("User not authenticated")

  fun isOnline(): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)

    return capabilities != null &&
        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
  }

  override fun getNewUid(): String {
    return UUID.randomUUID().toString()
  }

  override suspend fun getAllListings(): List<Listing> {
    return try {
      val snapshot = db.collection(LISTINGS_COLLECTION_PATH).get().await()
      snapshot.documents.mapNotNull { it.toListing() }
    } catch (e: Exception) {

      throw Exception("Failed to fetch all listings: ${e.message}")
    }
  }

  override suspend fun getProposals(): List<Proposal> {
    return try {
      val snapshot =
          db.collection(LISTINGS_COLLECTION_PATH)
              .whereEqualTo("type", ListingType.PROPOSAL.name)
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
              .whereEqualTo("type", ListingType.REQUEST.name)
              .get()
              .await()
      snapshot.toObjects(Request::class.java)
    } catch (e: Exception) {
      throw Exception("Failed to fetch requests: ${e.message}")
    }
  }

  override suspend fun getListing(listingId: String): Listing? {
    return try {
      val document = db.collection(LISTINGS_COLLECTION_PATH).document(listingId).get().await()
      document.toListing()
    } catch (e: Exception) {
      // Return null if listing not found or another error occurs
      null
    }
  }

  override suspend fun getListingsByUser(userId: String): List<Listing> {
    return try {
      val snapshot =
          db.collection(LISTINGS_COLLECTION_PATH)
              .whereEqualTo("creatorUserId", userId)
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

  @SuppressLint("ServiceCast")
  private suspend fun addListing(listing: Listing) {
    try {
      validateForWrite(listing)

      if (listing.creatorUserId != currentUserId) {
        throw Exception("Access denied: You can only create listings for yourself.")
      }
      if (isOnline()) {
        db.collection(LISTINGS_COLLECTION_PATH).document(listing.listingId).set(listing).await()
      } else {
        db.collection(LISTINGS_COLLECTION_PATH).document(listing.listingId).set(listing)
      }
    } catch (e: Exception) {
      if (isOnline()) {
        throw Exception("Failed to add listing: ${e.message}")
      }
    }
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    try {
      validateForWrite(listing)

      val docRef = db.collection(LISTINGS_COLLECTION_PATH).document(listingId)
      val existingListing = getListing(listingId) ?: throw Exception("Listing not found.")

      if (existingListing.creatorUserId != currentUserId) {
        throw Exception("Access denied: You can only update your own listings.")
      }
      if (isOnline()) {
        docRef.set(listing).await()
      } else {
        docRef.set(listing)
      }
    } catch (e: Exception) {
      throw Exception("Failed to update listing: ${e.message}")
    }
  }

  override suspend fun deleteListing(listingId: String) {
    try {
      val docRef = db.collection(LISTINGS_COLLECTION_PATH).document(listingId)
      val existingListing = getListing(listingId) ?: throw Exception("Listing not found.")

      if (existingListing.creatorUserId != currentUserId) {
        throw Exception("Access denied: You can only delete your own listings.")
      }
      if (isOnline()) {
        docRef.delete().await()
      } else {
        docRef.delete()
      }
    } catch (e: Exception) {
      throw Exception("Failed to delete listing: ${e.message}")
    }
  }

  override suspend fun deactivateListing(listingId: String) {
    try {
      val docRef = db.collection(LISTINGS_COLLECTION_PATH).document(listingId)
      val existingListing = getListing(listingId) ?: throw Exception("Listing not found.")

      if (existingListing.creatorUserId != currentUserId) {
        throw Exception("Access denied: You can only deactivate your own listings.")
      }

      if (isOnline()) {
        docRef.update("isActive", false).await()
      } else {
        docRef.update("isActive", false)
      }
    } catch (e: Exception) {
      throw Exception("Failed to deactivate listing: ${e.message}")
    }
  }

  override suspend fun searchBySkill(skill: Skill): List<Listing> {
    return try {
      val snapshot =
          db.collection(LISTINGS_COLLECTION_PATH)
              .whereEqualTo("skill.skill", skill.skill)
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

  private fun validateForWrite(l: Listing) {
    // ids
    ValidationUtils.requireId(l.listingId, "listingId")
    ValidationUtils.requireId(l.creatorUserId, "creatorUserId")

    // description (required + max)
    ValidationUtils.requireNonBlank(l.description, "description")
    ValidationUtils.requireMaxLength(l.description, "description", DESC_MAX)

    // hourly rate
    require(l.hourlyRate in HOURLY_RATE_MIN..HOURLY_RATE_MAX) {
      "hourlyRate must be between $HOURLY_RATE_MIN and $HOURLY_RATE_MAX."
    }
  }
}
