package com.android.sample.ui.map

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.android.sample.model.booking.Booking
import com.android.sample.model.booking.BookingRepository
import com.android.sample.model.booking.BookingStatus
import com.android.sample.model.map.Location
import com.android.sample.model.user.Profile
import com.android.sample.model.user.ProfileRepository
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

  @get:Rule val instantExecutorRule = InstantTaskExecutorRule()

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var profileRepository: ProfileRepository
  private lateinit var bookingRepository: BookingRepository
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
    bookingRepository = mockk()
    // Default for tests that don't care about bookings
    coEvery { bookingRepository.getAllBookings() } returns emptyList()
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
    viewModel = MapViewModel(profileRepository, bookingRepository)
    val state = viewModel.uiState.first()

    // Then
    assertEquals(LatLng(46.5196535, 6.6322734), state.userLocation)
    assertTrue(state.profiles.isEmpty())
    assertNull(state.selectedProfile)
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
    assertTrue(state.bookingPins.isEmpty())
  }

  @Test
  fun `loadProfiles fetches all profiles from repository`() = runTest {
    // Given
    val profiles = listOf(testProfile1, testProfile2)
    coEvery { profileRepository.getAllProfiles() } returns profiles

    // When
    viewModel = MapViewModel(profileRepository, bookingRepository)
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
    coEvery { profileRepository.getAllProfiles() } coAnswers { emptyList() }

    // When
    viewModel = MapViewModel(profileRepository, bookingRepository)

    // Then - final state should have isLoading = false
    val finalState = viewModel.uiState.first()
    assertFalse(finalState.isLoading)
  }

  @Test
  fun `loadProfiles handles empty list`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()

    // When
    viewModel = MapViewModel(profileRepository, bookingRepository)
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
    coEvery { bookingRepository.getAllBookings() } returns emptyList()

    // When
    viewModel = MapViewModel(profileRepository, bookingRepository)

    // Let init{ loadProfiles(); loadBookings() } finish
    advanceUntilIdle()

    // Then
    val state = viewModel.uiState.value
    assertTrue(state.profiles.isEmpty())
    assertNotNull(state.errorMessage)
    assertEquals("Failed to load user locations", state.errorMessage)
    assertFalse(state.isLoading)
  }

  @Test
  fun `selectProfile updates selected profile in state`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()
    viewModel = MapViewModel(profileRepository, bookingRepository)

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
    viewModel = MapViewModel(profileRepository, bookingRepository)
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
    viewModel = MapViewModel(profileRepository, bookingRepository)
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
    viewModel = MapViewModel(profileRepository, bookingRepository)

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
    viewModel = MapViewModel(profileRepository, bookingRepository)

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
    coEvery { bookingRepository.getAllBookings() } returns emptyList()

    viewModel = MapViewModel(profileRepository, bookingRepository)
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

  // ----------------------------
  // NEW TESTS FOR BOOKINGS/PINS
  // ----------------------------

  @Test
  fun `loadBookings builds bookingPins for valid tutor profile coords`() = runTest {
    // Given: no profiles needed here
    coEvery { profileRepository.getAllProfiles() } returns emptyList()

    val tutor =
        Profile(
            userId = "tutor1",
            name = "Tutor Valid",
            email = "t@host.com",
            location = Location(46.2043907, 6.1431577, "Geneva"),
            levelOfEducation = "",
            description = "Great tutor")

    val booking =
        Booking(
            bookingId = "b1",
            associatedListingId = "l1",
            bookerId = "student1",
            listingCreatorId = "tutor1",
            sessionStart = Date(),
            sessionEnd = Date(),
            status = BookingStatus.PENDING)

    coEvery { bookingRepository.getAllBookings() } returns listOf(booking)
    coEvery { profileRepository.getProfileById("tutor1") } returns tutor

    // When
    viewModel = MapViewModel(profileRepository, bookingRepository)
    val state = viewModel.uiState.first()

    // Then
    coVerify { bookingRepository.getAllBookings() }
    coVerify { profileRepository.getProfileById("tutor1") }
    assertEquals(1, state.bookingPins.size)
    val pin = state.bookingPins.first()
    assertEquals("b1", pin.bookingId)
    assertEquals(tutor.name, pin.title)
    assertEquals(LatLng(46.2043907, 6.1431577), pin.position)
    assertNotNull(pin.profile)
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
  }

  @Test
  fun `loadBookings includes bookingPins when tutor coords are zero but valid`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()

    val tutorZero =
        Profile(
            userId = "tutor2",
            name = "Tutor Zero",
            email = "z@host.com",
            location = Location(0.0, 0.0, "Unknown"),
            levelOfEducation = "",
            description = "")

    val booking =
        Booking(
            bookingId = "b2",
            associatedListingId = "l2",
            bookerId = "student1",
            listingCreatorId = "tutor2",
            sessionStart = Date(),
            sessionEnd = Date(),
            status = BookingStatus.PENDING)

    coEvery { bookingRepository.getAllBookings() } returns listOf(booking)
    coEvery { profileRepository.getProfileById("tutor2") } returns tutorZero

    // When
    viewModel = MapViewModel(profileRepository, bookingRepository)
    val state = viewModel.uiState.first()

    // Then
    assertEquals(1, state.bookingPins.size)
    val pin = state.bookingPins.first()
    assertEquals("b2", pin.bookingId)
    assertEquals(LatLng(0.0, 0.0), pin.position)
    assertEquals("Tutor Zero", pin.title)
    assertFalse(state.isLoading)
    assertNull(state.errorMessage)
  }

  @Test
  fun `loadBookings surfaces repository error and clears loading`() = runTest {
    // Given
    coEvery { profileRepository.getAllProfiles() } returns emptyList()
    coEvery { bookingRepository.getAllBookings() } throws Exception("Network down")

    // When
    viewModel = MapViewModel(profileRepository, bookingRepository)
    val state = viewModel.uiState.first()

    // Then
    assertTrue(state.errorMessage?.contains("Network down") == true)
    assertFalse(state.isLoading)
    assertTrue(state.bookingPins.isEmpty())
  }
}
