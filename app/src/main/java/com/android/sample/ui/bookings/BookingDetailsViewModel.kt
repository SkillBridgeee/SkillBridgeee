package com.android.sample.ui.bookings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.booking.PaymentStatus
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.ListingType
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingUIState(
    val booking: Booking = Booking(),
    val listing: Listing = Proposal(),
    val creatorProfile: Profile = Profile(),
    val bookerProfile: Profile = Profile(), // Profile of the person who made the booking
    val loadError: Boolean = false,
    val ratingProgress: RatingProgress = RatingProgress(),
    val isCreator: Boolean = false,
    val isBooker: Boolean = false,
    val onAcceptBooking: () -> Unit = {}, // Added callback for accepting a booking
    val onDenyBooking: () -> Unit = {} // Added callback for denying a booking
)

data class RatingProgress(
    val bookerRatedTutor: Boolean = false,
    val bookerRatedStudent: Boolean = false,
    val bookerRatedListing: Boolean = false,
    val creatorRatedTutor: Boolean = false,
    val creatorRatedStudent: Boolean = false,
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

        // Parallelize remote calls to improve latency
        coroutineScope {
          val creatorDeferred = async { profileRepository.getProfile(booking.listingCreatorId) }
          val bookerDeferred = async { profileRepository.getProfile(booking.bookerId) }
          val listingDeferred = async { listingRepository.getListing(booking.associatedListingId) }

          val creatorProfile =
              creatorDeferred.await()
                  ?: throw IllegalStateException(
                      "BookingDetailsViewModel : Creator profile not found")

          val bookerProfile =
              bookerDeferred.await()
                  ?: throw IllegalStateException(
                      "BookingDetailsViewModel : Booker profile not found")

          val listing =
              listingDeferred.await()
                  ?: throw IllegalStateException("BookingDetailsViewModel : Listing not found")
          val creatorId = booking.listingCreatorId
          val bookerId = booking.bookerId
          val bookingIdObj = booking.bookingId
          val listingIdObj = listing.listingId
          val currentUserId = profileRepository.getCurrentUserId()

          val tutorUserId = if (listing.type == ListingType.PROPOSAL) creatorId else bookerId

          val studentUserId = if (listing.type == ListingType.PROPOSAL) bookerId else creatorId

          val isCreator = currentUserId == creatorId
          val isBooker = currentUserId == bookerId

          val bookerRatedTutor =
              ratingRepository.hasRating(
                  fromUserId = bookerId,
                  toUserId = tutorUserId,
                  ratingType = RatingType.TUTOR,
                  targetObjectId = bookingIdObj,
              )

          val bookerRatedStudent =
              ratingRepository.hasRating(
                  fromUserId = bookerId,
                  toUserId = studentUserId,
                  ratingType = RatingType.STUDENT,
                  targetObjectId = bookingIdObj,
              )

          val bookerRatedListing =
              ratingRepository.hasRating(
                  fromUserId = bookerId,
                  toUserId = listing.creatorUserId, // listing owner; must be nonblank
                  ratingType = RatingType.LISTING,
                  targetObjectId = listingIdObj, // IMPORTANT: listingId, not bookingId
              )

          val creatorRatedTutor =
              ratingRepository.hasRating(
                  fromUserId = creatorId,
                  toUserId = tutorUserId,
                  ratingType = RatingType.TUTOR,
                  targetObjectId = bookingIdObj,
              )

          val creatorRatedStudent =
              ratingRepository.hasRating(
                  fromUserId = creatorId,
                  toUserId = studentUserId,
                  ratingType = RatingType.STUDENT,
                  targetObjectId = bookingIdObj,
              )

          val ratingProgress =
              RatingProgress(
                  bookerRatedTutor = bookerRatedTutor,
                  bookerRatedStudent = bookerRatedStudent,
                  bookerRatedListing = bookerRatedListing,
                  creatorRatedTutor = creatorRatedTutor,
                  creatorRatedStudent = creatorRatedStudent,
              )

          _bookingUiState.value =
              BookingUIState(
                  booking = booking,
                  listing = listing,
                  creatorProfile = creatorProfile,
                  bookerProfile = bookerProfile,
                  loadError = false,
                  ratingProgress = ratingProgress,
                  isCreator = isCreator,
                  isBooker = isBooker,
                  onAcceptBooking = { acceptBooking(booking.bookingId) },
                  onDenyBooking = { denyBooking(booking.bookingId) })
        }
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

  private fun acceptBooking(bookingId: String) {
    viewModelScope.launch {
      try {
        bookingRepository.updateBookingStatus(bookingId, BookingStatus.CONFIRMED)
        val updatedBooking = bookingRepository.getBooking(bookingId)
        if (updatedBooking != null) {
          _bookingUiState.value = bookingUiState.value.copy(booking = updatedBooking)
        }
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error accepting booking", e)
      }
    }
  }

  private fun denyBooking(bookingId: String) {
    viewModelScope.launch {
      try {
        bookingRepository.updateBookingStatus(bookingId, BookingStatus.CANCELLED)
        val updatedBooking = bookingRepository.getBooking(bookingId)
        if (updatedBooking != null) {
          _bookingUiState.value = bookingUiState.value.copy(booking = updatedBooking)
        }
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error denying booking", e)
      }
    }
  }

  fun markPaymentComplete() {
    val currentBookingId = bookingUiState.value.booking.bookingId
    if (currentBookingId.isBlank()) return

    viewModelScope.launch {
      try {
        bookingRepository.updatePaymentStatus(currentBookingId, PaymentStatus.PAID)
        val updatedBooking = bookingRepository.getBooking(currentBookingId)
        if (updatedBooking != null) {
          _bookingUiState.value =
              bookingUiState.value.copy(booking = updatedBooking, loadError = false)
        }
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error marking payment complete", e)
        _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      }
    }
  }

  fun confirmPaymentReceived() {
    val currentBookingId = bookingUiState.value.booking.bookingId
    if (currentBookingId.isBlank()) return

    viewModelScope.launch {
      try {
        bookingRepository.updatePaymentStatus(currentBookingId, PaymentStatus.CONFIRMED)
        val updatedBooking = bookingRepository.getBooking(currentBookingId)
        if (updatedBooking != null) {
          _bookingUiState.value =
              bookingUiState.value.copy(booking = updatedBooking, loadError = false)
        }
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error confirming payment received", e)
        _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      }
    }
  }

  private fun sanitizeComment(input: String, maxLen: Int = 500): String {
    // Remove HTML tags, control chars, collapse whitespace, trim, and truncate.
    var s =
        input
            .replace(Regex("<.*?>"), "") // strip simple HTML tags
            .replace(Regex("\\p{C}"), "") // remove control chars
            .replace(Regex("\\s+"), " ") // normalize whitespace
            .trim()
    if (s.length > maxLen) s = s.take(maxLen)
    return s
  }

  fun submitBookerRatings(
      userStars: Int,
      listingStars: Int,
      userComment: String,
      listingComment: String
  ) {
    val booking = bookingUiState.value.booking
    val listing = bookingUiState.value.listing

    // Sanitize comments
    val sanitizedUserComment = sanitizeComment(userComment)
    val sanitizedListingComment = sanitizeComment(listingComment)

    if (!validateRatingSubmission(booking, intArrayOf(userStars, listingStars))) return

    val bookingIdObj = booking.bookingId
    val listingIdObj = listing.listingId
    val bookerId = booking.bookerId

    val roleIds = resolveTutorAndStudentIds(booking, listing.type)

    // Booker rates WHO?
    val (toUserId, ratingType) =
        if (listing.type == ListingType.REQUEST) {
          roleIds.studentUserId to RatingType.STUDENT
        } else {
          roleIds.tutorUserId to RatingType.TUTOR
        }

    viewModelScope.launch {
      try {
        val userRating =
            Rating(
                ratingId = ratingRepository.getNewUid(),
                fromUserId = bookerId,
                toUserId = toUserId,
                starRating = userStars.toStarRating(),
                comment = sanitizedUserComment,
                ratingType = ratingType,
                targetObjectId = bookingIdObj,
            )

        val listingRating =
            Rating(
                ratingId = ratingRepository.getNewUid(),
                fromUserId = bookerId,
                toUserId = listing.creatorUserId,
                starRating = listingStars.toStarRating(),
                comment = sanitizedListingComment,
                ratingType = RatingType.LISTING,
                targetObjectId = listingIdObj,
            )

        userRating.validate()
        listingRating.validate()

        ratingRepository.addRating(userRating)
        ratingRepository.addRating(listingRating)

        recomputeAggregateIfNeeded(
            ratingType = ratingType,
            tutorUserId = roleIds.tutorUserId,
            studentUserId = roleIds.studentUserId)

        load(bookingIdObj)
      } catch (e: Exception) {
        Log.e("BookingDetailsViewModel", "Error submitting booker ratings", e)
        _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      }
    }
  }

  fun submitCreatorRating(stars: Int, comment: String) {
    val state = bookingUiState.value
    val booking = state.booking
    val listing = state.listing

    if (booking.bookingId.isBlank()) return
    if (!state.isCreator) return
    if (booking.status != BookingStatus.COMPLETED) return

    if (stars !in 1..5) {
      _bookingUiState.value = state.copy(loadError = true)
      return
    }

    // Sanitize creator comment
    val sanitizedComment = sanitizeComment(comment)

    val creatorId = booking.listingCreatorId
    val bookerId = booking.bookerId
    val bookingId = booking.bookingId

    val tutorUserId = if (listing.type == ListingType.PROPOSAL) creatorId else bookerId
    val studentUserId = if (listing.type == ListingType.PROPOSAL) bookerId else creatorId

    val (toUserId, ratingType) =
        if (listing.type == ListingType.REQUEST) {
          tutorUserId to RatingType.TUTOR
        } else {
          studentUserId to RatingType.STUDENT
        }

    viewModelScope.launch {
      try {
        val rating =
            Rating(
                ratingId = ratingRepository.getNewUid(),
                fromUserId = creatorId,
                toUserId = toUserId,
                starRating = stars.toStarRating(),
                comment = sanitizedComment,
                ratingType = ratingType,
                targetObjectId = bookingId,
            )

        rating.validate()
        ratingRepository.addRating(rating)

        if (ratingType == RatingType.TUTOR) {
          RatingAggregationHelper.recomputeTutorAggregateRating(
              tutorUserId, ratingRepository, profileRepository)
        } else {
          RatingAggregationHelper.recomputeStudentAggregateRating(
              studentUserId, ratingRepository, profileRepository)
        }

        load(bookingId)
      } catch (e: Exception) {
        _bookingUiState.value = state.copy(loadError = true)
      }
    }
  }

  private data class RoleIds(
      val tutorUserId: String,
      val studentUserId: String,
  )

  private fun resolveTutorAndStudentIds(booking: Booking, listingType: ListingType): RoleIds {
    val creatorId = booking.listingCreatorId
    val bookerId = booking.bookerId

    val tutorUserId = if (listingType == ListingType.PROPOSAL) creatorId else bookerId
    val studentUserId = if (listingType == ListingType.PROPOSAL) bookerId else creatorId
    return RoleIds(tutorUserId = tutorUserId, studentUserId = studentUserId)
  }

  private fun validateRatingSubmission(booking: Booking, stars: IntArray): Boolean {
    if (booking.bookingId.isBlank()) return false
    if (booking.status != BookingStatus.COMPLETED) return false
    if (stars.any { it !in 1..5 }) {
      _bookingUiState.value = bookingUiState.value.copy(loadError = true)
      return false
    }
    return true
  }

  private suspend fun recomputeAggregateIfNeeded(
      ratingType: RatingType,
      tutorUserId: String,
      studentUserId: String,
  ) {
    when (ratingType) {
      RatingType.TUTOR ->
          RatingAggregationHelper.recomputeTutorAggregateRating(
              tutorUserId = tutorUserId,
              ratingRepo = ratingRepository,
              profileRepo = profileRepository)
      RatingType.STUDENT ->
          RatingAggregationHelper.recomputeStudentAggregateRating(
              studentUserId = studentUserId,
              ratingRepo = ratingRepository,
              profileRepo = profileRepository)
      else -> Unit // LISTING etc.
    }
  }
}
