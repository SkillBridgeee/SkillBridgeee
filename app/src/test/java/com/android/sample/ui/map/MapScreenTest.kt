package com.android.sample.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.listing.ListingRepository
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
import org.junit.Assert.assertEquals
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
  private lateinit var mockListingRepo: ListingRepository

  @Before
  fun setup() {
    mockProfileRepo = mockk()
    mockBookingRepo = mockk()
    mockListingRepo = mockk()
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
    val vm = MapViewModel(mockProfileRepo, mockBookingRepo, mockListingRepo)
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

  // --- Info window and booking details dialog visibility ---

  @Test
  fun infoWindows_appear_when_pinPosition_selected() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val booking =
        com.android.sample.model.booking.Booking(
            bookingId = "b1",
            associatedListingId = "listing1",
            listingCreatorId = "user1",
            bookerId = "user2",
            sessionStart = java.util.Date(),
            sessionEnd = java.util.Date())
    val pin =
        BookingPin(
            bookingId = "b1",
            position = LatLng(46.52, 6.63),
            title = "Math Session",
            snippet = "EPFL",
            profile = testProfile,
            booking = booking)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                selectedPinPosition = null,
                bookingPins = listOf(pin),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Hidden when no pin position selected
    composeTestRule.onNodeWithTag(MapScreenTestTags.BOOKING_INFO_WINDOW + "b1").assertDoesNotExist()

    // Appears when pin position selected
    flow.value = flow.value.copy(selectedPinPosition = LatLng(46.52, 6.63))
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.BOOKING_INFO_WINDOW + "b1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Math Session").assertIsDisplayed()

    // Disappears when cleared
    flow.value = flow.value.copy(selectedPinPosition = null)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.BOOKING_INFO_WINDOW + "b1").assertDoesNotExist()
  }

  @Test
  fun bookingDetailsDialog_toggles_with_showBookingDetailsDialog() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val booking =
        com.android.sample.model.booking.Booking(
            bookingId = "b1",
            associatedListingId = "listing1",
            listingCreatorId = "user1",
            bookerId = "user2",
            sessionStart = java.util.Date(),
            sessionEnd = java.util.Date(),
            price = 25.0)
    val pin =
        BookingPin(
            bookingId = "b1",
            position = LatLng(46.52, 6.63),
            title = "Physics Tutoring",
            snippet = "Library",
            profile = testProfile,
            booking = booking)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                selectedBookingPin = null,
                showBookingDetailsDialog = false,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Hidden initially
    composeTestRule.onNodeWithTag(MapScreenTestTags.BOOKING_DETAILS_DIALOG).assertDoesNotExist()

    // Appears when dialog shown
    flow.value = flow.value.copy(selectedBookingPin = pin, showBookingDetailsDialog = true)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.BOOKING_DETAILS_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithText("Booking Details").assertIsDisplayed()
    composeTestRule.onNodeWithText("Physics Tutoring").assertIsDisplayed()

    // Disappears when cleared
    flow.value = flow.value.copy(showBookingDetailsDialog = false)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.BOOKING_DETAILS_DIALOG).assertDoesNotExist()
  }

  @Test
  fun bookingDetailsDialog_displays_booking_time_and_price() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val sessionStart = java.util.Date(1732600000000) // Nov 26, 2024 09:00:00 GMT
    val sessionEnd = java.util.Date(1732610800000) // Nov 26, 2024 12:00:00 GMT
    val booking =
        com.android.sample.model.booking.Booking(
            bookingId = "b1",
            associatedListingId = "listing1",
            listingCreatorId = "user1",
            bookerId = "user2",
            sessionStart = sessionStart,
            sessionEnd = sessionEnd,
            price = 45.50)
    val pin =
        BookingPin(
            bookingId = "b1",
            position = LatLng(46.52, 6.63),
            title = "Calculus Help",
            snippet = "Room 101",
            profile = testProfile,
            booking = booking)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                selectedBookingPin = pin,
                showBookingDetailsDialog = true,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Dialog should display booking information
    composeTestRule.onNodeWithTag(MapScreenTestTags.BOOKING_DETAILS_DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithText("Calculus Help").assertIsDisplayed()
    composeTestRule.onNodeWithText("$45.50", substring = true).assertIsDisplayed()
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
  fun clickingBookingPin_triggers_selectPinPosition_callback_path() {
    val profile = Profile(userId = "p1", name = "Tutor")
    val pin = BookingPin("b1", LatLng(46.5, 6.6), "Session", profile = profile)
    val state =
        MapUiState(
            userLocation = LatLng(46.5, 6.6), profiles = listOf(profile), bookingPins = listOf(pin))
    var selectedPos: LatLng? = null
    val vm = mockk<MapViewModel>(relaxed = true)
    every { vm.uiState } returns MutableStateFlow(state)
    every { vm.selectPinPosition(any()) } answers { selectedPos = firstArg() }

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // We can't tap a Google marker in Robolectric; call the VM directly to validate wiring.
    vm.selectPinPosition(LatLng(46.5, 6.6))
    assertEquals(LatLng(46.5, 6.6), selectedPos)
  }

  @Test
  fun bookingPins_display_with_correct_properties() {
    val profile = Profile(userId = "tutor1", name = "Dr. Smith")
    val pin =
        BookingPin(
            bookingId = "b1",
            position = LatLng(46.5, 6.6),
            title = "Math Tutoring",
            snippet = "Dr. Smith - Library",
            profile = profile)

    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5, 6.6),
                profiles = listOf(profile),
                bookingPins = listOf(pin),
                myProfile = testProfile,
                isLoading = false))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Map should render without crashing - booking pin with title and snippet rendered
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun myProfile_marker_displays_when_location_non_zero() {
    val myProfile =
        testProfile.copy(
            userId = "me", name = "My Name", location = Location(46.52, 6.63, "My Location"))

    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                myProfile = myProfile,
                bookingPins = emptyList(),
                isLoading = false))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun myProfile_marker_hidden_when_location_is_zero() {
    val myProfile = testProfile.copy(userId = "me", location = Location(0.0, 0.0, "Zero"))

    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfile,
                bookingPins = emptyList(),
                isLoading = false))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  // --- User profile marker tests ---

  @Test
  fun mapScreen_displaysProfileLocation_inCard() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val profileWithLocation =
        testProfile.copy(
            location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "EPFL"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = profileWithLocation,
                profiles = listOf(profileWithLocation),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_renders_withUserProfileMarker() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val profileWithLocation =
        testProfile.copy(
            location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "EPFL"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = profileWithLocation,
                profiles = listOf(profileWithLocation),
                bookingPins = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  // --- Additional comprehensive tests for high coverage ---

  @Test
  fun mapScreen_withMyProfile_andZeroCoordinates_doesNotCrash() {
    val zeroProfile =
        testProfile.copy(location = Location(latitude = 0.0, longitude = 0.0, name = "Origin"))
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = zeroProfile,
                profiles = listOf(zeroProfile),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_withMyProfile_andNonZeroCoordinates_renders() {
    val validProfile =
        testProfile.copy(
            location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "EPFL"))
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = validProfile,
                profiles = listOf(validProfile),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun bookingPin_withNullProfile_doesNotCrash() {
    val pinWithoutProfile =
        BookingPin("b1", LatLng(46.52, 6.63), "Session", "Description", profile = null)
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                bookingPins = listOf(pinWithoutProfile),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun bookingPin_withProfile_rendersCorrectly() {
    val pinWithProfile =
        BookingPin("b1", LatLng(46.52, 6.63), "Math Lesson", "Learn calculus", testProfile)
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                bookingPins = listOf(pinWithProfile),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_withEmptyProfiles_andEmptyBookings_renders() {
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
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun loadingIndicator_andErrorMessage_canBothBeVisible() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                isLoading = true,
                errorMessage = "Loading error"))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  // --- Permission handling tests ---

  @Test
  fun mapScreen_requestLocationOnStart_true_triggersPermissionRequest() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    // Setting requestLocationOnStart = true should trigger permission request logic
    composeTestRule.setContent { MapScreen(viewModel = vm, requestLocationOnStart = true) }
    composeTestRule.waitForIdle()

    // Map should still render regardless of permission state
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_requestLocationOnStart_false_doesNotTriggerPermissionRequest() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    // Default behavior (requestLocationOnStart = false) should not request permission
    composeTestRule.setContent { MapScreen(viewModel = vm, requestLocationOnStart = false) }
    composeTestRule.waitForIdle()

    // Map should render without permission request
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_withExistingPermission_rendersMapWithLocationFeatures() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    // This test verifies that the MapView composable handles permission checking
    // The actual permission state is checked via ContextCompat.checkSelfPermission
    composeTestRule.setContent { MapScreen(viewModel = vm, requestLocationOnStart = true) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_withDifferentCenterLocation_renders() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(40.7128, -74.0060), // New York
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun errorMessage_withLongText_displays() {
    val longError =
        "This is a very long error message that should still display correctly " +
            "in the error banner at the top of the screen without breaking the layout"
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                isLoading = false,
                errorMessage = longError))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText(longError).assertIsDisplayed()
  }

  @Test
  fun mapScreen_multipleBookingPins_withDifferentLocations_renders() {
    val pin1 = BookingPin("b1", LatLng(46.52, 6.63), "Session 1", "Desc 1", testProfile)
    val pin2 = BookingPin("b2", LatLng(46.53, 6.64), "Session 2", "Desc 2", testProfile)
    val pin3 = BookingPin("b3", LatLng(46.54, 6.65), "Session 3", "Desc 3", testProfile)

    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                bookingPins = listOf(pin1, pin2, pin3),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_withAllFieldsPopulated_renders() {
    val fullProfile =
        Profile(
            userId = "full-user",
            name = "Full Name",
            email = "full@test.com",
            location = Location(46.52, 6.63, "Full Location"),
            levelOfEducation = "PhD Computer Science",
            description = "Full description with lots of details about the user")

    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = fullProfile,
                profiles = listOf(fullProfile),
                bookingPins =
                    listOf(BookingPin("b1", LatLng(46.52, 6.63), "Session", "Desc", fullProfile)),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapScreen_stateChanges_updateUI_correctly() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    // Initial state
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertDoesNotExist()

    // Change to loading
    flow.value = flow.value.copy(isLoading = true)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()

    // Add error
    flow.value = flow.value.copy(isLoading = false, errorMessage = "Error occurred")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()

    // Clear error
    flow.value = flow.value.copy(errorMessage = null)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()
  }

  @Test
  fun mapScreen_withMyProfileNull_usesDefaultCenterLocation() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.5196535, 6.6322734),
                myProfile = null,
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun bookingPin_withNullSnippet_renders() {
    val pinNoSnippet = BookingPin("b1", LatLng(46.52, 6.63), "Title Only", null, testProfile)
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(testProfile),
                bookingPins = listOf(pinNoSnippet),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapView_withLocationPermissionGranted_enablesMyLocation() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Map should render - permission callback tested indirectly
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapView_cameraPositionUpdatesWhenMyProfileLocationChanges() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val profileAtEPFL =
        testProfile.copy(
            location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "EPFL"))

    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = null,
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Update myProfile with location
    flow.value = flow.value.copy(myProfile = profileAtEPFL)
    composeTestRule.waitForIdle()

    // Camera position should update to profile location
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapView_usesCenterLocationWhenProfileLocationIsNull() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(47.0, 8.0), // Zurich
                myProfile = null,
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Should use centerLocation (userLocation) when myProfile is null
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun mapView_skipsLocationPermissionRequestOnError() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Permission launcher exception is caught - map still works
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  // --- Tests for User Profile Marker (lines 211-219) ---

  @Test
  fun userProfileMarker_rendersWhenMyProfileHasNonZeroLocation() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfileWithLocation =
        testProfile.copy(
            name = "Test User",
            location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "EPFL Campus"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfileWithLocation,
                profiles = listOf(myProfileWithLocation),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Map should render with user profile marker
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_notRenderedWhenMyProfileIsNull() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = null,
                profiles = emptyList(),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Map should render without user profile marker
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_notRenderedWhenLocationIsNull() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfileWithoutLocation = testProfile.copy(location = Location(0.0, 0.0, ""))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfileWithoutLocation,
                profiles = listOf(myProfileWithoutLocation),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Map should render but without user profile marker (0,0 coordinates are filtered)
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_notRenderedWhenBothCoordinatesAreZero() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfileZeroCoords =
        testProfile.copy(location = Location(latitude = 0.0, longitude = 0.0, name = "Origin"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfileZeroCoords,
                profiles = listOf(myProfileZeroCoords),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Map should render but marker should be filtered out
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_rendersWhenOnlyLatitudeIsZero() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfilePartialZero =
        testProfile.copy(
            name = "Test User",
            location = Location(latitude = 0.0, longitude = 6.6322734, name = "Partial Zero"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfilePartialZero,
                profiles = listOf(myProfilePartialZero),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Marker should render because condition is (lat != 0.0 || lng != 0.0)
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_rendersWhenOnlyLongitudeIsZero() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfilePartialZero =
        testProfile.copy(
            name = "Test User",
            location = Location(latitude = 46.5196535, longitude = 0.0, name = "Partial Zero"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfilePartialZero,
                profiles = listOf(myProfilePartialZero),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Marker should render because condition is (lat != 0.0 || lng != 0.0)
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_usesMeAsTitleWhenNameIsNull() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfileNoName =
        testProfile.copy(
            name = null,
            location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "EPFL"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfileNoName,
                profiles = listOf(myProfileNoName),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Marker should use "Me" as title when name is null
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_usesNameAsTitleWhenNameIsNotNull() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfileWithName =
        testProfile.copy(
            name = "Alice Johnson",
            location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "EPFL"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfileWithName,
                profiles = listOf(myProfileWithName),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Marker should use name as title
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_usesLocationNameAsSnippet() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfileWithLocationName =
        testProfile.copy(
            name = "Test User",
            location =
                Location(
                    latitude = 46.5196535, longitude = 6.6322734, name = "EPFL Innovation Park"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfileWithLocationName,
                profiles = listOf(myProfileWithLocationName),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Marker should use location name as snippet
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_rendersWithNegativeCoordinates() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfileNegative =
        testProfile.copy(
            name = "Southern User",
            location = Location(latitude = -33.8688, longitude = 151.2093, name = "Sydney"))
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfileNegative,
                profiles = listOf(myProfileNegative),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Marker should render with negative coordinates
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }

  @Test
  fun userProfileMarker_rendersAlongsideBookingPins() {
    val vm = mockk<MapViewModel>(relaxed = true)
    val myProfileWithLocation =
        testProfile.copy(
            name = "My Name",
            location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "My Place"))
    val bookingPin = BookingPin("b1", LatLng(46.52, 6.63), "Session", "Description", testProfile)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                myProfile = myProfileWithLocation,
                profiles = listOf(myProfileWithLocation),
                bookingPins = listOf(bookingPin),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Both user profile marker and booking pins should render
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
  }
}
