package com.android.sample.ui.HomePage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.authentication.UserSessionManager
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.listing.Proposal
import com.android.sample.model.listing.Request
import com.android.sample.model.skill.MainSubject
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the complete UI state of the Home (Main) screen.
 *
 * @property welcomeMessage A greeting message for the current user.
 * @property subjects A list of subjects for the List to display.
 * @property proposals A list of active proposals to display.
 * @property requests A list of active requests to display.
 */
data class HomeUiState(
    val welcomeMessage: String = "Welcome back!",
    val subjects: List<MainSubject> = MainSubject.entries.toList(),
    val proposals: List<Proposal> = emptyList(),
    val requests: List<Request> = emptyList(),
    val errorMsg: String? = null
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

  private val identificationErrorMsg = "An error occurred during your identification."
  private val listingErrorMsg = "An error occurred while loading proposals and requests."
  private val generalError =
      "An error occurred during your identification and while loading proposals and requests."

  init {
    // Load all initial data when the ViewModel is created.
    viewModelScope.launch { load() }
  }

  /**
   * Loads and prepares data for the Home UI state.
   *
   * This function fetches proposals and requests from the listing repository, filters and sorts
   * them to get the top 10 active items, and retrieves a personalized welcome message. It then
   * updates the [uiState] with this data. In case of any errors during data fetching, it logs a
   * warning and falls back to a default empty state.
   */
  fun load() {
    viewModelScope.launch {
      try {
        val allProposals = listingRepository.getProposals()
        val allRequests = listingRepository.getRequests()
        val welcomeMsg = getWelcomeMsg()

        val topProposals =
            allProposals.filter { it.isActive }.sortedByDescending { it.createdAt }.take(10)

        val topRequests =
            allRequests.filter { it.isActive }.sortedByDescending { it.createdAt }.take(10)

        _uiState.update { current ->
          current.copy(
              welcomeMessage = welcomeMsg ?: current.welcomeMessage,
              proposals = topProposals,
              requests = topRequests
              // subjects stays whatever it was (currently the default)
              )
        }
      } catch (e: Exception) {
        Log.w("HomePageViewModel", "Failed to build HomeUiState, using fallback", e)
        _uiState.update { current ->
          current.copy(
              // keep existing subjects and welcomeMessage if you want,
              // but reset proposals/requests to safe defaults
              proposals = emptyList(),
              requests = emptyList())
        }
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
   * Builds the welcome message displayed to the user.
   *
   * This function attempts to retrieve the current user's name and returns a personalized welcome
   * message if the name is available. If the username cannot be fetched, it falls back to a generic
   * welcome message.
   *
   * @return A welcome message string, personalized when possible, or null if user lookup failed.
   */
  private suspend fun getWelcomeMsg(): String? {
    val userName = runCatching { getUserName() }.getOrNull()
    // If we got a user ID but no profile, return generic message
    // If we couldn't get user ID at all, return null to keep previous message
    val userId = UserSessionManager.getCurrentUserId()
    return if (userId != null) {
      if (userName != null) "Welcome back, $userName!" else "Welcome back!"
    } else {
      null // No user ID means temporary auth issue, keep previous message
    }
  }
}
