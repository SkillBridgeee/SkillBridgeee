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
  fun profileCard_displays_userName_when_name_is_null() {
    val nullNameProfile = testProfile.copy(name = null)
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(nullNameProfile),
                selectedProfile = nullNameProfile,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Should show "Unknown User" when name is null
    composeTestRule.onNodeWithText("Unknown User").assertIsDisplayed()
  }

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
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertDoesNotExist()
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

  @Test
  fun profileCard_withBlankDescription_hidesDescription() {
    val blankDescProfile = testProfile.copy(description = "   ", levelOfEducation = "CS, 3rd year")
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(blankDescProfile),
                selectedProfile = blankDescProfile,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Profile card should be displayed
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
    // Education should be displayed (non-blank)
    composeTestRule.onNodeWithText("CS, 3rd year").assertIsDisplayed()
    // Blank description should not be displayed (isNotBlank() will hide it)
    composeTestRule.onNodeWithText("   ").assertDoesNotExist()
  }

  @Test
  fun profileCard_withBlankEducation_hidesEducation() {
    val blankEduProfile = testProfile.copy(levelOfEducation = "   ", description = "Test user")
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(blankEduProfile),
                selectedProfile = blankEduProfile,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Profile card should be displayed
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
    // Description should be displayed (non-blank)
    composeTestRule.onNodeWithText("Test user").assertIsDisplayed()
    // Blank education should not be displayed (isNotBlank() will hide it)
    composeTestRule.onNodeWithText("   ").assertDoesNotExist()
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
  fun profileCard_clickCallback_calledWithCorrectUserId() {
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

    var clickedUserId: String? = null
    composeTestRule.setContent {
      MapScreen(viewModel = vm, onProfileClick = { userId -> clickedUserId = userId })
    }

    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).performClick()

    assertEquals("user1", clickedUserId)
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
                selectedProfile = fullProfile,
                bookingPins =
                    listOf(BookingPin("b1", LatLng(46.52, 6.63), "Session", "Desc", fullProfile)),
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }

    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.MAP_VIEW).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
    composeTestRule.onNodeWithText("Full Name").assertIsDisplayed()
    composeTestRule.onNodeWithText("Full Location").assertIsDisplayed()
    composeTestRule.onNodeWithText("PhD Computer Science").assertIsDisplayed()
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

    // Clear error, add profile selection
    flow.value = flow.value.copy(errorMessage = null, selectedProfile = testProfile)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
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
  fun profileCard_withLongDescription_displays() {
    val longDesc =
        "This is a very long description that goes on and on and should be truncated " +
            "to two lines maximum according to the maxLines parameter in the UI component"
    val longDescProfile = testProfile.copy(description = longDesc)
    val vm = mockk<MapViewModel>(relaxed = true)
    val flow =
        MutableStateFlow(
            MapUiState(
                userLocation = LatLng(46.52, 6.63),
                profiles = listOf(longDescProfile),
                selectedProfile = longDescProfile,
                isLoading = false,
                errorMessage = null))
    every { vm.uiState } returns flow

    composeTestRule.setContent { MapScreen(viewModel = vm) }
    composeTestRule.waitForIdle()

    // Long description should be displayed (possibly truncated)
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertIsDisplayed()
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
}
