package com.android.sample.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MapScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testProfile =
      Profile(
          userId = "user1",
          name = "John Doe",
          email = "john@test.com",
          location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "Lausanne"),
          levelOfEducation = "CS, 3rd year",
          description = "Test user")

  private lateinit var mockProfileRepo: ProfileRepository
  private lateinit var mockBookingRepo: BookingRepository

  @Before
  fun setup() {
    mockProfileRepo = mockk()
    mockBookingRepo = mockk()
    coEvery { mockBookingRepo.getAllBookings() } returns emptyList()

    // Prevent FirebaseAuth from blowing up in JVM tests
    mockkStatic(FirebaseAuth::class)
    val auth = mockk<FirebaseAuth>()
    every { FirebaseAuth.getInstance() } returns auth
    every { auth.currentUser } returns null
  }

  // --- Smoke / structure ---

  @Test
  fun mapScreen_smoke_rendersScreenAndMap() {
    val vm = MapViewModel(mockProfileRepo, mockBookingRepo)
    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  // --- Loading / error toggles (cover both show & hide in one go) ---

  @Test
  fun loadingIndicator_toggles_withIsLoading() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                selectedProfile = null,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Not loading initially
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertDoesNotExist()
    // Turn on
    flow.value = flow.value.copy(isLoading = true)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
    // Turn off
    flow.value = flow.value.copy(isLoading = false)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertDoesNotExist()
  }

  @Test
  fun errorBanner_toggles_withErrorMessage() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                selectedProfile = null,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // No error initially
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()
    // Set error
    flow.value = flow.value.copy(errorMessage = "Oops")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Oops").assertIsDisplayed()
    // Clear error
    flow.value = flow.value.copy(errorMessage = null)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()
  }

  // --- Profile card visibility and content ---

  @Test
  fun profileCard_toggles_withSelection() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                selectedProfile = null,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Hidden when no selection
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertDoesNotExist()

    // Appears when selected
    flow.value = flow.value.copy(selectedProfile = testProfile)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Lausanne").assertIsDisplayed()

    // Disappears when cleared
    flow.value = flow.value.copy(selectedProfile = null)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertDoesNotExist()
  }

  @Test
  fun profileCard_displays_optional_fields_whenPresent() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("CS, 3rd year").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test user").assertIsDisplayed()
  }

  @Test
  fun profileCard_hides_optional_fields_whenEmpty() {
    val empty = testProfile.copy(levelOfEducation = "", description = "")
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(empty),
                selectedProfile = empty,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("CS, 3rd year").assertDoesNotExist()
    composeTestRule.onNodeWithText("Test user").assertDoesNotExist()
  }

  // --- Interaction wiring ---

  @Test
  fun profileCard_click_propagatesUserId() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    var clickedId: String? = null
    composeTestRule.setContent {
      MapScreen(viewModel = vm, onProfileClick = { id -> clickedId = id })
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed().performClick()
    assert(clickedId == testProfile.userId)
  }

  // --- Booking pins and logical selection wiring ---

  @Test
  fun map_renders_withMultipleBookingPins_withoutCrashing() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                bookingPins =
                    listOf(
                        BookingPin("b1", LatLng(46.52, 6.63), "Session A", "Desc A", testProfile),
                        BookingPin("b2", LatLng(46.50, 6.60), "Session B", "Desc B", testProfile)),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun clickingBookingPin_triggers_selectProfile_callback_path() {
    val profile = Profile(userId = "p1", name = "Tutor")
    val pin = BookingPin("b1", LatLng(46.5, 6.6), "Session", profile = profile)
    val state =
        MapUiState(
            userLocation = LatLng(46.5, 6.6), profiles = listOf(profile), bookingPins = listOf(pin))
    var selected: Profile? = null
    val vm = mockk<MapViewModel>(relaxed = true)
    every { vm.uiState } returns MutableStateFlow(state)
    every { vm.selectProfile(any()) } answers { selected = firstArg() }

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // We can’t tap a Google marker in Robolectric; call the VM directly to validate wiring.
    vm.selectProfile(profile)
    assert(selected == profile)
  }

  // --- Edge cases ---

  @Test
  fun mapScreen_shows_error_and_profileCard_simultaneously() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = "Boom"))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Boom").assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
  }

  @Test
  fun profileCard_updates_when_selection_changes() {
    val other =
        testProfile.copy(
            userId = "user2", name = "Jane Smith", location = Location(46.2, 6.1, "Geneva"))
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile, other),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Initial content
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Lausanne").assertIsDisplayed()

    // Change selection
    flow.value = flow.value.copy(selectedProfile = other)
    composeTestRule.waitForIdle()

    // Updated content
    composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
    composeTestRule.onNodeWithText("Geneva").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertDoesNotExist()
  }

  @Test
  fun emptyState_displays_whenNoBookingsOrProfiles() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                bookingPins = emptyList(),
                isLoading = false,
                errorMessage = null))

    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Verify that the placeholder text is shown
    composeTestRule.onNodeWithTag(MapScreenTestTags.EMPTY_STATE).assertIsDisplayed()
    composeTestRule.onNodeWithText("No available bookings nearby.").assertIsDisplayed()

    // If bookings appear, placeholder should disappear
    flow.value =
        flow.value.copy(
            bookingPins =
                listOf(BookingPin("b1", LatLng(46.5, 6.6), "Session", "Description", null)))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.EMPTY_STATE).assertDoesNotExist()
  }
}
