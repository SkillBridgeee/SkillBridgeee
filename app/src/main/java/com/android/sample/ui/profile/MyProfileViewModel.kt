package com.android.sample.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.HttpClientProvider
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.map.NominatimLocationRepository
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the MyProfile screen.
 *
 * Holds all fields needed for profile display + editing and the user's created listings.
 */
data class MyProfileUIState(
    val userId: String? = null,
    val name: String? = "",
    val email: String? = "",
    val selectedLocation: Location? = null,
    val locationQuery: String = "",
    val locationSuggestions: List<Location> = emptyList(),
    val description: String? = "",
    val invalidNameMsg: String? = null,
    val invalidEmailMsg: String? = null,
    val invalidLocationMsg: String? = null,
    val invalidDescMsg: String? = null,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val updateError: String? = null,
    val listings: List<Listing> = emptyList(),
    val listingsLoading: Boolean = false,
    val listingsLoadError: String? = null
) {
  /** True if all required fields are valid */
  val isValid: Boolean
    get() =
        invalidNameMsg == null &&
            invalidEmailMsg == null &&
            invalidLocationMsg == null &&
            invalidDescMsg == null &&
            !name.isNullOrBlank() &&
            !email.isNullOrBlank() &&
            selectedLocation != null &&
            !description.isNullOrBlank()
}

/**
 * ViewModel controlling the profile screen.
 *
 * Responsibilities:
 * - Load user profile data
 * - Update profile fields
 * - Validate input
 * - Fetch user-created listings to show on profile
 */
class MyProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val locationRepository: LocationRepository =
        NominatimLocationRepository(HttpClientProvider.client),
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository,
    private val userId: String = Firebase.auth.currentUser?.uid ?: ""
) : ViewModel() {

  companion object {
    private const val TAG = "MyProfileViewModel"
  }

  /** Holds current profile UI state */
  private val _uiState = MutableStateFlow(MyProfileUIState())
  val uiState: StateFlow<MyProfileUIState> = _uiState.asStateFlow()

  private var locationSearchJob: Job? = null
  private val locationSearchDelayTime: Long = 1000

  private val nameMsgError = "Name cannot be empty"
  private val emailEmptyMsgError = "Email cannot be empty"
  private val emailInvalidMsgError = "Email is not in the right format"
  private val locationMsgError = "Location cannot be empty"
  private val descMsgError = "Description cannot be empty"

  /**
   * Loads profile information and user's own listings.
   *
   * @param profileUserId Optional — used when viewing another user's profile.
   */
  fun loadProfile(profileUserId: String? = null) {
    val currentId = profileUserId ?: userId
    viewModelScope.launch {
      try {
        val profile = profileRepository.getProfile(userId = currentId)

        _uiState.value =
            MyProfileUIState(
                userId = currentId,
                name = profile?.name,
                email = profile?.email,
                selectedLocation = profile?.location,
                locationQuery = profile?.location?.name ?: "",
                description = profile?.description)

        // Load listings created by this user
        loadUserListings(currentId)
      } catch (e: Exception) {
        Log.e(TAG, "Error loading MyProfile by ID: $currentId", e)
      }
    }
  }

  /**
   * Loads listings created by the given user and updates UI state.
   *
   * Uses a dedicated `listingsLoading` flag so the rest of the screen can remain visible.
   */
  fun loadUserListings(ownerId: String = _uiState.value.userId ?: userId) {
    viewModelScope.launch {
      // set listings loading state (does not affect full-screen isLoading)
      _uiState.update { it.copy(listingsLoading = true, listingsLoadError = null) }
      try {
        val items = listingRepository.getListingsByUser(ownerId).sortedByDescending { it.createdAt }
        _uiState.update {
          it.copy(listings = items, listingsLoading = false, listingsLoadError = null)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error loading listings for user: $ownerId", e)
        _uiState.update {
          it.copy(
              listings = emptyList(),
              listingsLoading = false,
              listingsLoadError = "Failed to load listings.")
        }
      }
    }
  }

  /**
   * Attempts to update the profile.
   *
   * If data is invalid, sets validation messages instead.
   */
  fun editProfile() {
    val state = _uiState.value
    if (!state.isValid) {
      setError()
      return
    }

    val currentId = state.userId ?: userId
    val profile =
        Profile(
            userId = currentId,
            name = state.name ?: "",
            email = state.email ?: "",
            location = state.selectedLocation!!,
            description = state.description ?: "")

    editProfileToRepository(currentId, profile)
  }

  /** Saves updated profile to repository */
  private fun editProfileToRepository(userId: String, profile: Profile) {
    viewModelScope.launch {
      _uiState.update { it.copy(updateError = null) }
      try {
        profileRepository.updateProfile(userId = userId, profile = profile)
      } catch (e: Exception) {
        Log.e(TAG, "Error updating profile for user: $userId", e)
        _uiState.update { it.copy(updateError = "Failed to update profile. Please try again.") }
      }
    }
  }

  /** Fills all validation messages if user tries to save invalid input */
  fun setError() {
    _uiState.update {
      it.copy(
          invalidNameMsg = if (it.name.isNullOrBlank()) nameMsgError else null,
          invalidEmailMsg = validateEmail(it.email ?: ""),
          invalidLocationMsg = if (it.selectedLocation == null) locationMsgError else null,
          invalidDescMsg = if (it.description.isNullOrBlank()) descMsgError else null)
    }
  }

  /** Input field setters + validation */
  fun setName(name: String) {
    _uiState.value =
        _uiState.value.copy(
            name = name, invalidNameMsg = if (name.isBlank()) nameMsgError else null)
  }

  fun setEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email, invalidEmailMsg = validateEmail(email))
  }

  fun setDescription(desc: String) {
    _uiState.value =
        _uiState.value.copy(
            description = desc, invalidDescMsg = if (desc.isBlank()) descMsgError else null)
  }

  /** Validates email format */
  private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return email.matches(emailRegex.toRegex())
  }

  private fun validateEmail(email: String): String? =
      when {
        email.isBlank() -> emailEmptyMsgError
        !isValidEmail(email) -> emailInvalidMsgError
        else -> null
      }

  /** Selects a location from suggestions */
  fun setLocation(location: Location) {
    _uiState.value = _uiState.value.copy(selectedLocation = location, locationQuery = location.name)
  }

  /** Updates location query and performs delayed search for suggestions. */
  fun setLocationQuery(query: String) {
    _uiState.value = _uiState.value.copy(locationQuery = query)
    locationSearchJob?.cancel()

    if (query.isNotEmpty()) {
      locationSearchJob =
          viewModelScope.launch {
            delay(locationSearchDelayTime)
            try {
              val results = locationRepository.search(query)
              _uiState.value =
                  _uiState.value.copy(locationSuggestions = results, invalidLocationMsg = null)
            } catch (_: Exception) {
              _uiState.value = _uiState.value.copy(locationSuggestions = emptyList())
            }
          }
    } else {
      _uiState.value =
          _uiState.value.copy(
              locationSuggestions = emptyList(),
              invalidLocationMsg = locationMsgError,
              selectedLocation = null)
    }
  }
}
