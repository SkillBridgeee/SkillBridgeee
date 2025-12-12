package com.android.sample.model.listing

interface ListingRepository {
  fun getNewUid(): String

  suspend fun getAllListings(): List<Listing>

  suspend fun getProposals(): List<Proposal>

  suspend fun getRequests(): List<Request>

  suspend fun getListing(listingId: String): Listing?

  suspend fun getListingsByUser(userId: String): List<Listing>

  suspend fun addProposal(proposal: Proposal)

  suspend fun addRequest(request: Request)

  suspend fun updateListing(listingId: String, listing: Listing)

  suspend fun deleteListing(listingId: String)

  suspend fun deleteAllListingOfUser(userId: String)

  /** Deactivates a listing */
  suspend fun deactivateListing(listingId: String)

  /** Searches listings by skill type */
  suspend fun searchBySkill(skill: com.android.sample.model.skill.Skill): List<Listing>

  /** Searches listings by location proximity */
  suspend fun searchByLocation(
      location: com.android.sample.model.map.Location,
      radiusKm: Double
  ): List<Listing>
}
