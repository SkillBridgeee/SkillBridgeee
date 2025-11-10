package com.android.sample.ui.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
  const val PROFILE_CARD = "profile_card"
  const val PROFILE_NAME = "profile_name"
  const val PROFILE_LOCATION = "profile_location"

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
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
    onProfileClick: (String) -> Unit = {}
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
          onBookingClicked = { pin -> pin.profile?.let { viewModel.selectProfile(it) } })

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

      // Selected profile card at bottom - shows tutor/student info when booking marker clicked
      uiState.selectedProfile?.let { profile ->
        ProfileInfoCard(
            profile = profile,
            onProfileClick = { onProfileClick(profile.userId) },
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp))
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
 * @param onBookingClicked Callback when a booking pin is clicked.
 */
@Composable
private fun MapView(
    centerLocation: LatLng,
    bookingPins: List<BookingPin>,
    myProfile: Profile?,
    onBookingClicked: (BookingPin) -> Unit
) {
  // Track location permission state
  var hasLocationPermission by remember { mutableStateOf(false) }

  // Permission launcher
  val permissionLauncher =
      rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {
          isGranted ->
        hasLocationPermission = isGranted
      }

  // Request location permission on first composition
  // Only if launcher was successfully created (not in test environment)
  LaunchedEffect(Unit) {
    try {
      permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    } catch (_: Exception) {
      // In test environment, permission launcher might fail - that's ok
      // hasLocationPermission will remain false
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
      properties = mapProperties) {
        // Booking markers - show where the user has sessions
        bookingPins.forEach { pin ->
          Marker(
              state = MarkerState(position = pin.position),
              title = pin.title,
              snippet = pin.snippet,
              onClick = {
                onBookingClicked(pin)
                false
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
 * Displays information about the selected profile (tutor/student from booking).
 *
 * @param profile The profile to display.
 * @param onProfileClick Callback when the profile card is clicked.
 * @param modifier Modifier for the profile card.
 */
@Composable
private fun ProfileInfoCard(
    profile: Profile,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier = modifier.fillMaxWidth().testTag(MapScreenTestTags.PROFILE_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      onClick = onProfileClick) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)) {
              Text(
                  text = profile.name ?: "Unknown User",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag(MapScreenTestTags.PROFILE_NAME))

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                  text = profile.location.name,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.testTag(MapScreenTestTags.PROFILE_LOCATION))

              if (profile.levelOfEducation.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.levelOfEducation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              if (profile.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profile.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2)
              }
            }
      }
}
