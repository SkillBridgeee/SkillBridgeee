package com.android.sample.ui.map

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
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

  @Test
  fun mapScreen_displaysCorrectly() {
    // Given
    val mockRepository = mockk<ProfileRepository>()
    coEvery { mockRepository.getAllProfiles() } returns emptyList()
    val viewModel = MapViewModel(mockRepository)

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
    val mockRepository = mockk<ProfileRepository>()
    coEvery { mockRepository.getAllProfiles() } returns listOf(testProfile)
    val viewModel = MapViewModel(mockRepository)

    // When
    composeTestRule.setContent { MapScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(MapScreenTestTags.PROFILE_CARD).assertDoesNotExist()
  }

  @Test
  fun mapScreen_doesNotShowLoading_whenNotLoading() {
    // Given
    val mockRepository = mockk<ProfileRepository>()
    coEvery { mockRepository.getAllProfiles() } returns emptyList()
    val viewModel = MapViewModel(mockRepository)

    // When
    composeTestRule.setContent { MapScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(MapScreenTestTags.LOADING_INDICATOR).assertDoesNotExist()
  }

  @Test
  fun mapScreen_doesNotShowError_whenNoError() {
    // Given
    val mockRepository = mockk<ProfileRepository>()
    coEvery { mockRepository.getAllProfiles() } returns emptyList()
    val viewModel = MapViewModel(mockRepository)

    // When
    composeTestRule.setContent { MapScreen(viewModel = viewModel) }

    // Then
    composeTestRule.onNodeWithTag(MapScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()
  }
}
