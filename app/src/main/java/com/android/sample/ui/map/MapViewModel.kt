package com.android.sample.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI state for the Map screen.
 *
 * @param userLocation The current user's location (camera position)
 * @param profiles List of all user profiles to display on the map
 * @param selectedProfile The currently selected profile when a marker is clicked
 * @param isLoading Whether data is currently being loaded
 * @param errorMessage Error message if loading fails
 */
data class MapUiState(
    val userLocation: LatLng = LatLng(46.5196535, 6.6322734), // Default to Lausanne/EPFL
    val profiles: List<Profile> = emptyList(),
    val selectedProfile: Profile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bookingPins: List<BookingPin> = emptyList(),
)

data class BookingPin(
    val bookingId: String,
    val position: LatLng,
    val title: String,
    val snippet: String? = null,
    val profile: Profile? = null
)

/**
 * ViewModel for the Map screen.
 *
 * Manages the state of the map, including user locations and profile markers. Loads all user
 * profiles from the repository and displays them on the map.
 */
class MapViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val bookingRepository: BookingRepository = BookingRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(MapUiState())
  val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

  init {
    loadProfiles()
    loadBookings()
  }

  /** Loads all user profiles from the repository and updates the map state. */
  fun loadProfiles() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      try {
        val profiles = profileRepository.getAllProfiles()
        _uiState.value = _uiState.value.copy(profiles = profiles, isLoading = false)
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        val me = profiles.firstOrNull { it.userId == uid }
        val loc = me?.location
        if (loc != null && (loc.latitude != 0.0 || loc.longitude != 0.0)) {
          _uiState.value = _uiState.value.copy(userLocation = LatLng(loc.latitude, loc.longitude))
        }
      } catch (_: Exception) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, errorMessage = "Failed to load user locations")
      }
    }
  }

  fun loadBookings() {
    viewModelScope.launch {
      try {
        val bookings = bookingRepository.getAllBookings()
        val pins =
            bookings.mapNotNull { booking ->
              val tutor = profileRepository.getProfileById(booking.listingCreatorId)
              val loc = tutor?.location
              if (loc != null && (loc.latitude != 0.0 || loc.longitude != 0.0)) {
                BookingPin(
                    bookingId = booking.bookingId,
                    position = LatLng(loc.latitude, loc.longitude),
                    title = tutor.name ?: "Session",
                    snippet = tutor.description.takeIf { it.isNotBlank() },
                    profile = tutor)
              } else null
            }
        _uiState.value = _uiState.value.copy(bookingPins = pins)
      } catch (e: Exception) {
        if (_uiState.value.errorMessage == null) {
          _uiState.value = _uiState.value.copy(errorMessage = e.message)
        }
      } finally {
        _uiState.value = _uiState.value.copy(isLoading = false)
      }
    }
  }

  /**
   * Updates the selected profile when a marker is clicked.
   *
   * @param profile The profile to select, or null to deselect
   */
  fun selectProfile(profile: Profile?) {
    _uiState.value = _uiState.value.copy(selectedProfile = profile)
  }

  /**
   * Updates the camera position to a specific location.
   *
   * @param location The location to move the camera to
   */
  fun moveToLocation(location: Location) {
    val latLng = LatLng(location.latitude, location.longitude)
    _uiState.value = _uiState.value.copy(userLocation = latLng)
  }
}
