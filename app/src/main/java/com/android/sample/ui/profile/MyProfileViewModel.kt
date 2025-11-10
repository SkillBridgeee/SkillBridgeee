package com.android.sample.ui.profile

import android.location.Address
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.HttpClientProvider
import com.android.sample.model.listing.Listing
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.map.NominatimLocationRepository
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Message constants (kept at file start so tests can reference exact text)
const val NAME_EMPTY_MSG = "Name cannot be empty"
const val EMAIL_EMPTY_MSG = "Email cannot be empty"
const val EMAIL_INVALID_MSG = "Email is not in the right format"
const val LOCATION_EMPTY_MSG = "Location cannot be empty"
const val DESC_EMPTY_MSG = "Description cannot be empty"
const val GPS_FAILED_MSG = "Failed to obtain GPS location"
const val LOCATION_PERMISSION_DENIED_MSG = "Location permission denied"
const val UPDATE_PROFILE_FAILED_MSG = "Failed to update profile. Please try again."

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
    val updateError: String? = null,
    val listings: List<Listing> = emptyList(),
    val listingsLoading: Boolean = false,
    val listingsLoadError: String? = null,
    val updateSuccess: Boolean = false
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
  private val locationMsgError = "Location cannot be empty"
  private val descMsgError = "Description cannot be empty"

  private var originalProfile: Profile? = null

  /** Loads the profile data (to be implemented) */
  fun loadProfile(profileUserId: String? = null) {
    val currentId = profileUserId ?: userId
    viewModelScope.launch {
      try {
        val profile = profileRepository.getProfile(userId = currentId)
        originalProfile = profile
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
    val newProfile =
        Profile(
            userId = currentId,
            name = state.name ?: "",
            email = state.email ?: "",
            location = state.selectedLocation!!,
            description = state.description ?: "")

    val original = originalProfile
    if (original != null && !hasProfileChanged(original, newProfile)) {
      return
    }

    originalProfile = newProfile
    editProfileToRepository(currentId, newProfile)
  }

  /**
   * Checks if the profile has changed compared to the original.
   *
   * @param original The original Profile object.
   * @param updated The updated Profile object.
   */
  private fun hasProfileChanged(original: Profile, updated: Profile): Boolean {
    return original.name != updated.name ||
        original.email != updated.email ||
        original.description != updated.description ||
        original.location.name != updated.location.name ||
        original.location.latitude != updated.location.latitude ||
        original.location.longitude != updated.location.longitude
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
        _uiState.update { it.copy(updateSuccess = true) }
      } catch (e: Exception) {
        Log.e(TAG, "Error updating profile for user: $userId", e)
        _uiState.update { it.copy(updateError = UPDATE_PROFILE_FAILED_MSG) }
      }
    }
  }

  // Set all messages error, if invalid field
  fun setError() {
    _uiState.update {
      it.copy(
          invalidNameMsg = if (it.name.isNullOrBlank()) nameMsgError else null,
          invalidEmailMsg = validateEmail(it.email ?: ""),
          invalidLocationMsg = if (it.selectedLocation == null) locationMsgError else null,
          invalidDescMsg = if (it.description.isNullOrBlank()) descMsgError else null)
    }
  }

  // Updates the name and validates it
  fun setName(name: String) {
    _uiState.value =
        _uiState.value.copy(
            name = name, invalidNameMsg = if (name.isBlank()) NAME_EMPTY_MSG else null)
  }

  // Updates the email and validates it
  fun setEmail(email: String) {
    _uiState.value = _uiState.value.copy(email = email, invalidEmailMsg = validateEmail(email))
  }

  // Updates the desc and validates it
  fun setDescription(desc: String) {
    _uiState.value =
        _uiState.value.copy(
            description = desc, invalidDescMsg = if (desc.isBlank()) DESC_EMPTY_MSG else null)
  }

  /** Validates email format */
  private fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return email.matches(emailRegex.toRegex())
  }

  // Return the good error message corresponding of the given input
  private fun validateEmail(email: String): String? {
    return when {
      email.isBlank() -> EMAIL_EMPTY_MSG
      !isValidEmail(email) -> EMAIL_INVALID_MSG
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
              invalidLocationMsg = LOCATION_EMPTY_MSG,
              selectedLocation = null)
    }
  }

  /**
   * Fetches the current location using GPS and updates the UI state accordingly.
   *
   * This function attempts to retrieve the current GPS location using the provided
   * [GpsLocationProvider]. If successful, it uses a [Geocoder] to convert the latitude and
   * longitude into a human-readable address. The UI state is then updated with the fetched location
   * details. If the location cannot be obtained or if there are permission issues, appropriate
   * error messages are set in the UI state.
   *
   * @param provider The [GpsLocationProvider] used to obtain the current GPS location.
   * @param context The Android context used for geocoding.
   */
  @Suppress("DEPRECATION")
  fun fetchLocationFromGps(provider: GpsLocationProvider, context: android.content.Context) {
    viewModelScope.launch {
      try {
        val androidLoc = provider.getCurrentLocation()
        if (androidLoc != null) {
          val geocoder = Geocoder(context, Locale.getDefault())
          val addresses: List<Address> =
              geocoder.getFromLocation(androidLoc.latitude, androidLoc.longitude, 1)?.toList()
                  ?: emptyList()
          val addressText =
              if (addresses.isNotEmpty()) {
                // Take the first address from the selected list which is the most relevant
                val address = addresses[0]
                // Build a readable address string
                listOfNotNull(address.locality, address.adminArea, address.countryName)
                    .joinToString(", ")
              } else {
                "${androidLoc.latitude}, ${androidLoc.longitude}"
              }

          val mapLocation =
              Location(
                  latitude = androidLoc.latitude,
                  longitude = androidLoc.longitude,
                  name = addressText)

          _uiState.update {
            it.copy(
                selectedLocation = mapLocation,
                locationQuery = addressText,
                invalidLocationMsg = null)
          }
        } else {
          _uiState.update { it.copy(invalidLocationMsg = GPS_FAILED_MSG) }
        }
      } catch (_: SecurityException) {
        _uiState.update { it.copy(invalidLocationMsg = LOCATION_PERMISSION_DENIED_MSG) }
      } catch (_: Exception) {
        _uiState.update { it.copy(invalidLocationMsg = GPS_FAILED_MSG) }
      }
    }
  }

  /**
   * Handles the scenario when location permission is denied by updating the UI state with an
   * appropriate error message.
   */
  fun onLocationPermissionDenied() {
    _uiState.update { it.copy(invalidLocationMsg = LOCATION_PERMISSION_DENIED_MSG) }
  }

  /** Clears the update success flag in the UI state. */
  fun clearUpdateSuccess() {
    _uiState.update { it.copy(updateSuccess = false) }
  }
}
