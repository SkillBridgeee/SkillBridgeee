package com.android.sample.mockRepository.listingRepo

import com.android.sample.model.listing.*
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill

/**
 * A fake implementation of [ListingRepository] that always throws exceptions.
 *
 * This mock repository is used for testing how the application handles errors when interacting with
 * listing-related data sources.
 *
 * Each method in this class intentionally throws a descriptive exception to simulate various
 * failure scenarios such as:
 * - Network failures
 * - Database access issues
 * - Invalid input or missing data
 *
 * Typical use cases:
 * - Verifying ViewModel or UseCase error handling logic.
 * - Ensuring the UI reacts correctly to repository failures (e.g., displaying error messages, retry
 *   buttons, or fallback states).
 * - Testing resilience and recovery flows in the app.
 */
class ListingFakeRepoError : ListingRepository {

  override fun getNewUid(): String {
    throw IllegalStateException("Failed to generate new listing UID")
  }

  override suspend fun getAllListings(): List<Listing> {
    throw RuntimeException("Error fetching all listings")
  }

  override suspend fun getProposals(): List<Proposal> {
    throw RuntimeException("Error fetching proposals")
  }

  override suspend fun getRequests(): List<Request> {
    throw RuntimeException("Error fetching requests")
  }

  override suspend fun getListing(listingId: String): Listing? {
    throw IllegalArgumentException("Error fetching listing with id: $listingId")
  }

  override suspend fun getListingsByUser(userId: String): List<Listing> {
    throw RuntimeException("Error fetching listings for user: $userId")
  }

  override suspend fun addProposal(proposal: Proposal) {
    throw UnsupportedOperationException("Error adding proposal: ${proposal.listingId}")
  }

  override suspend fun addRequest(request: Request) {
    throw UnsupportedOperationException("Error adding request: ${request.listingId}")
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    throw IllegalStateException("Error updating listing with id: $listingId")
  }

  override suspend fun deleteListing(listingId: String) {
    throw IllegalStateException("Error deleting listing with id: $listingId")
  }

  override suspend fun deleteAllListingOfUser(userId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun deactivateListing(listingId: String) {
    throw IllegalStateException("Error deactivating listing with id: $listingId")
  }

  override suspend fun searchBySkill(skill: Skill): List<Listing> {
    throw RuntimeException("Error searching listings by skill: ${skill.skill}")
  }

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
    throw RuntimeException("Error searching listings by location: $location within ${radiusKm}km")
  }
}
