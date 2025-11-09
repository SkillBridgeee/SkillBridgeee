package com.android.sample.ui.bookings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingCardUIV2(val booking: Booking, val creatorProfile: Profile, val listing: Listing)

/**
 * Minimal VM:
 * - uiState is just the final list of cards
 * - init calls load()
 * - load() loops bookings and pulls listing/profile/rating to build each card
 */
class MyBookingsViewModel(
    private val bookingRepo: BookingRepository = BookingRepositoryProvider.repository,
    private val listingRepo: ListingRepository = ListingRepositoryProvider.repository,
    private val profileRepo: ProfileRepository = ProfileRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow<List<BookingCardUIV2>>(emptyList())
  val uiState: StateFlow<List<BookingCardUIV2>> = _uiState.asStateFlow()

  private val userId = UserSessionManager.getCurrentUserId()!!

  init {
    load()
  }

  fun load() {
    viewModelScope.launch {
      try {
        // Get all the bookings of the user
        val allUsersBooking = bookingRepo.getBookingsByUserId(userId)
        if (allUsersBooking.isEmpty()) {
          _uiState.value = emptyList()
          return@launch
        }

        // Load Profile of the listingCreator (no duplication)
        val creatorProfileCache = getCreatorProfilesCache(allUsersBooking)
        // Load all the listing of the bookings
        val listingCache = getAssociatedListingsCache(allUsersBooking)

        //
        val bookingsWithProfiles =
            buildBookingsWithData(allUsersBooking, creatorProfileCache, listingCache)

        _uiState.value = bookingsWithProfiles
      } catch (e: Exception) {
        Log.e("BookingsListViewModel", "Error loading user bookings for $userId", e)
        _uiState.value = emptyList()
      }
    }
  }

  private suspend fun getCreatorProfilesCache(bookings: List<Booking>): Map<String, Profile> {
    val uniqueCreatorIds: Set<String> = bookings.map { it.listingCreatorId }.toSet()
    val creatorProfileCache: MutableMap<String, Profile> = mutableMapOf()

    for (creatorId in uniqueCreatorIds) {
      profileRepo.getProfile(creatorId)?.let { profile -> creatorProfileCache[creatorId] = profile }
    }
    return creatorProfileCache
  }

  private suspend fun getAssociatedListingsCache(bookings: List<Booking>): Map<String, Listing> {
    val uniqueListingIds: Set<String> = bookings.map { it.associatedListingId }.toSet()
    val listingCache: MutableMap<String, Listing> = mutableMapOf()

    for (listingId in uniqueListingIds) {
      listingRepo.getListing(listingId)?.let { listing -> listingCache[listingId] = listing }
    }
    return listingCache
  }

  // --- Sous-Méthode 3 : Mapper Booking + Profile + Listing ---
  private fun buildBookingsWithData(
      bookings: List<Booking>,
      profileCache: Map<String, Profile>,
      listingCache: Map<String, Listing>
  ): List<BookingCardUIV2> {
    return bookings.mapNotNull { booking ->
      val creatorProfile = profileCache[booking.listingCreatorId]
      val associatedListing = listingCache[booking.associatedListingId]

      // On ne retourne l'objet que si toutes les données requises sont présentes
      if (creatorProfile != null && associatedListing != null) {
        BookingCardUIV2(
            booking = booking, creatorProfile = creatorProfile, listing = associatedListing)
      } else {
        // Loguer si un élément est manquant pour le débogage
        Log.w("BookingsListViewModel", "Missing data for booking: ${booking.bookingId}")
        null
      }
    }
  }
}
