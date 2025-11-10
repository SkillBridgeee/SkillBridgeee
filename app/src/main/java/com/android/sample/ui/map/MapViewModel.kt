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
 * @param myProfile The current user's profile to show on the map
 * @param selectedProfile The profile selected when clicking a booking marker
 * @param isLoading Whether data is currently being loaded
 * @param errorMessage Error message if loading fails
 * @param bookingPins List of booking pins for the current user's bookings
 */
data class MapUiState(
    val userLocation: LatLng = LatLng(46.5196535, 6.6322734), // Default to Lausanne/EPFL
    val profiles: List<Profile> = emptyList(),
    val myProfile: Profile? = null,
    val selectedProfile: Profile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bookingPins: List<BookingPin> = emptyList(),
)

/**
 * Represents a booking pin on the map.
 *
 * @param bookingId The ID of the booking
 * @param position The geographical position of the pin
 * @param title The title to display on the pin
 * @param snippet An optional snippet to display on the pin
 * @param profile The associated user profile for the booking
 */
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
 *
 * @param profileRepository The repository used to fetch user profiles.
 * @param bookingRepository The repository used to fetch bookings.
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
          _uiState.value =
              _uiState.value.copy(
                  myProfile = me, userLocation = LatLng(loc.latitude, loc.longitude))
        }
      } catch (_: Exception) {
        _uiState.value =
            _uiState.value.copy(isLoading = false, errorMessage = "Failed to load user locations")
      }
    }
  }

  /** Loads all bookings from the repository and updates the map state with booking pins. */
  fun loadBookings() {
    viewModelScope.launch {
      try {
        val currentUserId = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        if (currentUserId == null) {
          _uiState.value = _uiState.value.copy(isLoading = false, bookingPins = emptyList())
          return@launch
        }

        val allBookings = bookingRepository.getAllBookings()
        // Filter to only show bookings where current user is involved
        val userBookings =
            allBookings.filter { booking ->
              booking.bookerId == currentUserId || booking.listingCreatorId == currentUserId
            }

        val pins =
            userBookings.mapNotNull { booking ->
              // Show the location of the OTHER person in the booking
              val otherUserId =
                  if (booking.bookerId == currentUserId) {
                    booking.listingCreatorId
                  } else {
                    booking.bookerId
                  }

              val otherProfile = profileRepository.getProfileById(otherUserId)
              val loc = otherProfile?.location
              if (loc != null && isValidLatLng(loc.latitude, loc.longitude)) {
                BookingPin(
                    bookingId = booking.bookingId,
                    position = LatLng(loc.latitude, loc.longitude),
                    title = otherProfile.name ?: "Session",
                    snippet = otherProfile.description.takeIf { it.isNotBlank() },
                    profile = otherProfile)
              } else null
            }
        _uiState.value = _uiState.value.copy(bookingPins = pins)
      } catch (e: Exception) {
        // Silently handle errors (e.g., missing Firestore indexes, no bookings, network issues)
        // The map will simply not show booking pins, which is acceptable
        _uiState.value = _uiState.value.copy(bookingPins = emptyList())
        // Log for debugging but don't show error to user since map itself works fine
        println("MapViewModel: Could not load bookings - ${e.message}")
      } finally {
        _uiState.value = _uiState.value.copy(isLoading = false)
      }
    }
  }

  /**
   * Selects a profile when a booking marker is clicked. This will show the profile card at the
   * bottom of the map.
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

  /**
   * Checks if the given latitude and longitude represent a valid geographical location.
   *
   * @param lat The latitude to check.
   * @param lng The longitude to check.
   */
  private fun isValidLatLng(lat: Double, lng: Double): Boolean {
    return !lat.isNaN() && !lng.isNaN() && lat in -90.0..90.0 && lng in -180.0..180.0
  }
}
