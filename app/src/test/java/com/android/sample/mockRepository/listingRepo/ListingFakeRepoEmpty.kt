package com.android.sample.mockRepository.listingRepo

import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill

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
