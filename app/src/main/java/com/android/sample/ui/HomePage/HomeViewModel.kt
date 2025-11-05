package com.android.sample.ui.HomePage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
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
    val welcomeMessage: String = "Welcome back!",
    val subjects: List<MainSubject> = MainSubject.entries.toList(),
    var tutors: List<Profile> = emptyList()
)

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

  private val _uiState = MutableStateFlow(HomeUiState())
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
    // Load all initial data when the ViewModel is created.
    viewModelScope.launch { load() }
  }

  /**
   * Loads all data required for the Home screen.
   * - Fetches all listings and profiles
   * - Matches listings with their creator profiles to build the tutor list
   * - Retrieves the current user's name and builds a welcome message
   * - Updates the UI state with the prepared data
   *
   * In case of failure, logs the error and falls back to a default UI state.
   */
  fun load() {
    viewModelScope.launch {
      try {
        val allProposals = listingRepository.getProposals()
        val allProfiles = profileRepository.getAllProfiles()

        val tutorProfiles = getTutors(allProposals, allProfiles)
        val welcomeMsg = getWelcomeMsg()

        _uiState.value = HomeUiState(welcomeMessage = welcomeMsg, tutors = tutorProfiles)
      } catch (e: Exception) {
        // Log the error for debugging while providing a safe fallback UI state
        Log.w("HomePageViewModel", "Failed to build HomeUiState, using fallback", e)
        _uiState.value = HomeUiState()
      }
    }
  }

  /**
   * Retrieves the current user's name.
   * - Gets the logged-in user's ID from the session manager
   * - Fetches the user's profile and returns their name
   *
   * Returns null if no user is logged in or if the profile cannot be retrieved. Logs a warning and
   * safely returns null if an error occurs.
   */
  private suspend fun getUserName(): String? {
    return runCatching {
          val userId = UserSessionManager.getCurrentUserId()
          if (userId != null) {
            profileRepository.getProfile(userId)?.name
          } else null
        }
        .onFailure { Log.w("HomePageViewModel", "Failed to get current profile", it) }
        .getOrNull()
  }

  /**
   * Get all Profile that propose courses.
   *
   * @param proposals List of proposals submitted by users.
   * @param profiles List of all available user profiles.
   * @return A list of profiles corresponding to the creators of the given proposals.
   */
  private fun getTutors(proposals: List<Proposal>, profiles: List<Profile>): List<Profile> {
    // TODO: Add sorting logic for tutors based on rating here.
    return proposals.mapNotNull { proposal ->
      profiles.find { it.userId == proposal.creatorUserId }
    }
  }

  /**
   * Builds the welcome message displayed to the user.
   *
   * This function attempts to retrieve the current user's name and returns a personalized welcome
   * message if the name is available. If the username cannot be fetched, it falls back to a generic
   * welcome message.
   *
   * @return A welcome message string, personalized when possible.
   */
  private suspend fun getWelcomeMsg(): String {
    val userName = runCatching { getUserName() }.getOrNull()
    return if (userName != null) "Welcome back, $userName!" else "Welcome back!"
  }
}
