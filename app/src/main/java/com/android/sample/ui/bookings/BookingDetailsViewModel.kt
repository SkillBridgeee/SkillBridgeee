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
import com.android.sample.model.listing.Proposal
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
    val creatorId: String = "",
    val courseName: String = "",
    val type: ListingType = ListingType.PROPOSAL,
    val location: Location = Location(),
    val description: String = "",
    val hourlyRate: String = "",
    val start: Date = Date(),
    val end: Date = Date(),
    val subject: MainSubject = MainSubject.ACADEMICS,
)

data class BookingUIState(
    val booking: Booking = Booking(),
    val listing: Listing = Proposal(),
    val creatorProfile: Profile = Profile(),
    val loadError: Boolean = false
)

class BookingDetailsViewModel(
    private val bookingRepository: BookingRepository = BookingRepositoryProvider.repository,
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
) : ViewModel() {

  private val _bookingUiState = MutableStateFlow(BookingUIState())
  // Public read-only state flow for the UI to observe
  val bookingUiState: StateFlow<BookingUIState> = _bookingUiState.asStateFlow()

  fun load(bookingId: String) {
    viewModelScope.launch {
      try {
        val booking1 = bookingRepository.getBooking(bookingId)
        val creatorProfile1 = profileRepository.getProfile(booking1!!.listingCreatorId)
        val listing1 = listingRepository.getListing(booking1.associatedListingId)

        _bookingUiState.value =
            bookingUiState.value.copy(
                booking = booking1,
                listing = listing1!!,
                creatorProfile = creatorProfile1!!,
                loadError = false)
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error loading booking details for $bookingId", e)
        bookingUiState.value.copy(loadError = true)
      }
    }
  }
}
