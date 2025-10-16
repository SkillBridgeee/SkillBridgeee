package com.android.sample

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.rating.RatingInfo
import com.android.sample.model.skill.Skill
import com.android.sample.model.tutor.FakeProfileRepository
import com.android.sample.model.user.Profile
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the complete UI state of the Home (Main) screen.
 *
 * @property welcomeMessage A greeting message for the current user.
 * @property skills A list of skills retrieved from the local repository.
 * @property tutors A list of tutor cards prepared for display.
 */
data class HomeUiState(
    val welcomeMessage: String = "",
    val skills: List<Skill> = emptyList(),
    val tutors: List<TutorCardUi> = emptyList()
)

/**
 * UI representation of a tutor card displayed on the main page.
 *
 * @property name Tutor's display name.
 * @property subject Subject or skill taught by the tutor.
 * @property hourlyRate Tutor's hourly rate, formatted to two decimals.
 * @property ratingStars Average star rating (rounded 0–5).
 * @property ratingCount Total number of ratings for the tutor.
 */
data class TutorCardUi(
    val name: String,
    val subject: String,
    val hourlyRate: Double,
    val ratingStars: Int,
    val ratingCount: Int
)

/**
 * ViewModel responsible for managing and preparing data for the Main Page (HomeScreen).
 *
 * It loads skills, listings, and tutor profiles from local repositories and exposes them as a
 * unified [HomeUiState] via a [StateFlow]. It also handles user actions such as booking and adding
 * tutors (currently as placeholders).
 */
class MainPageViewModel : ViewModel() {

  private val profileRepository = FakeProfileRepository()
  private val listingRepository = ListingRepositoryProvider.repository

  private val _navigationEvent = MutableStateFlow<String?>(null)
  val navigationEvent: StateFlow<String?> = _navigationEvent.asStateFlow()

  private val _uiState = MutableStateFlow(HomeUiState())
  /** The publicly exposed immutable UI state observed by the composables. */
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
    // Load all initial data when the ViewModel is created.
    viewModelScope.launch { load() }
  }

  /**
   * Loads all data required for the main page.
   *
   * Fetches data from local repositories (skills, listings, and tutors) and builds a list of
   * [TutorCardUi] safely using [buildTutorCardSafely]. Updates the [_uiState] with a formatted
   * welcome message and the loaded data.
   */
  suspend fun load() {
    try {
      val skills = emptyList<Skill>()
      val listings = listingRepository.getAllListings()
      val tutors = profileRepository.tutors

      val tutorCards = listings.mapNotNull { buildTutorCardSafely(it, tutors) }
      val userName = profileRepository.fakeUser.name

      _uiState.value =
          HomeUiState(
              welcomeMessage = "Welcome back, $userName!", skills = skills, tutors = tutorCards)
    } catch (e: Exception) {
      // Fallback in case of repository or mapping failure.
      _uiState.value = HomeUiState(welcomeMessage = "Welcome back, Ava!")
    }
  }

  /**
   * Safely builds a [TutorCardUi] object for the given [Listing] and tutor list.
   *
   * Any errors encountered during construction are caught, and null is returned to prevent one
   * failing item from breaking the entire list rendering.
   *
   * @param listing The [Listing] representing a tutor's offering.
   * @param tutors The list of available [Profile]s.
   * @return A constructed [TutorCardUi], or null if the data is invalid.
   */
  private fun buildTutorCardSafely(listing: Listing, tutors: List<Profile>): TutorCardUi? {
    return try {
      val tutor = tutors.find { it.userId == listing.creatorUserId } ?: return null

      TutorCardUi(
          name = tutor.name,
          subject = listing.skill.skill,
          hourlyRate = formatPrice(listing.hourlyRate),
          ratingStars = computeAvgStars(tutor.tutorRating),
          ratingCount = ratingCountFor(tutor.tutorRating))
    } catch (e: Exception) {
      null
    }
  }

  /**
   * Computes the average rating for a tutor and converts it to a rounded integer value.
   *
   * @param rating The [RatingInfo] containing average and total ratings.
   * @return The rounded star rating, clamped between 0 and 5.
   */
  private fun computeAvgStars(rating: RatingInfo): Int {
    if (rating.totalRatings == 0) return 0
    val avg = rating.averageRating
    return avg.roundToInt().coerceIn(0, 5)
  }

  /**
   * Retrieves the total number of ratings for a tutor.
   *
   * @param rating The [RatingInfo] object.
   * @return The total number of ratings.
   */
  private fun ratingCountFor(rating: RatingInfo): Int = rating.totalRatings

  /**
   * Formats the hourly rate to two decimal places for consistent display.
   *
   * @param hourlyRate The raw hourly rate value.
   * @return The formatted hourly rate as a [Double].
   */
  private fun formatPrice(hourlyRate: Double): Double {
    return String.format("%.2f", hourlyRate).toDouble()
  }

  /**
   * Handles the "Book" button click event for a tutor.
   *
   * This function will be expanded in future versions to handle booking logic.
   *
   * @param tutorName The name of the tutor being booked.
   */
  fun onBookTutorClicked(tutorName: String) {
    viewModelScope.launch {
      // TODO handle booking logic
    }
  }

  /**
   * Handles the "Add Tutor" button click event.
   *
   * This function will be expanded in future versions to handle adding new tutors.
   */
  fun onAddTutorClicked(profileId: String) {
    viewModelScope.launch { _navigationEvent.value = profileId }
  }

  fun onNavigationHandled() {
    _navigationEvent.value = null
  }
}
