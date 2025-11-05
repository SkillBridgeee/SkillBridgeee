package com.android.sample.ui.map

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var profileRepository: ProfileRepository
  private lateinit var viewModel: MapViewModel

  private val testProfile1 =
      Profile(
          userId = "user1",
          name = "John Doe",
          email = "john@test.com",
          location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "Lausanne"),
          levelOfEducation = "CS, 3rd year",
          description = "Test user 1")

  private val testProfile2 =
      Profile(
          userId = "user2",
          name = "Jane Smith",
          email = "jane@test.com",
          location = Location(latitude = 46.2043907, longitude = 6.1431577, name = "Geneva"),
          levelOfEducation = "Math, 2nd year",
          description = "Test user 2")

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    profileRepository = mockk()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `initial state has default values`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()

    // When
    viewModel = MapViewModel(profileRepository)
    val state = viewModel.uiState.first()

    // Then
    assertEquals(LatLng(46.5196535, 6.6322734), state.userLocation)
    assertTrue(state.profiles.isEmpty())
    assertNull(state.selectedProfile)
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
  }

  @Test
  fun `loadProfiles fetches all profiles from repository`() = runTest {
    // Given
    val profiles = listOf(testProfile1, testProfile2)
    coEvery { profileRepository.getAllProfiles() } returns profiles

    // When
    viewModel = MapViewModel(profileRepository)
    val state = viewModel.uiState.first()

    // Then
    coVerify { profileRepository.getAllProfiles() }
    assertEquals(2, state.profiles.size)
    assertEquals(testProfile1, state.profiles[0])
    assertEquals(testProfile2, state.profiles[1])
  }

  @Test
  fun `loadProfiles sets loading state correctly`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } coAnswers
        {
          // Simulate delay
          emptyList()
        }

    // When
    viewModel = MapViewModel(profileRepository)

    // Then - final state should have isLoading = false
    val finalState = viewModel.uiState.first()
    assertFalse(finalState.isLoading)
  }

  @Test
  fun `loadProfiles handles empty list`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()

    // When
    viewModel = MapViewModel(profileRepository)
    val state = viewModel.uiState.first()

    // Then
    assertTrue(state.profiles.isEmpty())
    assertNull(state.errorMessage)
    assertFalse(state.isLoading)
  }

  @Test
  fun `loadProfiles handles repository error`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } throws Exception("Network error")

    // When
    viewModel = MapViewModel(profileRepository)
    val state = viewModel.uiState.first()

    // Then
    assertTrue(state.profiles.isEmpty())
    assertNotNull(state.errorMessage)
    assertEquals("Failed to load user locations", state.errorMessage)
    assertFalse(state.isLoading)
  }

  @Test
  fun `selectProfile updates selected profile in state`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()
    viewModel = MapViewModel(profileRepository)

    // When
    viewModel.selectProfile(testProfile1)
    val state = viewModel.uiState.first()

    // Then
    assertEquals(testProfile1, state.selectedProfile)
  }

  @Test
  fun `selectProfile with null clears selected profile`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()
    viewModel = MapViewModel(profileRepository)
    viewModel.selectProfile(testProfile1)

    // When
    viewModel.selectProfile(null)
    val state = viewModel.uiState.first()

    // Then
    assertNull(state.selectedProfile)
  }

  @Test
  fun `moveToLocation updates camera position`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()
    viewModel = MapViewModel(profileRepository)
    val newLocation = Location(latitude = 47.3769, longitude = 8.5417, name = "Zurich")

    // When
    viewModel.moveToLocation(newLocation)
    val state = viewModel.uiState.first()

    // Then
    assertEquals(LatLng(47.3769, 8.5417), state.userLocation)
  }

  @Test
  fun `loadProfiles can be called manually after initialization`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()
    viewModel = MapViewModel(profileRepository)

    // Change mock to return different data
    coEvery { profileRepository.getAllProfiles() } returns listOf(testProfile1)

    // When
    viewModel.loadProfiles()
    val state = viewModel.uiState.first()

    // Then
    assertEquals(1, state.profiles.size)
    assertEquals(testProfile1, state.profiles[0])
    coVerify(exactly = 2) { profileRepository.getAllProfiles() }
  }

  @Test
  fun `multiple profile selections update state correctly`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()
    viewModel = MapViewModel(profileRepository)

    // When
    viewModel.selectProfile(testProfile1)
    var state = viewModel.uiState.first()
    assertEquals(testProfile1, state.selectedProfile)

    viewModel.selectProfile(testProfile2)
    state = viewModel.uiState.first()

    // Then
    assertEquals(testProfile2, state.selectedProfile)
  }

  @Test
  fun `error message is cleared on successful reload`() = runTest {
    // Given - first call fails
    coEvery { profileRepository.getAllProfiles() } throws Exception("Error")
    viewModel = MapViewModel(profileRepository)
    var state = viewModel.uiState.first()
    assertNotNull(state.errorMessage)

    // When - second call succeeds
    coEvery { profileRepository.getAllProfiles() } returns listOf(testProfile1)
    viewModel.loadProfiles()
    state = viewModel.uiState.first()

    // Then
    assertNull(state.errorMessage)
    assertEquals(1, state.profiles.size)
  }
}
