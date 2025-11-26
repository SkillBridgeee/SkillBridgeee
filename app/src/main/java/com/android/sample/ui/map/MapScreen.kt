package com.android.sample.ui.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.sample.model.booking.name
import com.android.sample.model.user.Profile
import com.android.sample.ui.map.MapScreenTestTags.BOOKING_MARKER_PREFIX
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

object MapScreenTestTags {
  const val MAP_SCREEN = "map_screen"
  const val MAP_VIEW = "map_view"
  const val LOADING_INDICATOR = "loading_indicator"
  const val ERROR_MESSAGE = "error_message"
  const val BOOKING_INFO_WINDOW = "booking_info_window_"
  const val BOOKING_DETAILS_DIALOG = "booking_details_dialog"
  const val DIALOG_CLOSE_BUTTON = "dialog_close_button"

  const val BOOKING_MARKER_PREFIX = "booking_marker_"
  const val USER_PROFILE_MARKER = "user_profile_marker"
}

/**
 * MapScreen displays a Google Map centered on a specific location.
 *
 * Features:
 * - Shows user's real-time GPS location (blue dot) when permission granted
 * - Shows user's profile location (blue marker)
 * - Shows all user's bookings (red markers)
 * - Clicking a booking shows a profile card
 * - Supports zoom and pan gestures
 *
 * @param modifier Optional modifier for the screen
 * @param viewModel The MapViewModel instance
 * @param onProfileClick Callback when a profile card is clicked (for future navigation)
 * @param requestLocationOnStart Whether to request location permission on first composition
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
    onProfileClick: (String) -> Unit = {},
    requestLocationOnStart: Boolean = false
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(modifier = modifier.testTag(MapScreenTestTags.MAP_SCREEN)) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      // Google Map
      val myProfile = uiState.myProfile

      MapView(
          centerLocation = uiState.userLocation,
          bookingPins = uiState.bookingPins,
          myProfile = myProfile,
          onPinClicked = { position -> viewModel.selectPinPosition(position) },
          onMapClicked = { viewModel.clearSelection() },
          requestLocationOnStart = requestLocationOnStart)

      // Loading indicator
      if (uiState.isLoading) {
        CircularProgressIndicator(
            modifier =
                Modifier.align(Alignment.Center).testTag(MapScreenTestTags.LOADING_INDICATOR))
      }

      // Error message
      uiState.errorMessage?.let { error ->
        Card(
            modifier =
                Modifier.align(Alignment.TopCenter)
                    .padding(16.dp)
                    .testTag(MapScreenTestTags.ERROR_MESSAGE),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer)) {
              Text(
                  text = error,
                  modifier = Modifier.padding(16.dp),
                  color = MaterialTheme.colorScheme.onErrorContainer)
            }
      }

      // Info windows for bookings at selected pin position
      uiState.selectedPinPosition?.let { position ->
        val bookingsAtPosition = uiState.bookingPins.filter { it.position == position }
        BookingInfoWindows(
            bookings = bookingsAtPosition,
            onBookingClick = { pin -> viewModel.selectBookingPin(pin) },
            modifier = Modifier.align(Alignment.Center).padding(bottom = 100.dp))
      }

      // Booking details dialog - shows when clicking on an info window
      if (uiState.showBookingDetailsDialog && uiState.selectedBookingPin != null) {
        BookingDetailsDialog(
            bookingPin = uiState.selectedBookingPin!!,
            onDismiss = { viewModel.hideBookingDetailsDialog() })
      }
    }
  }
}

/**
 * Displays the Google Map centered on the users location.
 *
 * @param centerLocation The default center location of the map.
 * @param bookingPins List of booking pins to display on the map.
 * @param myProfile The current user's profile to show on the map.
 * @param onPinClicked Callback when a pin position is clicked (to show info windows).
 * @param onMapClicked Callback when the map itself is clicked (to clear selections).
 * @param requestLocationOnStart Whether to request location permission on first composition.
 * @param permissionChecker Injectable function to check if permission is granted. Defaults to
 *   checking ACCESS_FINE_LOCATION via ContextCompat. Useful for testing.
 * @param permissionRequester Injectable function to request a permission. Defaults to using the
 *   permission launcher. Useful for testing.
 */
