package com.android.sample.model.listing

import android.util.Log
import com.android.sample.model.map.Location
import com.android.sample.model.skill.ExpertiseLevel
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.skill.Skill
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlinx.coroutines.tasks.await

const val LISTINGS_COLLECTION_PATH = "listings"

class ListingRepositoryFirestore(private val db: FirebaseFirestore) : ListingRepository {

  override fun getNewUid(): String {
    return db.collection(LISTINGS_COLLECTION_PATH).document().id
  }

  override suspend fun getAllListings(): List<Listing> {
    val snapshot = db.collection(LISTINGS_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { documentToListing(it) }
  }

  override suspend fun getProposals(): List<Proposal> {
    val snapshot =
        db.collection(LISTINGS_COLLECTION_PATH).whereEqualTo("type", "PROPOSAL").get().await()
    return snapshot.mapNotNull { documentToListing(it) as? Proposal }
  }

  override suspend fun getRequests(): List<Request> {
    val snapshot =
        db.collection(LISTINGS_COLLECTION_PATH).whereEqualTo("type", "REQUEST").get().await()
    return snapshot.mapNotNull { documentToListing(it) as? Request }
  }

  override suspend fun getListing(listingId: String): Listing {
    val document = db.collection(LISTINGS_COLLECTION_PATH).document(listingId).get().await()
    return documentToListing(document)
        ?: throw Exception("ListingRepositoryFirestore: Listing not found")
  }

  override suspend fun getListingsByUser(userId: String): List<Listing> {
    val snapshot =
        db.collection(LISTINGS_COLLECTION_PATH).whereEqualTo("userId", userId).get().await()
    return snapshot.mapNotNull { documentToListing(it) }
  }

  override suspend fun addProposal(proposal: Proposal) {
    val data = proposal.toMap().plus("type" to "PROPOSAL")
    db.collection(LISTINGS_COLLECTION_PATH).document(proposal.listingId).set(data).await()
  }

  override suspend fun addRequest(request: Request) {
    val data = request.toMap().plus("type" to "REQUEST")
    db.collection(LISTINGS_COLLECTION_PATH).document(request.listingId).set(data).await()
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    val data =
        when (listing) {
          is Proposal -> listing.toMap().plus("type" to "PROPOSAL")
          is Request -> listing.toMap().plus("type" to "REQUEST")
        }
    db.collection(LISTINGS_COLLECTION_PATH).document(listingId).set(data).await()
  }

  override suspend fun deleteListing(listingId: String) {
    db.collection(LISTINGS_COLLECTION_PATH).document(listingId).delete().await()
  }

  override suspend fun deactivateListing(listingId: String) {
    db.collection(LISTINGS_COLLECTION_PATH).document(listingId).update("isActive", false).await()
  }

  override suspend fun searchBySkill(skill: Skill): List<Listing> {
    val snapshot = db.collection(LISTINGS_COLLECTION_PATH).get().await()
    return snapshot.mapNotNull { documentToListing(it) }.filter { it.skill == skill }
  }

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
    val snapshot = db.collection(LISTINGS_COLLECTION_PATH).get().await()
    return snapshot
        .mapNotNull { documentToListing(it) }
        .filter { listing -> calculateDistance(location, listing.location) <= radiusKm }
  }

  private fun documentToListing(document: DocumentSnapshot): Listing? {
    return try {
      val type = document.getString("type") ?: return null

      when (type) {
        "PROPOSAL" -> documentToProposal(document)
        "REQUEST" -> documentToRequest(document)
        else -> null
      }
    } catch (e: Exception) {
      Log.e("ListingRepositoryFirestore", "Error converting document to Listing", e)
      null
    }
  }

