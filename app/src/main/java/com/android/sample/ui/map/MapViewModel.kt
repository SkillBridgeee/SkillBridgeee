package com.android.sample.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingRepositoryProvider
import com.android.sample.model.listing.ListingRepository
import com.android.sample.model.listing.ListingRepositoryProvider
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
 * @param selectedPinPosition The position of the selected pin (to show info windows for bookings at
 *   that location)
 * @param selectedBookingPin The booking pin selected when clicking on an info window (for showing
 *   booking details)
 * @param showBookingDetailsDialog Whether to show the booking details dialog
 * @param isLoading Whether data is currently being loaded
 * @param errorMessage Error message if loading fails
 * @param bookingPins List of booking pins for the current user's bookings
 */
data class MapUiState(
    val userLocation: LatLng = LatLng(46.5196535, 6.6322734), // Default to Lausanne/EPFL
    val profiles: List<Profile> = emptyList(),
    val myProfile: Profile? = null,
    val selectedPinPosition: LatLng? = null,
    val selectedBookingPin: BookingPin? = null,
    val showBookingDetailsDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bookingPins: List<BookingPin> = emptyList(),
    val bookingsAtSelectedPosition: List<BookingPin> = emptyList(),
)

/**
 * Represents a booking pin on the map.
 *
 * @param bookingId The ID of the booking
 * @param position The geographical position of the pin
 * @param title The title to display on the pin
 * @param snippet An optional snippet to display on the pin
 * @param profile The associated user profile for the booking
 * @param booking The full booking object for displaying details
 */
data class BookingPin(
    val bookingId: String,
    val position: LatLng,
    val title: String,
    val snippet: String? = null,
    val profile: Profile? = null,
    val booking: com.android.sample.model.booking.Booking? = null
)

/**
 * ViewModel for the Map screen.
 *
 * Manages the state of the map, including user locations and profile markers. Loads all user
 * profiles from the repository and displays them on the map.
 *
 * @param profileRepository The repository used to fetch user profiles.
 * @param bookingRepository The repository used to fetch bookings.
 * @param listingRepository The repository used to fetch listings.
 */
class MapViewModel(
    private val profileRepository: ProfileRepository = ProfileRepositoryProvider.repository,
    private val bookingRepository: BookingRepository = BookingRepositoryProvider.repository,
    private val listingRepository: ListingRepository = ListingRepositoryProvider.repository
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
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull()
        val me = profiles.firstOrNull { it.userId == uid }

        // Update profiles and myProfile
        _uiState.value = _uiState.value.copy(profiles = profiles, myProfile = me, isLoading = false)

        // Update camera location if user has a valid location
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
              // Get the listing to show its location (where the session takes place)
              val listing = listingRepository.getListing(booking.associatedListingId)

              // Get the OTHER person in the booking for display info
              val otherUserId =
                  if (booking.bookerId == currentUserId) {
                    booking.listingCreatorId
                  } else {
                    booking.bookerId
                  }

              val otherProfile = profileRepository.getProfileById(otherUserId)

              // Use the listing's location (where session takes place) instead of profile location
              val loc = listing?.location
              if (listing != null && loc != null && isValidLatLng(loc.latitude, loc.longitude)) {
                BookingPin(
                    bookingId = booking.bookingId,
                    position = LatLng(loc.latitude, loc.longitude),
                    title = listing.title.ifBlank { otherProfile?.name ?: "Session" },
                    snippet = "${loc.name} - with ${otherProfile?.name ?: "Unknown"}",
                    profile = otherProfile,
                    booking = booking)
              } else null
            }
        _uiState.value = _uiState.value.copy(bookingPins = pins)
      } catch (e: Exception) {
        // Silently handle errors (e.g., missing Firestore indexes, no bookings, network issues)
        // The map will simply not show booking pins, which is acceptable
        _uiState.value = _uiState.value.copy(bookingPins = emptyList())
        // Log for debugging but don't show error to user since map itself works fine
        Log.w("MapViewModel", "Could not load bookings: ${e.message}", e)
      } finally {
        _uiState.value = _uiState.value.copy(isLoading = false)
      }
    }
  }

  /**
   * Selects a pin position when a marker is clicked. This will show the info windows for all
   * bookings at that location.
   *
   * @param position The pin position to select, or null to deselect
   */
  fun selectPinPosition(position: LatLng?) {
    val bookingsAtPosition =
        if (position != null) {
          _uiState.value.bookingPins.filter { it.position == position }
        } else {
          emptyList()
        }
    _uiState.value =
        _uiState.value.copy(
            selectedPinPosition = position, bookingsAtSelectedPosition = bookingsAtPosition)
  }

  /**
   * Selects a booking pin from an info window and shows the details dialog.
   *
   * @param pin The booking pin to select
   */
  fun selectBookingPin(pin: BookingPin) {
    _uiState.value = _uiState.value.copy(selectedBookingPin = pin, showBookingDetailsDialog = true)
  }

  /** Hides the booking details dialog. */
  fun hideBookingDetailsDialog() {
    _uiState.value = _uiState.value.copy(showBookingDetailsDialog = false)
  }

  /**
   * Clears all selections (pin position and booking pin). Called when clicking on the map or when
   * dismissing dialogs.
   */
  fun clearSelection() {
    _uiState.value =
        _uiState.value.copy(
            selectedPinPosition = null,
            selectedBookingPin = null,
            showBookingDetailsDialog = false,
            bookingsAtSelectedPosition = emptyList())
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
