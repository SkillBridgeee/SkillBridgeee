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

  override suspend fun getAllListings(): List<Listing> = listings.toList()

  override suspend fun getProposals(): List<Proposal> = listings.filterIsInstance<Proposal>()

  override suspend fun getRequests(): List<Request> = listings.filterIsInstance<Request>()

  override suspend fun getListing(listingId: String): Listing? =
      listings.find { listing -> listing.listingId == listingId }

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

  override suspend fun updateListing(listingId: String, listing: Listing) {
    val index = listings.indexOfFirst { it.listingId == listingId }
    if (index != -1) {
      listings[index] = listing
    }
  }

  override suspend fun deleteListing(listingId: String) {
    listings.removeAll { it.listingId == listingId }
  }

  override suspend fun deactivateListing(listingId: String) {
    val index = listings.indexOfFirst { it.listingId == listingId }
    if (index == -1) return

    val old = listings[index]

    val newListing: Listing =
        when (old) {
          is Proposal ->
              Proposal(
                  listingId = old.listingId,
                  creatorUserId = old.creatorUserId,
                  skill = old.skill,
                  title = old.title,
                  description = old.description,
                  location = old.location,
                  createdAt = old.createdAt,
                  isActive = false,
                  hourlyRate = old.hourlyRate,
                  type = old.type)
          is Request ->
              Request(
                  listingId = old.listingId,
                  creatorUserId = old.creatorUserId,
                  skill = old.skill,
                  title = old.title,
                  description = old.description,
                  location = old.location,
                  createdAt = old.createdAt,
                  isActive = false,
                  hourlyRate = old.hourlyRate,
                  type = old.type)
        }

    listings[index] = newListing
  }

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