@Composable
private fun MapView(
    centerLocation: LatLng,
    bookingPins: List<BookingPin>,
    myProfile: Profile?,
    onPinClicked: (LatLng) -> Unit,
    onMapClicked: () -> Unit = {},
    requestLocationOnStart: Boolean = false,
    permissionChecker: @Composable () -> Boolean = {
      val context = androidx.compose.ui.platform.LocalContext.current
      androidx.core.content.ContextCompat.checkSelfPermission(
          context, Manifest.permission.ACCESS_FINE_LOCATION) ==
          android.content.pm.PackageManager.PERMISSION_GRANTED
    },
    permissionRequester: ((String) -> Unit)? = null
) {
  // Get initial permission state using the injected checker
  val initialPermissionState = permissionChecker()

  // Track location permission state - initialized with checker result
  var hasLocationPermission by remember { mutableStateOf(initialPermissionState) }

  // Permission launcher that updates local state
  val permissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        hasLocationPermission = isGranted
      }

  // Wire default requester to the launcher if the caller didn't override
  val requester =
      remember(permissionLauncher, permissionRequester) {
        permissionRequester ?: { permission: String -> permissionLauncher.launch(permission) }
      }

  // Request location permission - reacts to requestLocationOnStart and hasLocationPermission
  LaunchedEffect(requestLocationOnStart, hasLocationPermission) {
    if (requestLocationOnStart && !hasLocationPermission) {
      try {
        requester(Manifest.permission.ACCESS_FINE_LOCATION)
      } catch (e: Exception) {
        android.util.Log.w(
            "MapScreen", "Permission launcher unavailable in this environment: ${e.message}")
      }
    }
  }

  // Camera position state
  val cameraPositionState = rememberCameraPositionState()

  val profileLatLng =
      myProfile
          ?.location
          ?.takeIf { it.latitude != 0.0 || it.longitude != 0.0 }
          ?.let { LatLng(it.latitude, it.longitude) }

  val target = profileLatLng ?: centerLocation

  LaunchedEffect(target) {
    if (cameraPositionState.position.target != target) {
      cameraPositionState.position = CameraPosition.fromLatLngZoom(target, 12f)
    }
  }

  // Map settings
  val mapUiSettings =
      MapUiSettings(
          zoomControlsEnabled = true,
          zoomGesturesEnabled = true,
          scrollGesturesEnabled = true,
          rotationGesturesEnabled = true,
          tiltGesturesEnabled = true,
          myLocationButtonEnabled = hasLocationPermission)

  val mapProperties = MapProperties(isMyLocationEnabled = hasLocationPermission)

  GoogleMap(
      modifier = Modifier.fillMaxSize().testTag(MapScreenTestTags.MAP_VIEW),
      cameraPositionState = cameraPositionState,
      uiSettings = mapUiSettings,
      properties = mapProperties,
      onMapClick = { onMapClicked() }) {
        // Booking markers - show where the user has sessions (red markers)
        bookingPins.forEach { pin ->
          Marker(
              state = MarkerState(position = pin.position),
              title = pin.title,
              snippet = pin.snippet,
              icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
              onClick = {
                onPinClicked(pin.position)
                true // Consume the event to prevent map click
              },
              tag = BOOKING_MARKER_PREFIX + pin.bookingId)
        }
        // User's profile location marker (blue pinpoint)
        myProfile?.location?.let { loc ->
          if (loc.latitude != 0.0 || loc.longitude != 0.0) {
            Marker(
                state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                title = myProfile.name ?: "Me",
                snippet = loc.name,
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                tag = MapScreenTestTags.USER_PROFILE_MARKER)
          }
        }
      }
}

/**
 * Displays info windows for bookings at the selected pin position. If multiple bookings are at the
 * same location, they are stacked vertically.
 *
 * @param bookings List of booking pins at the selected location.
 * @param onBookingClick Callback when an info window is clicked.
 * @param modifier Modifier for the info windows container.
 */
@Composable
private fun BookingInfoWindows(
    bookings: List<BookingPin>,
    onBookingClick: (BookingPin) -> Unit,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)) {
        bookings.forEach { booking ->
          BookingInfoWindow(booking = booking, onClick = { onBookingClick(booking) })
        }
      }
}