  private fun documentToProposal(document: DocumentSnapshot): Proposal? {
    val listingId = document.id
    val userId = document.getString("userId") ?: return null
    val userName = document.getString("userName") ?: return null
    val skillData = document.get("skill") as? Map<*, *>
    val skill =
        skillData?.let {
          val mainSubjectStr = it["mainSubject"] as? String ?: return null
          val skillStr = it["skill"] as? String ?: return null
          val skillTime = it["skillTime"] as? Double ?: 0.0
          val expertiseStr = it["expertise"] as? String ?: "BEGINNER"

          Skill(
              userId = userId,
              mainSubject = MainSubject.valueOf(mainSubjectStr),
              skill = skillStr,
              skillTime = skillTime,
              expertise = ExpertiseLevel.valueOf(expertiseStr))
        } ?: return null

    val description = document.getString("description") ?: return null
    val locationData = document.get("location") as? Map<*, *>
    val location =
        locationData?.let {
          Location(
              latitude = it["latitude"] as? Double ?: 0.0,
              longitude = it["longitude"] as? Double ?: 0.0,
              name = it["name"] as? String ?: "")
        } ?: Location()

    val createdAt = document.getTimestamp("createdAt")?.toDate() ?: Date()
    val isActive = document.getBoolean("isActive") ?: true
    val hourlyRate = document.getDouble("hourlyRate") ?: 0.0

    return Proposal(
        listingId = listingId,
        userId = userId,
        userName = userName,
        skill = skill,
        description = description,
        location = location,
        createdAt = createdAt,
        isActive = isActive,
        hourlyRate = hourlyRate)
  }

  private fun documentToRequest(document: DocumentSnapshot): Request? {
    val listingId = document.id
    val userId = document.getString("userId") ?: return null
    val userName = document.getString("userName") ?: return null
    val skillData = document.get("skill") as? Map<*, *>
    val skill =
        skillData?.let {
          val mainSubjectStr = it["mainSubject"] as? String ?: return null
          val skillStr = it["skill"] as? String ?: return null
          val skillTime = it["skillTime"] as? Double ?: 0.0
          val expertiseStr = it["expertise"] as? String ?: "BEGINNER"

          Skill(
              userId = userId,
              mainSubject = MainSubject.valueOf(mainSubjectStr),
              skill = skillStr,
              skillTime = skillTime,
              expertise = ExpertiseLevel.valueOf(expertiseStr))
        } ?: return null

    val description = document.getString("description") ?: return null
    val locationData = document.get("location") as? Map<*, *>
    val location =
        locationData?.let {
          Location(
              latitude = it["latitude"] as? Double ?: 0.0,
              longitude = it["longitude"] as? Double ?: 0.0,
              name = it["name"] as? String ?: "")
        } ?: Location()

    val createdAt = document.getTimestamp("createdAt")?.toDate() ?: Date()
    val isActive = document.getBoolean("isActive") ?: true
    val maxBudget = document.getDouble("maxBudget") ?: 0.0

    return Request(
        listingId = listingId,
        userId = userId,
        userName = userName,
        skill = skill,
        description = description,
        location = location,
        createdAt = createdAt,
        isActive = isActive,
        maxBudget = maxBudget)
  }

  private fun Proposal.toMap(): Map<String, Any?> {
    return mapOf(
        "userId" to userId,
        "userName" to userName,
        "skill" to
            mapOf(
                "mainSubject" to skill.mainSubject.name,
                "skill" to skill.skill,
                "skillTime" to skill.skillTime,
                "expertise" to skill.expertise.name),
        "description" to description,
        "location" to
            mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "name" to location.name),
        "createdAt" to createdAt,
        "isActive" to isActive,
        "hourlyRate" to hourlyRate)
  }

  private fun Request.toMap(): Map<String, Any?> {
    return mapOf(
        "userId" to userId,
        "userName" to userName,
        "skill" to
            mapOf(
                "mainSubject" to skill.mainSubject.name,
                "skill" to skill.skill,
                "skillTime" to skill.skillTime,
                "expertise" to skill.expertise.name),
        "description" to description,
        "location" to
            mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "name" to location.name),
        "createdAt" to createdAt,
        "isActive" to isActive,
        "maxBudget" to maxBudget)
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
