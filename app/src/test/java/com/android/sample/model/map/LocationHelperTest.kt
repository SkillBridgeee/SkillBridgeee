package com.android.sample.model.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import androidx.test.core.app.ApplicationProvider
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LocationHelperTest {

  private lateinit var context: Context
  private lateinit var mockProvider: GpsLocationProvider

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    mockProvider = mockk<GpsLocationProvider>()
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun fetchLocationFromGps_whenLocationDisabled_returnsErrorResult() = runTest {
    // Arrange
    every { mockProvider.isLocationEnabled() } returns false

    // Act
    val result = LocationHelper.fetchLocationFromGps(mockProvider, context)

    // Assert
    assertFalse(result.isSuccess)
    assertEquals(LocationHelper.LOCATION_DISABLED_MSG, result.errorMessage)
    assertNull(result.location)
    assertNull(result.addressText)
  }

  @Test
  fun fetchLocationFromGps_whenLocationIsNull_returnsErrorResult() = runTest {
    // Arrange
    every { mockProvider.isLocationEnabled() } returns true
    coEvery { mockProvider.getCurrentLocation() } returns null

    // Act
    val result = LocationHelper.fetchLocationFromGps(mockProvider, context)

    // Assert
    assertFalse(result.isSuccess)
    assertEquals(LocationHelper.GPS_FAILED_MSG, result.errorMessage)
    assertNull(result.location)
    assertNull(result.addressText)
  }

  @Test
  fun fetchLocationFromGps_whenSecurityException_returnsPermissionDeniedError() = runTest {
    // Arrange
    every { mockProvider.isLocationEnabled() } returns true
    coEvery { mockProvider.getCurrentLocation() } throws SecurityException("Permission denied")

    // Act
    val result = LocationHelper.fetchLocationFromGps(mockProvider, context)

    // Assert
    assertFalse(result.isSuccess)
    assertEquals(LocationHelper.LOCATION_PERMISSION_DENIED_MSG, result.errorMessage)
    assertNull(result.location)
    assertNull(result.addressText)
  }

  @Test
  fun fetchLocationFromGps_withValidLocationNoAddress_returnsCoordinatesAsAddress() = runTest {
    // Arrange
    val mockLocation = mockk<android.location.Location>()
    every { mockLocation.latitude } returns 48.8566
    every { mockLocation.longitude } returns 2.3522

    every { mockProvider.isLocationEnabled() } returns true
    coEvery { mockProvider.getCurrentLocation() } returns mockLocation

    mockkConstructor(Geocoder::class)
    @Suppress("DEPRECATION")
    every { anyConstructed<Geocoder>().getFromLocation(any(), any(), any()) } returns emptyList()

    // Act
    val result = LocationHelper.fetchLocationFromGps(mockProvider, context)

    // Assert
    assertTrue(result.isSuccess)
    val location = requireNotNull(result.location)
    assertEquals(48.8566, location.latitude, 0.0001)
    assertEquals(2.3522, location.longitude, 0.0001)
    assertEquals("48.8566, 2.3522", result.addressText)
    assertNull(result.errorMessage)
  }

  @Test
  fun fetchLocationFromGps_withValidLocationAndAddress_returnsFormattedAddress() = runTest {
    // Arrange
    val mockLocation = mockk<android.location.Location>()
    every { mockLocation.latitude } returns 48.8566
    every { mockLocation.longitude } returns 2.3522

    val mockAddress = mockk<Address>()
    every { mockAddress.locality } returns "Paris"
    every { mockAddress.adminArea } returns "Île-de-France"
    every { mockAddress.countryName } returns "France"

    every { mockProvider.isLocationEnabled() } returns true
    coEvery { mockProvider.getCurrentLocation() } returns mockLocation

    mockkConstructor(Geocoder::class)
    @Suppress("DEPRECATION")
    every { anyConstructed<Geocoder>().getFromLocation(any(), any(), any()) } returns
        listOf(mockAddress)

    // Act
    val result = LocationHelper.fetchLocationFromGps(mockProvider, context)

    // Assert
    assertTrue(result.isSuccess)
    val location = requireNotNull(result.location)
    assertEquals(48.8566, location.latitude, 0.0001)
    assertEquals(2.3522, location.longitude, 0.0001)
    assertEquals("Paris, Île-de-France, France", result.addressText)
    assertEquals("Paris, Île-de-France, France", location.name)
    assertNull(result.errorMessage)
  }

  @Test
  fun fetchLocationFromGps_withPartialAddress_returnsOnlyAvailableParts() = runTest {
    // Arrange
    val mockLocation = mockk<android.location.Location>()
    every { mockLocation.latitude } returns 40.7128
    every { mockLocation.longitude } returns -74.0060

    val mockAddress = mockk<Address>()
    every { mockAddress.locality } returns "New York"
    every { mockAddress.adminArea } returns null
    every { mockAddress.countryName } returns "USA"

    every { mockProvider.isLocationEnabled() } returns true
    coEvery { mockProvider.getCurrentLocation() } returns mockLocation

    mockkConstructor(Geocoder::class)
    @Suppress("DEPRECATION")
    every { anyConstructed<Geocoder>().getFromLocation(any(), any(), any()) } returns
        listOf(mockAddress)

    // Act
    val result = LocationHelper.fetchLocationFromGps(mockProvider, context)

    // Assert
    assertTrue(result.isSuccess)
    requireNotNull(result.location)
    assertEquals("New York, USA", result.addressText)
    assertNull(result.errorMessage)
  }

  @Test
  fun fetchLocationFromGps_whenGenericException_returnsGpsFailedError() = runTest {
    // Arrange
    every { mockProvider.isLocationEnabled() } returns true
    coEvery { mockProvider.getCurrentLocation() } throws RuntimeException("Unexpected error")

    // Act
    val result = LocationHelper.fetchLocationFromGps(mockProvider, context)

    // Assert
    assertFalse(result.isSuccess)
    assertEquals(LocationHelper.GPS_FAILED_MSG, result.errorMessage)
    assertNull(result.location)
    assertNull(result.addressText)
  }

  @Test
  fun gpsLocationResult_isSuccess_returnsTrueWhenLocationPresent() {
    // Arrange
    val location = Location(latitude = 1.0, longitude = 2.0, name = "Test")
    val result = GpsLocationResult(location = location, addressText = "Test")

    // Assert
    assertTrue(result.isSuccess)
  }

  @Test
  fun gpsLocationResult_isSuccess_returnsFalseWhenLocationNull() {
    // Arrange
    val result = GpsLocationResult(location = null, errorMessage = "Error")

    // Assert
    assertFalse(result.isSuccess)
  }

  @Test
  fun gpsLocationResult_isSuccess_returnsFalseWhenErrorPresent() {
    // Arrange
    val location = Location(latitude = 1.0, longitude = 2.0, name = "Test")
    val result = GpsLocationResult(location = location, errorMessage = "Error")

    // Assert
    assertFalse(result.isSuccess)
  }
}
