package com.android.sample.model.newListing

import android.content.Context
import android.location.Location as AndroidLocation
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.sample.mockRepository.listingRepo.ListingFakeRepoEmpty
import com.android.sample.model.listing.ListingRepositoryProvider
import com.android.sample.model.map.GpsLocationProvider
import com.android.sample.ui.newListing.NewListingViewModel
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
class NewListingViewModelLocationRobolectricTest {

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

    ListingRepositoryProvider.setForTests(ListingFakeRepoEmpty())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun fetchLocationFromGps_sets_selectedLocation_and_locationQuery() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = NewListingViewModel()

    val mockProvider = mockk<GpsLocationProvider>()
    val androidLoc =
        AndroidLocation("test").apply {
          latitude = 48.8566
          longitude = 2.3522
        }

    coEvery { mockProvider.getCurrentLocation() } returns androidLoc

    vm.fetchLocationFromGps(mockProvider, context)
    advanceUntilIdle()

    val s = vm.uiState.value
    assertNotNull(s.selectedLocation)
    assertEquals(s.selectedLocation!!.name, s.locationQuery)
  }

  @Test
  fun onLocationPermissionDenied_sets_error_message() = runTest {
    val vm = NewListingViewModel()
    vm.onLocationPermissionDenied()
    assertNotNull(vm.uiState.value.invalidLocationMsg)
  }
}
