package com.android.sample.model.signUp

import android.content.Context
import android.location.Location as AndroidLocation
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.model.user.FakeProfileRepository
import com.android.sample.model.user.ProfileRepositoryProvider
import com.android.sample.ui.signup.SignUpViewModel
import com.google.firebase.FirebaseApp
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class SignUpViewModelLocationRobolectricTest {

  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()

    try {
      FirebaseApp.clearInstancesForTest()
    } catch (_: Exception) {}
    try {
      FirebaseApp.initializeApp(context)
    } catch (_: IllegalStateException) {}

    ProfileRepositoryProvider.setForTests(FakeProfileRepository())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun fetchLocationFromGps_sets_selectedLocation_and_address() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = SignUpViewModel()

    val mockProvider = mockk<GpsLocationProvider>()
    io.mockk.every { mockProvider.isLocationEnabled() } returns true
    val androidLoc =
        AndroidLocation("test").apply {
          latitude = 48.8566
          longitude = 2.3522
        }
    coEvery { mockProvider.getCurrentLocation() } returns androidLoc

    // Act
    vm.fetchLocationFromGps(mockProvider, context)
    advanceUntilIdle()

    // Assert
    val s = vm.state.value
    assertNotNull(s.selectedLocation)
    assertEquals(s.selectedLocation!!.name, s.locationQuery)
    assertEquals(s.selectedLocation!!.name, s.address)
  }

  @Test
  fun onLocationPermissionDenied_sets_error_message() = runTest {
    val vm = SignUpViewModel()
    vm.onLocationPermissionDenied()
    assertNotNull(vm.state.value.error)
  }

  @Test
  fun fetchLocationFromGps_when_location_disabled_sets_error_message() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = SignUpViewModel()

    val mockProvider = mockk<GpsLocationProvider>()
    io.mockk.every { mockProvider.isLocationEnabled() } returns false

    vm.fetchLocationFromGps(mockProvider, context)
    advanceUntilIdle()

    val s = vm.state.value
    assertNotNull(s.error)
    assertEquals(
        "Location services are disabled. Please enable location in your device settings.", s.error)
  }
}
