package com.android.sample.mockRepository.listingRepo

import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill

/**
 * A fake implementation of [ListingRepository] that returns empty data for all queries.
 *
 * This mock repository is used for testing how the application behaves when there are no listings
 * available.
 *
 * Each method either returns an empty list or null, simulating the case where the data source
 * contains no listings, proposals, or requests.
 *
 * Typical use cases:
 * - Verifying ViewModel or UseCase logic when no data is present.
 * - Ensuring the UI correctly displays empty states (e.g., empty lists, messages).
 * - Testing fallback behavior or default states in the absence of listings.
 */
class ListingFakeRepoEmpty : ListingRepository {
  override fun getNewUid(): String {
    return ""
  }

  override suspend fun getAllListings(): List<Listing> {
    return emptyList()
  }

  override suspend fun getProposals(): List<Proposal> {
    return emptyList()
  }

  override suspend fun getRequests(): List<Request> {
    return emptyList()
  }

  override suspend fun getListing(listingId: String): Listing? {
    return null
  }

  override suspend fun getListingsByUser(userId: String): List<Listing> {
    return emptyList()
  }

  override suspend fun addProposal(proposal: Proposal) {
    TODO("Not yet implemented")
  }

  override suspend fun addRequest(request: Request) {
    TODO("Not yet implemented")
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteListing(listingId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun deactivateListing(listingId: String) {
    TODO("Not yet implemented")
  }

  override suspend fun searchBySkill(skill: Skill): List<Listing> {
    TODO("Not yet implemented")
  }

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
    TODO("Not yet implemented")
  }
}
