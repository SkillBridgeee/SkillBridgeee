package com.android.sample.ui.bookings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.rating.Rating
import com.android.sample.model.rating.RatingRepository
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
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
    viewModelScope.launch {
      if (demo) {
        _uiState.value = demoCards()
        return@launch
      }

      val result = mutableListOf<BookingCardUi>()
      try {
        val bookings = bookingRepo.getBookingsByUserId(userId)
        for (b in bookings) {
          val card = buildCardSafely(b)
          if (card != null) result += card
        }
        _uiState.value = result
      } catch (e: Throwable) {
        Log.e("MyBookingsViewModel", "Error loading bookings for $userId", e)
        _uiState.value = emptyList()
      }
    }
  }

  private fun demoCards(): List<BookingCardUi> {
    val now = Date()
    return listOf(
        BookingCardUi(
            id = "demo-1",
            tutorId = "tutor-1",
            tutorName = "Alice Martin",
            subject = "Guitar - Beginner",
            pricePerHourLabel = "$30.0/hr",
            durationLabel = "1hr",
            dateLabel = dateFmt.format(now),
            ratingStars = 5,
            ratingCount = 12),
        BookingCardUi(
            id = "demo-2",
            tutorId = "tutor-2",
            tutorName = "Lucas Dupont",
            subject = "French Conversation",
            pricePerHourLabel = "$25.0/hr",
            durationLabel = "1h 30m",
            dateLabel = dateFmt.format(now),
            ratingStars = 4,
            ratingCount = 8))
  }

  private suspend fun buildCardSafely(b: Booking): BookingCardUi? {
    return try {
      val listing = listingRepo.getListing(b.associatedListingId)
      val profile = profileRepo.getProfile(b.listingCreatorId)
      val ratings = ratingRepo.getRatingsOfListing(b.associatedListingId)
      buildCard(b, listing, profile, ratings)
    } catch (e: Throwable) {
      Log.e("MyBookingsViewModel", "Skipping booking ${b.bookingId}", e)
      null
    }
  }

  private fun buildCard(
      b: Booking,
      listing: Listing,
      profile: Profile,
      ratings: List<Rating>
  ): BookingCardUi {
    val tutorName = profile.name
    val subject = listing.skill.mainSubject.toString()
    val pricePerHourLabel = String.format(Locale.US, "$%.1f/hr", b.price)
    val durationLabel = formatDuration(b.sessionStart, b.sessionEnd)
    val dateLabel = formatDate(b.sessionStart)

    val ratingCount = ratings.size
    val ratingStars =
        if (ratingCount > 0) {
          val total = ratings.sumOf { it.starRating.value } // assuming value is Int 1..5
          (total.toDouble() / ratingCount).roundToInt().coerceIn(0, 5)
        } else {
          0
        }

    return BookingCardUi(
        id = b.bookingId,
        tutorId = b.listingCreatorId,
        tutorName = tutorName,
        subject = subject,
        pricePerHourLabel = pricePerHourLabel,
        durationLabel = durationLabel,
        dateLabel = dateLabel,
        ratingStars = ratingStars,
        ratingCount = ratingCount)
  }

  private fun formatDuration(start: Date, end: Date): String {
    val durationMs = (end.time - start.time).coerceAtLeast(0L)
    val hours = durationMs / (60 * 60 * 1000)
    val mins = (durationMs / (60 * 1000)) % 60
    return if (mins == 0L) {
      val plural = if (hours > 1L) "s" else ""
      "${hours}hr$plural"
    } else {
      "${hours}h ${mins}m"
    }
  }

  private fun formatDate(d: Date): String =
      try {
        dateFmt.format(d)
      } catch (_: Throwable) {
        ""
      }
}
