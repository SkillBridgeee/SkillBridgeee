package com.android.sample.ui.bookings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BookingCardUi(
    val id: String,
    val tutorId: String,
    val tutorName: String,
    val subject: String,
    val pricePerHourLabel: String,
    val durationLabel: String,
    val dateLabel: String,
    val ratingStars: Int = 0,
    val ratingCount: Int = 0
)

/**
 * Minimal VM:
 * - uiState is just the final list of cards
 * - init calls load()
 * - load() loops bookings and pulls listing/profile/rating to build each card
 */
class MyBookingsViewModel(
    private val bookingRepo: BookingRepository,
    private val userId: String,
    private val listingRepo: ListingRepository,
    private val profileRepo: ProfileRepository = ProfileRepositoryProvider.repository,
    private val ratingRepo: RatingRepository,
    private val locale: Locale = Locale.getDefault(),
    private val demo: Boolean = false
) : ViewModel() {

  private val _uiState = MutableStateFlow<List<BookingCardUi>>(emptyList())
  val uiState: StateFlow<List<BookingCardUi>> = _uiState.asStateFlow()
  val items: StateFlow<List<BookingCardUi>> = uiState

  private val dateFmt = SimpleDateFormat("dd/MM/yyyy", locale)

  init {
    viewModelScope.launch { load() }
  }

  fun load() {
    try {
      viewModelScope.launch {
        if (demo) {
          val now = Date()
          val c1 =
              BookingCardUi(
                  id = "demo-1",
                  tutorId = "tutor-1",
                  tutorName = "Alice Martin",
                  subject = "Guitar - Beginner",
                  pricePerHourLabel = "$30.0/hr",
                  durationLabel = "1hr",
                  dateLabel = dateFmt.format(now),
                  ratingStars = 5,
                  ratingCount = 12)
          val c2 =
              BookingCardUi(
                  id = "demo-2",
                  tutorId = "tutor-2",
                  tutorName = "Lucas Dupont",
                  subject = "French Conversation",
                  pricePerHourLabel = "$25.0/hr",
                  durationLabel = "1h 30m",
                  dateLabel = dateFmt.format(now),
                  ratingStars = 4,
                  ratingCount = 8)
          _uiState.value = listOf(c1, c2)
          return@launch
        }

        try {
          val bookings = bookingRepo.getBookingsByUserId(userId)
          val result = mutableListOf<BookingCardUi>()

          for (b in bookings) {
            try {
              val listing = listingRepo.getListing(b.associatedListingId)
              val profile = profileRepo.getProfile(b.listingCreatorId)
              val rating = ratingRepo.getRatingsOfListing(b.associatedListingId)

              val tutorName = profile.name
              val subject = listing.skill.mainSubject
              val pricePerHourLabel = String.format(Locale.US, "$%.1f/hr", b.price)

              val durationMs = (b.sessionEnd.time - b.sessionStart.time).coerceAtLeast(0L)
              val hours = durationMs / (60 * 60 * 1000)
              val mins = (durationMs / (60 * 1000)) % 60
              val durationLabel =
                  if (mins == 0L) {
                    val plural = if (hours > 1L) "s" else ""
                    "${hours}hr$plural"
                  } else {
                    "${hours}h ${mins}m"
                  }

              val dateLabel =
                  try {
                    java.text.SimpleDateFormat("dd/MM/yyyy", locale).format(b.sessionStart)
                  } catch (_: Throwable) {
                    ""
                  }

              val ratingStars = rating?.starRating?.value?.coerceIn(0, 5) ?: 0
              val ratingCount = if (rating != null) 1 else 0

              result +=
                  BookingCardUi(
                      id = b.bookingId,
                      tutorId = b.listingCreatorId,
                      tutorName = tutorName,
                      subject = subject.toString(),
                      pricePerHourLabel = pricePerHourLabel,
                      durationLabel = durationLabel,
                      dateLabel = dateLabel,
                      ratingStars = ratingStars,
                      ratingCount = ratingCount)
            } catch (inner: Throwable) {
              Log.e("MyBookingsViewModel", "Skipping booking due to error", inner)
            }
          }
          _uiState.value = result
        } catch (e: Exception) {
          Log.e("MyBookingsViewModel", "Error loading bookings for user $userId", e)
          _uiState.value = emptyList()
        }
      }
    } catch (e: Exception) {
      Log.e("MyBookingsViewModel", "Error launching load", e)
    }
  }
}
