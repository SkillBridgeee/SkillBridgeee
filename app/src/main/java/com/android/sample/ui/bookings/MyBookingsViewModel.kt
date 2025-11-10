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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyBookingsUIState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val bookings: List<BookingCardUI> = emptyList()
)

data class BookingCardUI(val booking: Booking, val creatorProfile: Profile, val listing: Listing)

/**
 * Minimal VM:
 * - uiState is just the final list of cards
 * - init calls load()
 * - load() loops bookings and pulls listing/profile/rating to build each card
 */
class MyBookingsViewModel(
    private val bookingRepo: BookingRepository = BookingRepositoryProvider.repository,
    val listingRepo: ListingRepository = ListingRepositoryProvider.repository,
    val profileRepo: ProfileRepository = ProfileRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(MyBookingsUIState())
  val uiState = _uiState.asStateFlow()

  init {
    load()
  }

  fun load() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, hasError = false) }
      try {

        val userId = runCatching { UserSessionManager.getCurrentUserId() }.getOrNull().orEmpty()

        // Get all the bookings of the user
        val allUsersBooking = bookingRepo.getBookingsByUserId(userId)
        if (allUsersBooking.isEmpty()) {
          _uiState.update { it.copy(isLoading = false, hasError = false, bookings = emptyList()) }
          return@launch
        }

        // Load Profile of the listingCreator (no duplication)
        val creatorProfileCache = getCreatorProfilesCache(allUsersBooking)
        // Load all the listing of the bookings
        val listingCache = getAssociatedListingsCache(allUsersBooking)

        // Match the profile to the booking
        val bookingsWithProfiles =
            buildBookingsWithData(allUsersBooking, creatorProfileCache, listingCache)

        _uiState.update {
          it.copy(isLoading = false, hasError = false, bookings = bookingsWithProfiles)
        }
      } catch (e: Exception) {
        Log.e("BookingsListViewModel", "Error loading user bookings", e)
        _uiState.update { it.copy(isLoading = false, hasError = true, bookings = emptyList()) }
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

  private fun buildBookingsWithData(
      bookings: List<Booking>,
      profileCache: Map<String, Profile>,
      listingCache: Map<String, Listing>
  ): List<BookingCardUI> {
    return bookings.mapNotNull { booking ->
      val creatorProfile = profileCache[booking.listingCreatorId]
      val associatedListing = listingCache[booking.associatedListingId]

      if (creatorProfile != null && associatedListing != null) {
        BookingCardUI(
            booking = booking, creatorProfile = creatorProfile, listing = associatedListing)
      } else {
        Log.w("BookingsListViewModel", "Missing data for booking: ${booking.bookingId}")
        null
      }
    }
  }
}
