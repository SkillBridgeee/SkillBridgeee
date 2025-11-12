package com.android.sample.mockRepository.listingRepo

import com.android.sample.model.listing.*
import com.android.sample.model.map.Location
import com.android.sample.model.skill.Skill
import java.util.*

class FakeListingRepoForBookings : ListingRepository {

  // Créons directement les listings correspondant aux bookings
  private val listings: Map<String, Listing> =
      mapOf(
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

  override suspend fun addProposal(proposal: Proposal) {}

  override suspend fun addRequest(request: Request) {}

  override suspend fun updateListing(listingId: String, listing: Listing) {}

  override suspend fun deleteListing(listingId: String) {}

  override suspend fun deactivateListing(listingId: String) {}

  override suspend fun searchBySkill(skill: Skill): List<Listing> = emptyList()

  override suspend fun searchByLocation(location: Location, radiusKm: Double): List<Listing> =
      emptyList()
}
