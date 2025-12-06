package com.android.sample.ui.listing

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.communication.conversation.ConversationRepositoryProvider
import com.android.sample.model.communication.newImplementation.ConversationManager
import com.android.sample.model.communication.overViewConv.OverViewConvRepositoryProvider
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.rating.RatingRepositoryProvider
import com.android.sample.model.rating.RatingType
import com.android.sample.model.rating.StarRating
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.components.RatingAggregationHelper
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the listing detail screen
 *
 * @param listing The listing being displayed
 * @param creator The profile of the listing creator
 * @param isLoading Whether the data is currently loading
 * @param error Any error message to display
 * @param isOwnListing Whether the current user is the creator of this listing
 * @param bookingInProgress Whether a booking is being created
 * @param bookingError Any error during booking creation
 * @param bookingSuccess Whether booking was created successfully
 * @param conversationCreationWarning Warning message if conversation creation fails
 * @param listingBookings List of bookings for this listing (for owner view)
 * @param bookingsLoading Whether bookings are being loaded
 * @param bookerProfiles Map of booker user IDs to their profiles
 * @param currentUserId The ID of the current user (for determining which actions to show)
 */
data class ListingUiState(
    val listing: Listing? = null,
    val creator: Profile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOwnListing: Boolean = false,
    val bookingInProgress: Boolean = false,
    val bookingError: String? = null,
    val bookingSuccess: Boolean = false,
    val conversationCreationWarning: String? = null,
    val listingBookings: List<Booking> = emptyList(),
    val bookingsLoading: Boolean = false,
    val listingDeleted: Boolean = false,
    val bookerProfiles: Map<String, Profile> = emptyMap(),
    val tutorRatingPending: Boolean = false,
    val currentUserId: String? = null
)

/**
 * ViewModel for the listing detail screen
 *
 * @param listingRepo Repository for listings
 * @param profileRepo Repository for profiles
 * @param bookingRepo Repository for bookings
 */
