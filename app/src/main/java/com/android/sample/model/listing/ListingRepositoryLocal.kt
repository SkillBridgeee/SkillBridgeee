package com.android.sample.model.listing

import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill

class ListingRepositoryLocal : ListingRepository {
  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun getAllListings(): List<Listing> {
    TODO("Not yet implemented")
  }

  override suspend fun getProposals(): List<Proposal> {
    TODO("Not yet implemented")
  }

  override suspend fun getRequests(): List<Request> {
    TODO("Not yet implemented")
  }

  override suspend fun getListing(listingId: String): Listing {
    TODO("Not yet implemented")
  }

  override suspend fun getListingsByUser(userId: String): List<Listing> {
    TODO("Not yet implemented")
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
