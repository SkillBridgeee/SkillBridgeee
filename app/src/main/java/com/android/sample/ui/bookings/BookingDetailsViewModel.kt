package com.android.sample.ui.bookings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.ListingType
import com.android.sample.model.map.Location
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BkgDetailsUIState(
    val creatorName: String = "",
    val creatorMail: String = "",
    val courseName: String = "",
    val type: ListingType = ListingType.PROPOSAL,
    val location: Location = Location(),
    val description: String = "",
    val hourlyRate: String = "",
    val start: Date = Date(),
    val end: Date = Date(),
    val subject: MainSubject = MainSubject.ACADEMICS,
)

class BookingDetailsViewModel(
    private val bookingRepository: BookingRepository = BookingRepositoryProvider.repository,
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(BkgDetailsUIState())
  // Public read-only state flow for the UI to observe
  val uiState: StateFlow<BkgDetailsUIState> = _uiState.asStateFlow()

  fun load(bookingId: String) {
    viewModelScope.launch {
      try {
        val booking = bookingRepository.getBooking(bookingId)

        if (booking == null) {
          updateUiStateFromData(bookingId, null, null, null)
          return@launch
        }

        val creatorId = booking.listingCreatorId
        val listingId = booking.associatedListingId

        val creatorProfile = profileRepository.getProfile(creatorId)
        val listing = listingRepository.getListing(listingId)

        updateUiStateFromData(bookingId = bookingId, booking, listing, creatorProfile)
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error loading booking details for $bookingId", e)
      }
    }
  }

  private fun updateUiStateFromData(
      bookingId: String,
      booking: Booking?,
      listing: Listing?,
      creatorProfile: Profile?
  ) {

    val newState =
        if (booking != null && listing != null && creatorProfile != null) {
          BkgDetailsUIState(
              creatorName = creatorProfile.name!!,
              creatorMail = creatorProfile.email,
              courseName = listing.skill.skill,
              type = listing.type,
              location = listing.location,
              description = listing.description,
              hourlyRate = listing.hourlyRate.toString(),
              start = booking.sessionStart,
              end = booking.sessionEnd,
              subject = listing.skill.mainSubject,
          )
        } else {
          Log.e("BookingDetailsViewModel", "Booking or Listing not found for ID: $bookingId")
        }
    _uiState.value = newState as BkgDetailsUIState
  }
}
