package com.android.sample

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the complete UI state of the Home (Main) screen.
 *
 * @property welcomeMessage A greeting message for the current user.
 * @property subjects A list of subjects for the List to display.
 * @property tutors A list of tutor cards prepared for display.
 */
data class HomeUiState(
    val welcomeMessage: String = "",
    val subjects: List<MainSubject> = MainSubject.entries.toList(),
    var tutors: List<TutorCardUi> = emptyList()
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
) {}

/**
 * ViewModel responsible for managing and preparing data for the Main Page (HomeScreen).
 *
 * It loads skills, listings, and tutor profiles from local repositories and exposes them as a
 * unified [HomeUiState] via a [StateFlow]. It also handles user actions such as booking and adding
 * tutors (currently as placeholders).
 */
class MainPageViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository
) : ViewModel() {

  private val defaultWelcomeMsg = "Welcome back!"
  private val pageTag = "MainPageViewModel"

  // TODO regarder la navigation aucune idée pour l'instant
  private val _navigationEvent = MutableStateFlow<String?>(null)
  val navigationEvent: StateFlow<String?> = _navigationEvent.asStateFlow()

  private val _uiState = MutableStateFlow(HomeUiState())

  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
    // Load all initial data when the ViewModel is created.
    viewModelScope.launch { load() }
  }

  /**
   * Loads all data required for the main page.
   *
   * Fetches data from local repositories (skills, listings, and tutors) and builds a list of
   * [TutorCardUi]. Updates the [_uiState] with a formatted welcome message and the loaded data.
   */
  suspend fun load() {
    try {
      val listings = listingRepository.getAllListings()
      val profiles = profileRepository.getAllProfiles()

      val tutorProfiles =
          listings.mapNotNull { listing -> profiles.find { it.userId == listing.creatorUserId } }
      val userName = mutableStateOf("")
      navigationEvent.value?.let { getCurrentUserName("user123") { name -> userName.value = name } }
          ?: "Ava"

      _uiState.value = HomeUiState(welcomeMessage = "Welcome back, $userName!", tutors = tutorCards)
    } catch (e: Exception) {
      // Log the error for debugging while providing a safe fallback UI state
      Log.w(pageTag, "Failed to build HomeUiState, using fallback", e)
      _uiState.value = HomeUiState(welcomeMessage = defaultWelcomeMsg)
    }
  }

  /**
   * Handles the "Book" button click event for a tutor.
   *
   * This function will be expanded in future versions to handle booking logic.
   *
   * @param tutorName The name of the tutor being booked.
   */
  fun onTutorClick(profileId: String) {
    viewModelScope.launch { _navigationEvent.value = profileId }
  }

  /**
   * Handles the "Add Tutor" button click event.
   *
   * This function will be expanded in future versions to handle adding new tutors.
   */
  fun onAddTutorClicked(profileId: String) {
    viewModelScope.launch { _navigationEvent.value = profileId }
  }

  fun getCurrentUserName(userId: String, onResult: (String) -> Unit) {
    viewModelScope.launch {
      val profile = runCatching { profileRepository.getProfileById(userId) }.getOrNull()
      onResult(profile?.name ?: "User")
    }
  }

  fun onNavigationHandled() {
    _navigationEvent.value = null
  }
}