class ListingViewModel(
    private val listingRepo: ListingRepository = ListingRepositoryProvider.repository,
    private val profileRepo: ProfileRepository = ProfileRepositoryProvider.repository,
    private val bookingRepo: BookingRepository = BookingRepositoryProvider.repository,
    private val ratingRepo: RatingRepository = RatingRepositoryProvider.repository,
    private val conversationManager: ConversationManager =
        ConversationManager(
            convRepo = ConversationRepositoryProvider.repository,
            overViewRepo = OverViewConvRepositoryProvider.repository)
) : ViewModel() {

  companion object {
    // User-facing messages for booking and conversation creation
    const val MSG_BOOKING_SUCCESS =
        "Your booking has been created successfully and is pending confirmation."
    const val MSG_CONVERSATION_SUCCESS =
        "A conversation has been created with the tutor. You can now chat with them in the Discussions tab to coordinate your session."
    const val MSG_CONVERSATION_FAILURE_PREFIX = "Conversation could not be created: "
    const val MSG_CONVERSATION_ALTERNATIVE = "You can still contact the tutor through other means."
  }

  private val _uiState = MutableStateFlow(ListingUiState())
  val uiState: StateFlow<ListingUiState> = _uiState

  /**
   * Load listing details and creator profile
   *
   * @param listingId The ID of the listing to load
   */
  fun loadListing(listingId: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      try {
        val listing = listingRepo.getListing(listingId)
        if (listing == null) {
          _uiState.update { it.copy(isLoading = false, error = "Listing not found") }
          return@launch
        }

        val creator = profileRepo.getProfile(listing.creatorUserId)
        val currentUserId = UserSessionManager.getCurrentUserId()
        val isOwnListing = currentUserId == listing.creatorUserId

        _uiState.update {
          it.copy(
              listing = listing,
              creator = creator,
              isLoading = false,
              isOwnListing = isOwnListing,
              currentUserId = currentUserId,
              error = null)
        }

        // If this is the owner's listing, load bookings
        if (isOwnListing) {
          loadBookingsForListing(listingId)
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoading = false, error = "Failed to load listing: ${e.message}")
        }
      }
    }
  }

  /**
   * Load bookings for this listing (owner view)
   *
   * @param listingId The ID of the listing
   */
  private fun loadBookingsForListing(listingId: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(bookingsLoading = true) }
      try {
        val bookings = bookingRepo.getBookingsByListing(listingId)

        // Load booker profiles
        val bookerIds = bookings.map { it.bookerId }.distinct()
        val profiles = mutableMapOf<String, Profile>()
        bookerIds.forEach { userId ->
          profileRepo.getProfile(userId)?.let { profile -> profiles[userId] = profile }
        }

        // check if there is at least one COMPLETED booking not yet rated
        val currentTutorId = FirebaseAuth.getInstance().currentUser?.uid

        val hasPendingTutorRating =
            if (currentTutorId == null) {
              false
            } else {
              val completedForTutor =
                  bookings.filter {
                    it.status == BookingStatus.COMPLETED && it.listingCreatorId == currentTutorId
                  }

              completedForTutor.any { booking ->
                val alreadyRated =
                    try {
                      ratingRepo.hasRating(
                          fromUserId = currentTutorId,
                          toUserId = booking.bookerId,
                          ratingType = RatingType.STUDENT,
                          targetObjectId = booking.bookingId, // 👈 per-booking
                      )
                    } catch (e: Exception) {
                      Log.w("ListingViewModel", "Error checking existing rating", e)
                      false
                    }
                !alreadyRated
              }
            }

        _uiState.update {
          it.copy(
              listingBookings = bookings,
              bookerProfiles = profiles,
              bookingsLoading = false,
              tutorRatingPending = hasPendingTutorRating)
        }
      } catch (_: Exception) {
        _uiState.update { it.copy(bookingsLoading = false) }
      }
    }
  }

  /**
   * Create a booking for this listing
   *
   * @param sessionStart Start time of the session
   * @param sessionEnd End time of the session
   */
  fun createBooking(sessionStart: Date, sessionEnd: Date) {
    val listing = _uiState.value.listing
    if (listing == null) {
      _uiState.update { it.copy(bookingError = "Listing not found") }
      return
    }

    // Check if user is trying to book their own listing
    val currentUserId = UserSessionManager.getCurrentUserId()
    if (currentUserId == null) {
      _uiState.update { it.copy(bookingError = "You must be logged in to create a booking") }
      return
    }

    if (currentUserId == listing.creatorUserId) {
      _uiState.update { it.copy(bookingError = "You cannot book your own listing") }
      return
    }

    viewModelScope.launch {
      _uiState.update {
        it.copy(bookingInProgress = true, bookingError = null, bookingSuccess = false)
      }
      try {
        // Validate session times
        val durationMillis = sessionEnd.time - sessionStart.time
        if (durationMillis <= 0) {
          _uiState.update {
            it.copy(
                bookingInProgress = false,
                bookingError = "Invalid session time: End time must be after start time")
          }
          return@launch
        }

        // Calculate price based on session duration and hourly rate
        val durationHours = durationMillis.toDouble() / (1000.0 * 60 * 60)
        val price = listing.hourlyRate * durationHours

        val booking =
            Booking(
                bookingId = bookingRepo.getNewUid(),
                associatedListingId = listing.listingId,
                listingCreatorId = listing.creatorUserId,
                bookerId = currentUserId,
                sessionStart = sessionStart,
                sessionEnd = sessionEnd,
                status = BookingStatus.PENDING,
                price = price)

        booking.validate()

        bookingRepo.addBooking(booking)

        // Create a conversation between the booker and listing creator
        try {
          val creatorProfile = profileRepo.getProfile(listing.creatorUserId)
          val conversationName = creatorProfile?.name ?: "Booking Discussion"

          val convId =
              conversationManager.createConvAndOverviews(
                  creatorId = currentUserId,
                  otherUserId = listing.creatorUserId,
                  convName = conversationName)
          Log.d(
              "ListingViewModel",
              "Conversation created successfully: $convId between $currentUserId and ${listing.creatorUserId}")
        } catch (e: Exception) {
          Log.e("ListingViewModel", "Failed to create conversation", e)
          _uiState.update {
            it.copy(
                bookingInProgress = false,
                bookingSuccess = true, // Booking is successful even if conversation creation fails
                bookingError = null,
                conversationCreationWarning = "${MSG_CONVERSATION_FAILURE_PREFIX}${e.message}")
          }
          return@launch
        }

        _uiState.update {
          it.copy(bookingInProgress = false, bookingSuccess = true, bookingError = null)
        }
      } catch (e: IllegalArgumentException) {
        _uiState.update {
          it.copy(
              bookingInProgress = false,
              bookingError = "Invalid booking: ${e.message}",
              bookingSuccess = false)
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              bookingInProgress = false,
              bookingError = "Failed to create booking: ${e.message}",
              bookingSuccess = false)
        }
      }
    }
  }

  /**
   * Approve a booking for this listing
   *
   * @param bookingId The ID of the booking to approve
   */
  fun approveBooking(bookingId: String) {
    viewModelScope.launch {
      try {
        bookingRepo.confirmBooking(bookingId)
        // Refresh bookings to show updated status
        _uiState.value.listing?.let { loadBookingsForListing(it.listingId) }
      } catch (e: Exception) {
        Log.w("ListingViewModel", "Couldnt approve the booking", e)
      }
    }
  }

  /**
   * Reject a booking for this listing
   *
   * @param bookingId The ID of the booking to reject
   */
  fun rejectBooking(bookingId: String) {
    viewModelScope.launch {
      try {
        bookingRepo.cancelBooking(bookingId)
        // Refresh bookings to show updated status
        _uiState.value.listing?.let { loadBookingsForListing(it.listingId) }
      } catch (e: Exception) {
        Log.w("ListingViewModel", "Couldnt reject the booking", e)
      }
    }
  }

  private fun Int.toStarRating(): StarRating {
    val values = StarRating.values()
    val idx = (this - 1).coerceIn(0, values.size - 1)
    return values.getOrNull(idx) ?: values.first()
  }

  /**
   * Submits a rating from the tutor (current user) to the student for **one completed booking** of
   * this listing.
   *
   * Behaviour:
   * - Requires a listing to be loaded and the current user to be authenticated (tutor).
   * - Finds the first booking in `listingBookings` that:
   *     - belongs to this listing,
   *     - has status `COMPLETED`,
   *     - has `listingCreatorId == current tutor id`.
   * - Uses `ratingRepo.hasRating(...)` with `(fromUserId, toUserId, ratingType = STUDENT,
   *   targetObjectId = bookingId)` to check if that specific booking has already been rated. If a
   *   rating already exists, the function logs and returns without creating a duplicate.
   * - Converts the raw `stars` (1–5) to a `StarRating` enum and builds a `Rating` object targeting
   *   the student.
   * - Persists the rating via `ratingRepo.addRating(...)`.
   * - Recomputes the student's aggregate rating using
   *   `RatingAggregationHelper.recomputeStudentAggregateRating(...)` and writes it to the student's
   *   profile.
   * - Updates `_uiState` so the tutor rating section can be hidden (`tutorRatingPending = false`)
   *   and reloads bookings for the listing to reflect the updated state.
   *
   * Error handling:
   * - If no listing is loaded, the user is not authenticated, or no matching completed booking is
   *   found, the method logs a warning and exits early.
   * - Any repository errors are caught and logged so the app does not crash; in that case the
   *   rating might not be persisted and the UI state is left unchanged.
   *
   * @param stars The number of stars (1–5) that the tutor gives to the student for this booking.
   */
  fun submitTutorRating(stars: Int) {
    viewModelScope.launch {
      try {
        val listing = _uiState.value.listing
        if (listing == null) {
          Log.w("ListingViewModel", "Cannot submit rating: listing missing")
          return@launch
        }

        val fromUserId =
            FirebaseAuth.getInstance().currentUser?.uid ?: throw Exception("User not authenticated")

        val completedBooking =
            _uiState.value.listingBookings.firstOrNull {
              it.status == BookingStatus.COMPLETED && it.listingCreatorId == fromUserId
            }
        if (completedBooking == null) {
          Log.w("ListingViewModel", "No completed booking found to rate")
          return@launch
        }

        val toUserId = completedBooking.bookerId

        // unique per booking
        val alreadyRated =
            try {
              ratingRepo.hasRating(
                  fromUserId = fromUserId,
                  toUserId = toUserId,
                  ratingType = RatingType.STUDENT,
                  targetObjectId = completedBooking.bookingId, // 👈 changed
              )
            } catch (e: Exception) {
              Log.w("ListingViewModel", "Error checking existing rating", e)
              false
            }

        if (alreadyRated) {
          Log.d("ListingViewModel", "Rating for this booking already exists; skipping submit")
          _uiState.value.listing?.let { loadBookingsForListing(it.listingId) }
          return@launch
        }

        val ratingId = ratingRepo.getNewUid()
        val starEnum = stars.toStarRating()

        val rating =
            Rating(
                ratingId = ratingId,
                fromUserId = fromUserId,
                toUserId = toUserId,
                starRating = starEnum,
                comment = "",
                ratingType = RatingType.STUDENT,
                targetObjectId = completedBooking.bookingId, // 👈 changed
            )

        ratingRepo.addRating(rating)

        RatingAggregationHelper.recomputeStudentAggregateRating(
            studentUserId = toUserId, ratingRepo = ratingRepo, profileRepo = profileRepo)

        _uiState.update { it.copy(tutorRatingPending = false) }

        Log.d("ListingViewModel", "Tutor rating persisted: $stars stars -> $toUserId")
        _uiState.value.listing?.let { loadBookingsForListing(it.listingId) }
      } catch (e: Exception) {
        Log.w("ListingViewModel", "Failed to submit tutor rating", e)
      }
    }
  }

  /** Clears the booking success state. */
  fun clearBookingSuccess() {
    _uiState.update { it.copy(bookingSuccess = false) }
  }

  /** Clears the booking error state. */
  fun clearBookingError() {
    _uiState.update { it.copy(bookingError = null) }
  }

  /** Clears the conversation creation warning. */
  fun clearConversationWarning() {
    _uiState.update { it.copy(conversationCreationWarning = null) }
  }

  fun showBookingSuccess() {
    _uiState.update { it.copy(bookingSuccess = true) }
  }

  fun showBookingError(message: String) {
    _uiState.update { it.copy(bookingError = message) }
  }

  /**
   * Delete the current listing. Before deletion, cancel all bookings associated with the listing
   * (any booking not already CANCELLED will be set to CANCELLED).
   */
  fun deleteListing() {
    val listing = _uiState.value.listing
    if (listing == null) {
      _uiState.update { it.copy(error = "Listing not found") }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null, listingDeleted = false) }
      try {
        // fetch bookings for listing
        val bookings =
            try {
              bookingRepo.getBookingsByListing(listing.listingId)
            } catch (e: Exception) {
              // If fetching bookings fails, continue but log; we still attempt deletion
              Log.w("ListingViewModel", "Failed to fetch bookings for cancellation", e)
              emptyList()
            }

        // Cancel each non-cancelled booking. Log errors but continue.
        bookings
            .filter { it.status != BookingStatus.CANCELLED }
            .forEach { booking ->
              try {
                bookingRepo.cancelBooking(booking.bookingId)
              } catch (e: Exception) {
                Log.w("ListingViewModel", "Failed to cancel booking ${booking.bookingId}", e)
              }
            }

        // Delete the listing
        listingRepo.deleteListing(listing.listingId)

        // Update UI state: listing removed and bookings cleared
        _uiState.update {
          it.copy(
              listing = null,
              listingBookings = emptyList(),
              isOwnListing = false,
              isLoading = false,
              error = null,
              listingDeleted = true)
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              isLoading = false,
              error = "Failed to delete listing: ${e.message}",
              listingDeleted = false)
        }
      }
    }
  }

  fun clearListingDeleted() {
    _uiState.update { it.copy(listingDeleted = false) }
  }
}
