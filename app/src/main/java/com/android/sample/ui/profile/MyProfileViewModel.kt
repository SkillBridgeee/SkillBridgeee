package com.android.sample.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.HttpClientProvider
import com.android.sample.model.map.GpsLocationProvider
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

/** UI state for the MyProfile screen. Holds all data needed to edit a profile */
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
    val updateError: String? = null
) {
  // Checks if all fields are valid
  val isValid: Boolean
    get() =
        invalidNameMsg == null &&
            invalidEmailMsg == null &&
            invalidLocationMsg == null &&
            invalidDescMsg == null &&
            name?.isNotBlank() == true &&
            email?.isNotBlank() == true &&
            selectedLocation != null &&
            description?.isNotBlank() == true
}

// ViewModel to manage profile editing logic and state
class MyProfileViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val locationRepository: LocationRepository =
        NominatimLocationRepository(HttpClientProvider.client),
    private val userId: String = Firebase.auth.currentUser?.uid ?: ""
) : ViewModel() {

  companion object {
    private const val TAG = "MyProfileViewModel"
  }

  // Holds the current UI state
  private val _uiState = MutableStateFlow(MyProfileUIState())
  val uiState: StateFlow<MyProfileUIState> = _uiState.asStateFlow()

  private var locationSearchJob: Job? = null
  private val locationSearchDelayTime: Long = 1000

  private val nameMsgError = "Name cannot be empty"
  private val emailEmptyMsgError = "Email cannot be empty"
  private val emailInvalidMsgError = "Email is not in the right format"
  private val locationMsgError = "Location cannot be empty"
  private val descMsgError = "Description cannot be empty"

  /** Loads the profile data (to be implemented) */
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
      } catch (e: Exception) {
        Log.e("MyProfileViewModel", "Error loading MyProfile by ID: $currentId", e)
      }
    }
  }

  /**
   * Edits a Profile.
   *
   * @return true if the update process was started, false if validation failed.
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

    editProfileToRepository(userId = currentId, profile = profile)
  }

  /**
   * Edits a Profile in the repository.
   *
   * @param userId The ID of the profile to be edited.
   * @param profile The Profile object containing the new values.
   */
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

  // Set all messages error, if invalid field
  fun setError() {
    _uiState.update { currentState ->
      currentState.copy(
          invalidNameMsg = currentState.name?.let { if (it.isBlank()) nameMsgError else null },
          invalidEmailMsg = validateEmail(currentState.email ?: ""),
          invalidLocationMsg =
              if (currentState.selectedLocation == null) locationMsgError else null,
          invalidDescMsg =
              currentState.description?.let { if (it.isBlank()) descMsgError else null })
    }
  }

  // Updates the name and validates it
  fun setName(name: String) {
    _uiState.value =
        _uiState.value.copy(
            name = name, invalidNameMsg = if (name.isBlank()) nameMsgError else null)
  }

  // Updates the email and validates it
  fun setEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email, invalidEmailMsg = validateEmail(email))
  }

  // Updates the desc and validates it
  fun setDescription(desc: String) {
    _uiState.value =
        _uiState.value.copy(
            description = desc, invalidDescMsg = if (desc.isBlank()) descMsgError else null)
  }

  // Checks if the email format is valid
  private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return email.matches(emailRegex.toRegex())
  }

  // Return the good error message corresponding of the given input
  private fun validateEmail(email: String): String? {
    return when {
      email.isBlank() -> emailEmptyMsgError
      !isValidEmail(email) -> emailInvalidMsgError
      else -> null
    }
  }

  // Update the selected location and the locationQuery
  fun setLocation(location: Location) {
    _uiState.value = _uiState.value.copy(selectedLocation = location, locationQuery = location.name)
  }

  /**
   * Updates the location query in the UI state and fetches matching location suggestions.
   *
   * This function updates the current `locationQuery` value and triggers a search operation if the
   * query is not empty. The search is performed asynchronously within the `viewModelScope` using
   * the [locationRepository].
   *
   * @param query The new location search query entered by the user.
   * @see locationRepository
   * @see viewModelScope
   */
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

  /**
   * Fetch a GPS fix using the provided [GpsLocationProvider]. Updates the UI state with a simple
   * lat,lng string in `locationQuery` on success and sets an appropriate `invalidLocationMsg` on
   * failure (permission/error).
   */
  fun fetchLocationFromGps(provider: GpsLocationProvider) {
    viewModelScope.launch {
      try {
        // attempt to get a location (provider may block) — consider adding a timeout here if
        // desired
        val androidLoc = provider.getCurrentLocation()
        if (androidLoc != null) {
          val mapLocation =
              com.android.sample.model.map.Location(
                  latitude = androidLoc.latitude,
                  longitude = androidLoc.longitude,
                  name = "${androidLoc.latitude}, ${androidLoc.longitude}")
          _uiState.update {
            it.copy(
                selectedLocation = mapLocation,
                locationQuery = mapLocation.name,
                invalidLocationMsg = null)
          }
        } else {
          _uiState.update { it.copy(invalidLocationMsg = "Failed to obtain GPS location") }
        }
      } catch (se: SecurityException) {
        _uiState.update { it.copy(invalidLocationMsg = "Location permission denied") }
      } catch (e: Exception) {
        _uiState.update { it.copy(invalidLocationMsg = "Failed to obtain GPS location") }
      }
    }
  }
}
