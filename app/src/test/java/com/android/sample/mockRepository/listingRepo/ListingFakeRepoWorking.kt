package com.android.sample.mockRepository.listingRepo

import com.android.sample.model.listing.*
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import java.util.*

/**
 * A fake implementation of [ListingRepository] that provides a predefined set of listings.
 *
 * This mock repository is used for testing and development purposes, simulating a repository with
 * actual proposal and request listings without requiring a real backend.
 *
 * Features:
 * - Contains two initial listings: one Proposal and one Request.
 * - Supports adding, updating, deleting, and deactivating listings.
 * - Supports simple search by skill or location (mock implementation).
 * - Returns copies or filtered lists to avoid external mutation.
 *
 * Typical use cases:
 * - Verifying ViewModel or UseCase logic when listings exist.
 * - Testing UI rendering of proposals and requests.
 * - Simulating user actions such as adding or deactivating listings.
 */
class ListingFakeRepoWorking : ListingRepository {

  private val listings =
      mutableMapOf<String, Listing>(
          "listing_1" to
              Proposal(
                  listingId = "listing_1",
                  creatorUserId = "creator_1",
                  skill = Skill(skill = "Math"),
                  description = "Tutor proposal",
                  location = Location(),
                  createdAt = Date(),
                  hourlyRate = 30.0),
          "listing_2" to
              Request(
                  listingId = "listing_2",
                  creatorUserId = "creator_2",
                  skill = Skill(skill = "Physics"),
                  description = "Student request",
                  location = Location(),
                  createdAt = Date(),
                  hourlyRate = 45.0))

  override fun getNewUid(): String = "listing_${UUID.randomUUID()}"

  override suspend fun getAllListings(): List<Listing> = listings.values.toList()

  override suspend fun getProposals(): List<Proposal> = listings.values.filterIsInstance<Proposal>()

  override suspend fun getRequests(): List<Request> = listings.values.filterIsInstance<Request>()

  override suspend fun getListing(listingId: String): Listing? = listings[listingId]

  override suspend fun getListingsByUser(userId: String): List<Listing> =
      listings.values.filter { it.creatorUserId == userId }

  override suspend fun addProposal(proposal: Proposal) {
    listings[proposal.listingId.ifBlank { getNewUid() }] = proposal
  }

  override suspend fun addRequest(request: Request) {
    listings[request.listingId.ifBlank { getNewUid() }] = request
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    if (!listings.containsKey(listingId)) {
      throw IllegalArgumentException("Listing not found: $listingId")
    }
    listings[listingId] = listing
  }

  override suspend fun deleteListing(listingId: String) {
    if (listings.remove(listingId) == null) {
      throw IllegalArgumentException("Listing not found: $listingId")
    }
  }

  override suspend fun deleteAllListingOfUser(userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun deactivateListing(listingId: String) {
    val listing = listings[listingId]
    if (listing == null) {
      throw IllegalArgumentException("Listing not found: $listingId")
    } else {
      val updatedListing =
          when (listing) {
            is Proposal -> listing.copy(isActive = false)
            is Request -> listing.copy(isActive = false)
          }
      listings[listingId] = updatedListing
    }
  }

  override suspend fun searchBySkill(skill: Skill): List<Listing> =
      listings.values.filter {
        it.skill.skill.contains(skill.skill, ignoreCase = true) ||
            it.skill.mainSubject.name.contains(skill.skill, ignoreCase = true)
      }

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
    // Simulation simplifiée : renvoie toutes les listings ayant une location non vide
    return listings.values.filter { it.location.name == location.name }
  }
}
