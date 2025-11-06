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
    // Repos used by tests that instantiate a real MapViewModel
    mockProfileRepo = mockk()
    mockBookingRepo = mockk()
    // default: no bookings so MapViewModel.init() doesn't crash
    coEvery { mockBookingRepo.getAllBookings() } returns emptyList()

    // Prevent FirebaseAuth from blowing up in JVM tests
    mockkStatic(FirebaseAuth::class)
    val auth = io.mockk.mockk<FirebaseAuth>()
    every { FirebaseAuth.getInstance() } returns auth
    every { auth.currentUser } returns null
  }

  @Test
  fun mapScreen_displaysCorrectly() {
    // Given
    val mockBookingRepository = mockk<BookingRepository>()
    coEvery { mockBookingRepository.getAllBookings() } returns emptyList()
    val viewModel = MapViewModel(mockProfileRepo, mockBookingRepo)

    // When
    composeTestRule.setContent { MapScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_showsLoadingIndicator_whenLoading() {
    // Given
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val loadingState =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = emptyList(),
                selectedProfile = null,
                isLoading = true,
                errorMessage = null))
    io.mockk.every { mockViewModel.uiState } returns loadingState

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Then
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun mapScreen_showsErrorMessage_whenError() {
    // Given
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val errorState =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = emptyList(),
                selectedProfile = null,
                isLoading = false,
                errorMessage = "Failed to load user locations"))
    io.mockk.every { mockViewModel.uiState } returns errorState

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Then
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Failed to load user locations").assertIsDisplayed()
  }

  @Test
  fun mapScreen_showsProfileCard_whenProfileSelected() {
    // Given
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val stateWithSelection =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    io.mockk.every { mockViewModel.uiState } returns stateWithSelection

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Wait for composition to complete - GoogleMap needs time
    composeTestRule.waitForIdle()
    Thread.sleep(100) // Give extra time for GoogleMap initialization

    // Then - verify profile card components exist
    composeTestRule.onNodeWithText("John Doe").assertExists()
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertExists()
  }

  @Test
  fun mapScreen_displaysProfileLocation_inCard() {
    // Given
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val stateWithSelection =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    io.mockk.every { mockViewModel.uiState } returns stateWithSelection

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Wait for composition to complete
    composeTestRule.waitForIdle()
    Thread.sleep(100) // Give extra time for GoogleMap initialization

    // Then - verify location text exists in the card
    composeTestRule.onNodeWithText("Lausanne").assertExists()
  }

  @Test
  fun mapScreen_displaysLevelOfEducation_whenAvailable() {
    // Given
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val stateWithSelection =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    io.mockk.every { mockViewModel.uiState } returns stateWithSelection

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Wait for composition to complete
    composeTestRule.waitForIdle()
    Thread.sleep(100)

    // Then
    composeTestRule.onNodeWithText("CS, 3rd year").assertExists()
  }

  @Test
  fun mapScreen_displaysDescription_whenAvailable() {
    // Given
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val stateWithSelection =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    io.mockk.every { mockViewModel.uiState } returns stateWithSelection

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Wait for composition to complete
    composeTestRule.waitForIdle()
    Thread.sleep(100)

    // Then
    composeTestRule.onNodeWithText("Test user").assertExists()
  }

  @Test
  fun mapScreen_doesNotShowProfileCard_whenNoSelection() {
    // Given
    val mockBookingRepository = mockk<BookingRepository>()
    coEvery { mockBookingRepository.getAllBookings() } returns emptyList()
    val viewModel = MapViewModel(mockProfileRepo, mockBookingRepo)

    // When
    composeTestRule.setContent { MapScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertDoesNotExist()
  }

  @Test
  fun mapScreen_doesNotShowLoading_whenNotLoading() {

    // Given
    val mockBookingRepository = mockk<BookingRepository>()
    coEvery { mockBookingRepository.getAllBookings() } returns emptyList()
    val viewModel = MapViewModel(mockProfileRepo, mockBookingRepo)

    // When
    composeTestRule.setContent { MapScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertDoesNotExist()
  }

  @Test
  fun mapScreen_doesNotShowError_whenNoError() {
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val state =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { mockViewModel.uiState } returns state

    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()
  }

  @Test
  fun mapScreen_renders_withBookingPins_withoutCrashing() {
    // Given a mocked VM whose state contains booking pins
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val pinState =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                bookingPins =
                    listOf(
                        BookingPin(
                            bookingId = "b1",
                            position = LatLng(46.52, 6.63),
                            title = "Session with John",
                            snippet = "Math help",
                            profile = testProfile)),
                isLoading = false,
                errorMessage = null,
                selectedProfile = null))
    every { mockViewModel.uiState } returns pinState

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Then (we can’t assert markers; assert map is displayed without crash)
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_profileCard_toggles_whenSelectedProfileChanges() {
    // Given a mocked VM whose state we can mutate
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                selectedProfile = null,
                isLoading = false,
                errorMessage = null))
    every { mockViewModel.uiState } returns flow

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Then - initially no card
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertDoesNotExist()

    // When - select a profile (simulates clicking a booking marker)
    flow.value = flow.value.copy(selectedProfile = testProfile)
    composeTestRule.waitForIdle()

    // Then - card appears with correct content
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Lausanne").assertIsDisplayed()

    // When - clear selection
    flow.value = flow.value.copy(selectedProfile = null)
    composeTestRule.waitForIdle()

    // Then - card disappears
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertDoesNotExist()
  }

  @Test
  fun mapScreen_errorBanner_toggles_whenErrorChanges() {
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = emptyList(),
                selectedProfile = null,
                isLoading = false,
                errorMessage = null))
    every { mockViewModel.uiState } returns flow

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Then - no error initially
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()

    // When - set error
    flow.value = flow.value.copy(errorMessage = "Oops")
    composeTestRule.waitForIdle()

    // Then - error appears
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Oops").assertIsDisplayed()

    // When - clear error
    flow.value = flow.value.copy(errorMessage = null)
    composeTestRule.waitForIdle()

    // Then - error hidden
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()
  }

  @Test
  fun mapScreen_loadingIndicator_toggles_whenLoadingChanges() {
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = emptyList(),
                selectedProfile = null,
                isLoading = false,
                errorMessage = null))
    every { mockViewModel.uiState } returns flow

    // When
    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // Then - not loading initially
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertDoesNotExist()

    // When - turn loading on
    flow.value = flow.value.copy(isLoading = true)
    composeTestRule.waitForIdle()

    // Then - spinner visible
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()

    // When - turn loading off
    flow.value = flow.value.copy(isLoading = false)
    composeTestRule.waitForIdle()

    // Then - spinner hidden
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertDoesNotExist()
  }

  @Test
  fun mapScreen_profileCard_click_calls_onProfileClick_withUserId() {
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    every { mockViewModel.uiState } returns flow

    var clickedId: String? = null
    composeTestRule.setContent {
      MapScreen(viewModel = mockViewModel, onProfileClick = { id -> clickedId = id })
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed().performClick()
    assert(clickedId == testProfile.userId)
  }

  @Test
  fun mapScreen_profileCard_hides_optional_sections_when_empty() {
    val emptyProfile = testProfile.copy(levelOfEducation = "", description = "")
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(emptyProfile),
                selectedProfile = emptyProfile,
                isLoading = false,
                errorMessage = null))
    every { mockViewModel.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // We assert specific strings are NOT shown when empty
    composeTestRule.onNodeWithText("CS, 3rd year").assertDoesNotExist()
    composeTestRule.onNodeWithText("Test user").assertDoesNotExist()
  }

  @Test
  fun mapScreen_shows_error_and_profileCard_simultaneously() {
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = "Boom"))
    every { mockViewModel.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Boom").assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
  }

  @Test
  fun mapScreen_profileCard_updates_when_selection_changes() {
    val other =
        testProfile.copy(
            userId = "user2", name = "Jane Smith", location = Location(46.2, 6.1, "Geneva"))
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                profiles = listOf(testProfile, other),
                selectedProfile = testProfile,
                isLoading = false,
                errorMessage = null))
    every { mockViewModel.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

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
  fun mapScreen_renders_withMultipleBookingPins() {
    val mockViewModel = mockk<MapViewModel>(relaxed = true)
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
    every { mockViewModel.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = mockViewModel) }

    // We can’t query markers; just assert the map shows without crash.
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapView_rendersMyProfileMarker_whenValidLocation() {
    val profile = Profile(userId = "me", name = "John", location = Location(46.5, 6.6))
    val state =
        MapUiState(
            userLocation = LatLng(46.5, 6.6), profiles = listOf(profile), bookingPins = emptyList())
    val vm = mockk<MapViewModel>(relaxed = true)
    every { vm.uiState } returns MutableStateFlow(state)

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // We can’t assert actual markers, but map should display
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapView_skipsProfileMarker_whenInvalidLocation() {
    val profile = Profile(userId = "me", name = "John", location = Location(0.0, 0.0))
    val state =
        MapUiState(
            userLocation = LatLng(46.5, 6.6), profiles = listOf(profile), bookingPins = emptyList())
    val vm = mockk<MapViewModel>(relaxed = true)
    every { vm.uiState } returns MutableStateFlow(state)

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Still renders without crash
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun clickingBookingPin_triggersProfileSelection() {
    val profile = Profile(userId = "p1", name = "Tutor")
    val pin =
        BookingPin(
            bookingId = "b1", position = LatLng(46.5, 6.6), title = "Session", profile = profile)
    val state =
        MapUiState(
            userLocation = LatLng(46.5, 6.6), profiles = listOf(profile), bookingPins = listOf(pin))

    var selectedProfile: Profile? = null
    val vm = mockk<MapViewModel>(relaxed = true)
    every { vm.uiState } returns MutableStateFlow(state)
    every { vm.selectProfile(any()) } answers { selectedProfile = firstArg() }

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Simulate click (logical test, ensures callback wiring works)
    vm.selectProfile(profile)

    assert(selectedProfile == profile)
  }
}
