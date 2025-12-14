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
import kotlinx.coroutines.Job
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
  private val numRequestDisplayed = 10
  private val numProposalDisplayed = 10

  private var refreshJob: Job? = null

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
      val welcomeMsg = getWelcomeMsg()
      val proposalsResult = runCatching { getTopProposals(numProposalDisplayed) }
      val requestsResult = runCatching { getTopRequests(numRequestDisplayed) }

      val proposals = proposalsResult.getOrNull().orEmpty()
      val requests = requestsResult.getOrNull().orEmpty()

      val errorMsg =
          when {
            welcomeMsg == null && (proposalsResult.isFailure || requestsResult.isFailure) ->
                generalError
            welcomeMsg == null -> identificationErrorMsg
            proposalsResult.isFailure || requestsResult.isFailure -> listingErrorMsg
            else -> null
          }

      _uiState.update {
        it.copy(
            welcomeMessage = welcomeMsg ?: it.welcomeMessage,
            proposals = proposals,
            requests = requests,
            errorMsg = errorMsg)
      }
    }
  }

  //  /**
  //   * Retrieves the current user's name.
  //   * - Gets the logged-in user's ID from the session manager
  //   * - Fetches the user's profile and returns their name
  //   *
  //   * Returns null if no user is logged in or if the profile cannot be retrieved. Logs a warning
  // and
  //   * safely returns null if an error occurs.
  //   */
  //  private suspend fun getUserName(): String? {
  //    return runCatching {
  //          val userId = UserSessionManager.getCurrentUserId()
  //          if (userId != null) {
  //            profileRepository.getProfile(userId)?.name
  //          } else null
  //        }
  //        .onFailure { Log.e("HomePageViewModel", "Failed to get current profile", it) }
  //        .getOrNull()
  //  }
  //
  //  /**
  //   * Builds the welcome message displayed to the user.
  //   *
  //   * This function attempts to retrieve the current user's name and returns a personalized
  // welcome
  //   * message if the name is available. If the username cannot be fetched, it falls back to a
  // generic
  //   * welcome message.
  //   *
  //   * @return A welcome message string, personalized when possible, or null if user lookup
  // failed.
  //   */
  //  private suspend fun getWelcomeMsg(): String? {
  //    val userName = runCatching { getUserName() }.getOrNull()
  //    val userId = UserSessionManager.getCurrentUserId()
  //    return if (userId != null) {
  //      if (userName != null) "Welcome back, $userName!" else "Welcome back!"
  //    } else {
  //      null // No user ID means temporary auth issue, keep previous message
  //    }
  //  }

  private suspend fun getWelcomeMsg(): String? =
      try {
        val userId = UserSessionManager.getCurrentUserId()
        if (userId == null) throw Exception()
        val userName = profileRepository.getProfile(userId)?.name
        "Welcome back, $userName!"
      } catch (e: Exception) {
        Log.e("HomePageViewModel", "Failed to build welcome message", e)
        null
      }

  /**
   * Retrieves the top proposals from the repository.
   *
   * This function fetches all proposals, keeps only the active ones, then sorts them by creation
   * date in descending order (newest first). Finally, it returns only the first [numProposal] items
   * from the sorted list.
   *
   * @param numProposal The maximum number of proposals to return.
   * @return A list of the most recent active proposals, limited to [numProposal] items.
   */
  private suspend fun getTopProposals(numProposal: Int): List<Proposal> {
    val allProposals = listingRepository.getProposals()
    return allProposals.filter { it.isActive }.sortedByDescending { it.createdAt }.take(numProposal)
  }

  /**
   * Retrieves the top requests from the repository.
   *
   * This function fetches all requests, keeps only the active ones, then sorts them by creation
   * date in descending order (newest first). Finally, it returns only the first [numRequest] items
   * from the sorted list.
   *
   * @param numRequest The maximum number of requests to return.
   * @return A list of the most recent active requests, limited to [numRequest] items.
   */
  private suspend fun getTopRequests(numRequest: Int): List<Request> {
    val allRequests = listingRepository.getRequests()
    return allRequests.filter { it.isActive }.sortedByDescending { it.createdAt }.take(numRequest)
  }

  /**
   * Refreshes the home listing data by loading the latest proposals and requests.
   *
   * This function launches a coroutine in the ViewModel scope to fetch the most recent active
   * proposals and requests. Once the data is retrieved, the UI state is updated with the new lists.
   * If an error occurs during data loading, the function logs a warning and updates the UI state
   * with empty lists as a fallback.
   */
  fun refreshListing() {
    if (refreshJob?.isActive == true) return

    refreshJob =
        viewModelScope.launch {
          try {
            val topProposals = getTopProposals(numProposalDisplayed)
            val topRequests = getTopRequests(numRequestDisplayed)

            _uiState.update { current ->
              current.copy(proposals = topProposals, requests = topRequests)
            }
          } catch (e: Exception) {
            Log.e("HomePageViewModel", "Failed to refresh HomeUiState", e)
            _uiState.update { current -> current.copy(errorMsg = listingErrorMsg) }
          }
        }
  }
}
