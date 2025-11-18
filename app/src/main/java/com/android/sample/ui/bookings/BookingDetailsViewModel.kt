package com.android.sample.ui.bookings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.rating.RatingType
import com.android.sample.model.rating.StarRating
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
  // New: rating repository, obtained from provider (no constructor change needed)
  private val ratingRepository: RatingRepository = RatingRepositoryProvider.repository

  // Public read-only state flow for the UI to observe
  val bookingUiState: StateFlow<BookingUIState> = _bookingUiState.asStateFlow()

  fun load(bookingId: String) {
    viewModelScope.launch {
      try {
        val booking =
            bookingRepository.getBooking(bookingId)
                ?: throw IllegalStateException(
                    "BookingDetailsViewModel : Booking not found for id=$bookingId")

        val creatorProfile =
            profileRepository.getProfile(booking.listingCreatorId)
                ?: throw IllegalStateException(
                    "BookingDetailsViewModel : Creator profile not found")

        val listing =
            listingRepository.getListing(booking.associatedListingId)
                ?: throw IllegalStateException("BookingDetailsViewModel : Listing not found")

        _bookingUiState.value =
            bookingUiState.value.copy(
                booking = booking,
                listing = listing,
                creatorProfile = creatorProfile,
                loadError = false)
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error loading booking details for $bookingId", e)
        _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      }
    }
  }

  /**
   * Marks the currently loaded booking as completed and updates the UI state.
   * - This function attempts to update the booking status in the `BookingRepository` to
   *   `COMPLETED`. If the operation succeeds, the method fetches the updated booking from the
   *   repository so that the UI reflects the new status.
   * - If an error occurs (e.g., network or Firestore failure), the UI state is updated with
   *   `loadError = true`, allowing the UI layer to display an appropriate error message.
   * - This function does nothing if no valid booking ID is currently loaded.
   */
  fun markBookingAsCompleted() {
    val currentBookingId = bookingUiState.value.booking.bookingId
    if (currentBookingId.isBlank()) return

    viewModelScope.launch {
      try {
        bookingRepository.completeBooking(currentBookingId)

        // Refresh the booking from Firestore so UI gets the new status
        val updatedBooking = bookingRepository.getBooking(currentBookingId)
        if (updatedBooking != null) {
          _bookingUiState.value =
              bookingUiState.value.copy(booking = updatedBooking, loadError = false)
        }
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error completing booking $currentBookingId", e)
        _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      }
    }
  }

  fun submitStudentRatings(tutorStars: Int, listingStars: Int) {
    val booking = bookingUiState.value.booking

    // No booking loaded or not completed -> do nothing
    if (booking.bookingId.isBlank()) return
    if (booking.status != BookingStatus.COMPLETED) return

    val tutorRatingEnum = tutorStars.toStarRating()
    val listingRatingEnum = listingStars.toStarRating()

    viewModelScope.launch {
      try {
        // Student = booker, Tutor = listing creator
        val fromUserId = booking.bookerId // person giving the rating
        val tutorUserId = booking.listingCreatorId // person receiving tutor + listing rating

        // 1) Student rates the tutor
        val tutorRating =
            Rating(
                ratingId = ratingRepository.getNewUid(),
                fromUserId = fromUserId,
                toUserId = tutorUserId,
                starRating = tutorRatingEnum,
                comment = "",
                ratingType = RatingType.TUTOR,
                targetObjectId = tutorUserId,
            )

        // 2) Student rates the listing
        val listingRating =
            Rating(
                ratingId = ratingRepository.getNewUid(),
                fromUserId = fromUserId,
                toUserId = tutorUserId,
                starRating = listingRatingEnum,
                comment = "",
                ratingType = RatingType.LISTING,
                targetObjectId = booking.associatedListingId,
            )

        tutorRating.validate()
        listingRating.validate()

        ratingRepository.addRating(tutorRating)
        ratingRepository.addRating(listingRating)

        // optional: you could add a flag in UI state to hide rating UI after submit
        // _bookingUiState.value = bookingUiState.value.copy(ratingSubmitted = true)

      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error submitting student ratings", e)
        _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      }
    }
  }

  private fun Int.toStarRating(): StarRating =
      when (this) {
        1 -> StarRating.ONE
        2 -> StarRating.TWO
        3 -> StarRating.THREE
        4 -> StarRating.FOUR
        5 -> StarRating.FIVE
        else -> StarRating.FIVE // fallback
      }
}
