package com.android.sample.utils.fakeRepo.fakeListing

import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill

class FakeListingError : FakeListingRepo {

  override fun getLastListingCreated(): Listing? {
    throw IllegalStateException("Failed to get last listing created (mock error).")
  }

  override fun getNewUid(): String {
    throw IllegalStateException("Failed to generate UID (mock error).")
  }

  override suspend fun getAllListings(): List<Listing> {
    throw IllegalStateException("Failed to load all listings (mock error).")
  }

  override suspend fun getProposals(): List<Proposal> {
    throw IllegalStateException("Failed to load proposals (mock error).")
  }

  override suspend fun getRequests(): List<Request> {
    throw IllegalStateException("Failed to load requests (mock error).")
  }

  override suspend fun getListing(listingId: String): Listing? {
    throw IllegalStateException("Failed to load listing with id: $listingId (mock error).")
  }

  override suspend fun getListingsByUser(userId: String): List<Listing> {
    throw IllegalStateException("Failed to load listings for user: $userId (mock error).")
  }

  override suspend fun addProposal(proposal: Proposal) {
    throw IllegalStateException("Failed to add proposal (mock error).")
  }

  override suspend fun addRequest(request: Request) {
    throw IllegalStateException("Failed to add request (mock error).")
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {
    throw IllegalStateException("Failed to update listing with id: $listingId (mock error).")
  }

  override suspend fun deleteListing(listingId: String) {
    throw IllegalStateException("Failed to delete listing with id: $listingId (mock error).")
  }

  override suspend fun deactivateListing(listingId: String) {
    throw IllegalStateException("Failed to deactivate listing with id: $listingId (mock error).")
  }

  override suspend fun searchBySkill(skill: Skill): List<Listing> {
    throw IllegalStateException("Failed to search listings by skill: $skill (mock error).")
  }

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
    throw IllegalStateException(
        "Failed to search listings by location: $location with radius $radiusKm km (mock error).")
  }
}
