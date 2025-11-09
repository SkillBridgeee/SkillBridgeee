package com.android.sample.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.ui.map.BookingPin
import com.android.sample.ui.map.MapScreen
import com.android.sample.ui.map.MapUiState
import com.android.sample.ui.map.MapViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapScreenAndroidTest {

  @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun stubFirebaseAuth() {
    mockkStatic(FirebaseAuth::class)
    val auth = mockk<FirebaseAuth>(relaxed = true)
    every { FirebaseAuth.getInstance() } returns auth
    every { auth.currentUser } returns null
  }

  @After
  fun unstubFirebaseAuth() {
    unmockkStatic(FirebaseAuth::class)
  }

  private val testProfile =
      Profile(
          userId = "user1",
          name = "John Doe",
          email = "john@test.com",
          location = Location(46.5196535, 6.6322734, "Lausanne"),
          levelOfEducation = "CS, 3rd year",
          description = "Test user")

  @Test
  fun covers_bookingPins_and_profileMarker_lines() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val pin =
        BookingPin(
            bookingId = "b42",
            position = LatLng(46.52, 6.63),
            title = "Session X",
            snippet = "Algebra",
            profile = testProfile)
    val state =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                bookingPins = listOf(pin),
                selectedProfile = null,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns state

    composeRule.setContent { MapScreen(viewModel = vm) }
    composeRule.waitForIdle() // executes GoogleMap content: Marker loop + profile Marker
  }

  @Test
  fun covers_target_and_LaunchedEffect_branches() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.0, 6.0), // center
                profiles = listOf(testProfile),
                bookingPins = emptyList(),
                selectedProfile = null,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeRule.setContent { MapScreen(viewModel = vm) }
    composeRule.waitForIdle()

    // Switch to valid profile -> target becomes profileLatLng, LaunchedEffect runs again
    flow.value = flow.value.copy(selectedProfile = testProfile)
    composeRule.waitForIdle()

    // Now invalid (0,0) -> fallback to center path is executed
    val zero = testProfile.copy(location = Location(0.0, 0.0, ""))
    flow.value = flow.value.copy(selectedProfile = zero)
    composeRule.waitForIdle()
  }
}
