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
import com.android.sample.ui.components.RatingAggregationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingUIState(
    val booking: Booking = Booking(),
    val listing: Listing = Proposal(),
    val creatorProfile: Profile = Profile(),
    val loadError: Boolean = false,
    val ratingSubmitted: Boolean = false
)

class BookingDetailsViewModel(
    private val bookingRepository: BookingRepository = BookingRepositoryProvider.repository,
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository,
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val ratingRepository: RatingRepository = RatingRepositoryProvider.repository,
) : ViewModel() {

  private val _bookingUiState = MutableStateFlow(BookingUIState())
  // Public read-only state flow for the UI to observe
  val bookingUiState: StateFlow<BookingUIState> = _bookingUiState.asStateFlow()

  fun setUiStateForTest(state: BookingUIState) {
    _bookingUiState.value = state
  }

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

        val fromUserId = booking.bookerId
        val tutorUserId = booking.listingCreatorId
        val currentBookingId = booking.bookingId

        val tutorAlreadyRated =
            try {
              ratingRepository.hasRating(
                  fromUserId = fromUserId,
                  toUserId = tutorUserId,
                  ratingType = RatingType.TUTOR,
                  targetObjectId = currentBookingId,
              )
            } catch (e: Exception) {
              Log.w("BookingDetailsViewModel", "Error checking tutor rating", e)
              false
            }

        val listingAlreadyRated =
            try {
              ratingRepository.hasRating(
                  fromUserId = fromUserId,
                  toUserId = tutorUserId,
                  ratingType = RatingType.LISTING,
                  targetObjectId = currentBookingId,
              )
            } catch (e: Exception) {
              Log.w("BookingDetailsViewModel", "Error checking listing rating", e)
              false
            }

        val alreadySubmitted = tutorAlreadyRated && listingAlreadyRated

        // IMPORTANT: build a FRESH state, don't reuse old ratingSubmitted
        _bookingUiState.value =
            BookingUIState(
                booking = booking,
                listing = listing,
                creatorProfile = creatorProfile,
                loadError = false,
                ratingSubmitted = alreadySubmitted,
            )
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

  /**
   * Submits the student's ratings for both the tutor and the listing.
   *
   * This method:
   * - Ensures a valid booking is loaded.
   * - Ensures the booking has been completed (ratings allowed only after completion).
   * - Validates that both star values are within the range [1–5].
   * - Converts the raw star values into `StarRating` enums.
   * - Creates and validates two `Rating` objects:
   *     - a tutor rating (type = `TUTOR`)
   *     - a listing rating (type = `LISTING`)
   * - Persists both ratings via the `RatingRepository`.
   *
   * If any step fails (invalid input, missing booking, repository errors), the function logs a
   * warning/error and updates the UI state with `loadError = true` so the UI can react.
   *
   * @param tutorStars The number of stars (1–5) that the student gives to the tutor.
   * @param listingStars The number of stars (1–5) that the student gives to the listing/course.
   */
  fun submitStudentRatings(tutorStars: Int, listingStars: Int) {
    val booking = bookingUiState.value.booking

    // No booking loaded or not completed -> do nothing
    if (booking.bookingId.isBlank()) return
    if (booking.status != BookingStatus.COMPLETED) return

    // Validate inputs: both ratings must be between 1 and 5
    if (tutorStars !in 1..5 || listingStars !in 1..5) {
      Log.w(
          "BookingDetailsViewModel",
          "Ignoring invalid star values: tutor=$tutorStars, listing=$listingStars")
      _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      return
    }

    val tutorRatingEnum = tutorStars.toStarRating()
    val listingRatingEnum = listingStars.toStarRating()

    viewModelScope.launch {
      try {
        val fromUserId = booking.bookerId
        val tutorUserId = booking.listingCreatorId
        val bookingId = booking.bookingId
        // --- prevent duplicates: one rating per booking ---
        val tutorAlreadyRated =
            ratingRepository.hasRating(
                fromUserId = fromUserId,
                toUserId = tutorUserId,
                ratingType = RatingType.TUTOR,
                targetObjectId = bookingId,
            )

        val listingAlreadyRated =
            ratingRepository.hasRating(
                fromUserId = fromUserId,
                toUserId = tutorUserId,
                ratingType = RatingType.LISTING,
                targetObjectId = bookingId,
            )

        if (tutorAlreadyRated && listingAlreadyRated) {
          Log.d(
              "BookingDetailsViewModel", "Ratings for this booking already exist; skipping submit")
          _bookingUiState.value = bookingUiState.value.copy(ratingSubmitted = true)
          return@launch
        }

        // 1) Student rates the tutor (for THIS booking)
        val tutorRating =
            Rating(
                ratingId = ratingRepository.getNewUid(),
                fromUserId = fromUserId,
                toUserId = tutorUserId,
                starRating = tutorRatingEnum,
                comment = "",
                ratingType = RatingType.TUTOR,
                targetObjectId = bookingId,
            )

        // 2) Student rates the listing (for THIS booking)
        val listingRating =
            Rating(
                ratingId = ratingRepository.getNewUid(),
                fromUserId = fromUserId,
                toUserId = tutorUserId,
                starRating = listingRatingEnum,
                comment = "",
                ratingType = RatingType.LISTING,
                targetObjectId = bookingId,
            )

        tutorRating.validate()
        listingRating.validate()

        if (!tutorAlreadyRated) ratingRepository.addRating(tutorRating)
        if (!listingAlreadyRated) ratingRepository.addRating(listingRating)

        RatingAggregationHelper.recomputeTutorAggregateRating(
            tutorUserId = tutorUserId,
            ratingRepo = ratingRepository,
            profileRepo = profileRepository)

        _bookingUiState.value = bookingUiState.value.copy(ratingSubmitted = true)
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error submitting student ratings", e)
        _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      }
    }
  }

  /**
   * Converts an integer star count into a `StarRating` enum.
   *
   * Accepts only values in the range 1–5. Calling this method with any other integer results in an
   * [IllegalArgumentException], ensuring invalid values do not silently pass.
   *
   * @return The corresponding [StarRating] enum.
   * @receiver The integer star value to convert.
   * @throws IllegalArgumentException if the integer is not between 1 and 5.
   */
  private fun Int.toStarRating(): StarRating =
      when (this) {
        1 -> StarRating.ONE
        2 -> StarRating.TWO
        3 -> StarRating.THREE
        4 -> StarRating.FOUR
        5 -> StarRating.FIVE
        else -> throw IllegalArgumentException("Invalid star value: $this")
      }
}
