package com.android.sample.utils.fakeRepo.fakeListing

import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import java.util.UUID

class FakeListingEmpty : FakeListingRepo {
  private var lastListingCreated: Listing? = null
  private val listings = mutableListOf<Listing>()

  override fun getNewUid(): String = "listing_${UUID.randomUUID()}"

  override suspend fun getAllListings(): List<Listing> = listings

  override suspend fun getProposals(): List<Proposal> = listings.filterIsInstance<Proposal>()

  override suspend fun getRequests(): List<Request> = listings.filterIsInstance<Request>()

  override suspend fun getListing(listingId: String): Listing? =
      listings.first { listing -> listing.listingId == listingId }

  override suspend fun getListingsByUser(userId: String): List<Listing> =
      listings.filter { it.creatorUserId == userId }

  override suspend fun addProposal(proposal: Proposal) {
    lastListingCreated = proposal
    listings.add(proposal)
  }

  override suspend fun addRequest(request: Request) {
    lastListingCreated = request
    listings.add(request)
  }

  override suspend fun updateListing(listingId: String, listing: Listing) {}

  override suspend fun deleteListing(listingId: String) {}

  override suspend fun deactivateListing(listingId: String) {}

  override suspend fun searchBySkill(skill: Skill): List<Listing> {
    return listings.filter { listing -> listing.skill == skill }
  }

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> {
    return emptyList()
  }

  override fun getLastListingCreated(): Listing? {
    return lastListingCreated
  }
}
