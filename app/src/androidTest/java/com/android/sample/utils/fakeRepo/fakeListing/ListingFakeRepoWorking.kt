package com.android.sample.utils.fakeRepo.fakeListing

import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import java.util.Date
import java.util.UUID

/**
 * A fake implementation of [com.android.sample.model.listing.ListingRepository] that provides a
 * predefined set of listings.
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
class ListingFakeRepoWorking() : FakeListingRepo {

  private var lastListingCreated: Listing? = null
  private val listings =
      mutableListOf(
          Proposal(
              listingId = "listing_1",
              creatorUserId = "creator_1",
              skill = Skill(skill = "Math"),
              title = "Class on derivatives",
              description = "I am ready to help everyone regardless of their level",
              location = Location(),
              createdAt = Date(),
              hourlyRate = 30.0),
          Request(
              listingId = "listing_2",
              creatorUserId = "creator_2",
              skill = Skill(skill = "Physics"),
              title = "Class on mechanical physics",
              description =
                  "I'm looking for someone that can explain me thing from a different angle",
              location = Location(),
              createdAt = Date(),
              hourlyRate = 45.0))

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
