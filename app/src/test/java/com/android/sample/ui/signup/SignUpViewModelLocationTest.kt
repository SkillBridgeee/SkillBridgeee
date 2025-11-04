package com.android.sample.ui.signup

import com.android.sample.model.authentication.AuthenticationRepository
import com.android.sample.model.map.Location
import com.android.sample.model.map.LocationRepository
import com.android.sample.model.user.ProfileRepository
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignUpViewModelLocationTest {

  private val dispatcher = StandardTestDispatcher()
  private lateinit var mockAuthRepository: AuthenticationRepository
  private lateinit var mockProfileRepository: ProfileRepository
  private lateinit var mockLocationRepository: LocationRepository
  private lateinit var signUpUseCase: SignUpUseCase

  private val testLocations =
      listOf(
          Location(latitude = 46.5196535, longitude = 6.6322734, name = "Lausanne"),
          Location(latitude = 46.2043907, longitude = 6.1431577, name = "Geneva"))

  @Before
  fun setup() {
    Dispatchers.setMain(dispatcher)

    mockAuthRepository = mockk {
      every { getCurrentUser() } returns null
      every { signOut() } returns Unit
    }

    mockProfileRepository = mockk(relaxed = true) { coEvery { addProfile(any()) } returns Unit }

    mockLocationRepository = mockk { coEvery { search(any()) } returns testLocations }

    signUpUseCase = SignUpUseCase(mockAuthRepository, mockProfileRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun `locationQuery change updates state`() = runTest {
    // Given
    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)

    // When
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("Lausanne"))
    advanceUntilIdle()

    // Then
    assertEquals("Lausanne", viewModel.state.value.locationQuery)
    assertEquals("Lausanne", viewModel.state.value.address) // Address should also be updated
  }

  @Test
  fun `location search triggers after debounce delay`() = runTest {
    // Given
    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)

    // When
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("Swiss"))

    // Before debounce - no results yet
    assertEquals(0, viewModel.state.value.locationSuggestions.size)

    // After debounce (1 second)
    advanceTimeBy(1100)
    advanceUntilIdle()

    // Then
    assertEquals(2, viewModel.state.value.locationSuggestions.size)
    assertEquals("Lausanne", viewModel.state.value.locationSuggestions[0].name)
  }

  @Test
  fun `empty query clears suggestions and selected location`() = runTest {
    // Given
    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("Test"))
    advanceTimeBy(1100)
    advanceUntilIdle()

    // When
    viewModel.onEvent(SignUpEvent.LocationQueryChanged(""))
    advanceUntilIdle()

    // Then
    assertEquals("", viewModel.state.value.locationQuery)
    assertTrue(viewModel.state.value.locationSuggestions.isEmpty())
    assertNull(viewModel.state.value.selectedLocation)
  }

  @Test
  fun `location selection updates state with location details`() = runTest {
    // Given
    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)
    val location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "Lausanne")

    // When
    viewModel.onEvent(SignUpEvent.LocationSelected(location))

    // Then
    assertEquals("Lausanne", viewModel.state.value.locationQuery)
    assertEquals("Lausanne", viewModel.state.value.address)
    assertNotNull(viewModel.state.value.selectedLocation)
    assertEquals(46.5196535, viewModel.state.value.selectedLocation?.latitude ?: 0.0, 0.0001)
    assertEquals(6.6322734, viewModel.state.value.selectedLocation?.longitude ?: 0.0, 0.0001)
  }

  @Test
  fun `location search handles repository error gracefully`() = runTest {
    // Given
    coEvery { mockLocationRepository.search(any()) } throws Exception("Network error")
    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)

    // When
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("Test"))
    advanceTimeBy(1100)
    advanceUntilIdle()

    // Then - should not crash, suggestions should be empty
    assertTrue(viewModel.state.value.locationSuggestions.isEmpty())
  }

  @Test
  fun `rapid location query changes cancel previous searches`() = runTest {
    // Given
    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)

    // When - rapid typing
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("L"))
    advanceTimeBy(500)
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("La"))
    advanceTimeBy(500)
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("Lau"))
    advanceTimeBy(1100) // Only the last one should trigger
    advanceUntilIdle()

    // Then - query should be "Lau" with results
    assertEquals("Lau", viewModel.state.value.locationQuery)
    assertEquals(2, viewModel.state.value.locationSuggestions.size)
  }

  @Test
  fun `address field is populated when typing location`() = runTest {
    // Given
    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)

    // When
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("EPFL"))

    // Then
    assertEquals("EPFL", viewModel.state.value.address)
  }

  @Test
  fun `selected location is included in signup request`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser> { every { uid } returns "test-uid" }
    every { mockAuthRepository.getCurrentUser() } returns null
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns Result.success(mockUser)

    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)

    val location = Location(latitude = 46.5196535, longitude = 6.6322734, name = "Lausanne")

    // When
    viewModel.onEvent(SignUpEvent.NameChanged("John"))
    viewModel.onEvent(SignUpEvent.SurnameChanged("Doe"))
    viewModel.onEvent(SignUpEvent.LocationSelected(location))
    viewModel.onEvent(SignUpEvent.LevelOfEducationChanged("CS, 3rd"))
    viewModel.onEvent(SignUpEvent.DescriptionChanged("Test"))
    viewModel.onEvent(SignUpEvent.EmailChanged("john@test.com"))
    viewModel.onEvent(SignUpEvent.PasswordChanged("ValidPass123!"))
    viewModel.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    // Then - verify profile was created with location
    io.mockk.coVerify {
      mockProfileRepository.addProfile(
          match { profile ->
            profile.location.name == "Lausanne" &&
                profile.location.latitude == 46.5196535 &&
                profile.location.longitude == 6.6322734
          })
    }
  }

  @Test
  fun `submit without location selection uses address as location name`() = runTest {
    // Given
    val mockUser = mockk<FirebaseUser> { every { uid } returns "test-uid" }
    every { mockAuthRepository.getCurrentUser() } returns null
    coEvery { mockAuthRepository.signUpWithEmail(any(), any()) } returns Result.success(mockUser)

    val viewModel =
        SignUpViewModel(
            initialEmail = null,
            authRepository = mockAuthRepository,
            signUpUseCase = signUpUseCase,
            locationRepository = mockLocationRepository)

    // When - typing location but not selecting
    viewModel.onEvent(SignUpEvent.NameChanged("John"))
    viewModel.onEvent(SignUpEvent.SurnameChanged("Doe"))
    viewModel.onEvent(SignUpEvent.LocationQueryChanged("Some address"))
    viewModel.onEvent(SignUpEvent.LevelOfEducationChanged("CS, 3rd"))
    viewModel.onEvent(SignUpEvent.DescriptionChanged("Test"))
    viewModel.onEvent(SignUpEvent.EmailChanged("john@test.com"))
    viewModel.onEvent(SignUpEvent.PasswordChanged("ValidPass123!"))
    viewModel.onEvent(SignUpEvent.Submit)
    advanceUntilIdle()

    // Then - verify profile was created with location from address
    io.mockk.coVerify {
      mockProfileRepository.addProfile(
          match { profile ->
            profile.location.name == "Some address" &&
                profile.location.latitude == 0.0 &&
                profile.location.longitude == 0.0
          })
    }
  }
}