/**
 * Displays a single info window for a booking, similar to Google Maps info windows.
 *
 * @param booking The booking to display.
 * @param onClick Callback when the info window is clicked.
 */
@Composable
private fun BookingInfoWindow(booking: BookingPin, onClick: () -> Unit) {
  Card(
      modifier = Modifier.testTag(MapScreenTestTags.BOOKING_INFO_WINDOW + booking.bookingId),
      shape = RoundedCornerShape(8.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      onClick = onClick) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(12.dp)) {
          Text(
              text = booking.title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              maxLines = 1)

          booking.profile?.name?.let { name ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1)
          }
        }
      }
}

/**
 * Dialog that displays detailed information about a booking pin.
 *
 * @param bookingPin The booking pin to display details for
 * @param onDismiss Callback when the dialog is dismissed
 */
@Composable
private fun BookingDetailsDialog(bookingPin: BookingPin, onDismiss: () -> Unit) {
  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier =
                Modifier.fillMaxWidth(0.9f).testTag(MapScreenTestTags.BOOKING_DETAILS_DIALOG),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp) {
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .verticalScroll(rememberScrollState())
                          .padding(24.dp)) {
                    // Header with title and close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              text = "Booking Details",
                              style = MaterialTheme.typography.headlineSmall,
                              fontWeight = FontWeight.Bold)
                          IconButton(
                              onClick = onDismiss,
                              modifier = Modifier.testTag(MapScreenTestTags.DIALOG_CLOSE_BUTTON)) {
                                Icon(
                                    imageVector = Icons.Default.Close, contentDescription = "Close")
                              }
                        }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    // Booking title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                          imageVector = Icons.Default.LocationOn,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(24.dp))
                      Spacer(modifier = Modifier.width(8.dp))
                      Text(
                          text = bookingPin.title,
                          style = MaterialTheme.typography.titleLarge,
                          fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location
                    if (!bookingPin.snippet.isNullOrBlank()) {
                      Text(
                          text = "Location",
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(text = bookingPin.snippet, style = MaterialTheme.typography.bodyLarge)
                      Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Booking time and date information
                    bookingPin.booking?.let { booking ->
                      HorizontalDivider()
                      Spacer(modifier = Modifier.height(16.dp))

                      // Date
                      Text(
                          text = "Date",
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text =
                              java.text
                                  .SimpleDateFormat(
                                      "EEEE, MMMM d, yyyy", java.util.Locale.getDefault())
                                  .format(booking.sessionStart),
                          style = MaterialTheme.typography.bodyLarge)

                      Spacer(modifier = Modifier.height(12.dp))

                      // Time
                      Text(
                          text = "Time",
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text =
                              "${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(booking.sessionStart)} - ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(booking.sessionEnd)}",
                          style = MaterialTheme.typography.bodyLarge)

                      Spacer(modifier = Modifier.height(12.dp))

                      // Status
                      Text(
                          text = "Status",
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text = booking.status.name(),
                          style = MaterialTheme.typography.bodyLarge,
                          color = MaterialTheme.colorScheme.primary,
                          fontWeight = FontWeight.SemiBold)

                      Spacer(modifier = Modifier.height(12.dp))

                      // Price
                      Text(
                          text = "Price",
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text =
                              "$${String.format(java.util.Locale.getDefault(), "%.2f", booking.price)}",
                          style = MaterialTheme.typography.bodyLarge,
                          fontWeight = FontWeight.SemiBold)
                    }

                    // Other person info
                    bookingPin.profile?.let { profile ->
                      HorizontalDivider()
                      Spacer(modifier = Modifier.height(16.dp))

                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Session Partner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                      }

                      Spacer(modifier = Modifier.height(12.dp))

                      Text(
                          text = "Name",
                          style = MaterialTheme.typography.labelMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text = profile.name ?: "Unknown User",
                          style = MaterialTheme.typography.bodyLarge)

                      if (profile.levelOfEducation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Education Level",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profile.levelOfEducation,
                            style = MaterialTheme.typography.bodyLarge)
                      }
                    }
                  }
            }
      }
}
