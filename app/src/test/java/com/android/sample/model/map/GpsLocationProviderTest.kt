package com.android.sample.model.map

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GpsLocationProviderTest {

  @Test
  fun `getCurrentLocation returns last known location when available`() = runBlocking {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)

    val last = Location(LocationManager.GPS_PROVIDER).apply {
      latitude = 12.34
      longitude = 56.78
    }
    `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(last)

    val provider = GpsLocationProvider(context)
    val result = provider.getCurrentLocation()
    assertNotNull(result)
    assertEquals(12.34, result!!.latitude, 0.0001)
    assertEquals(56.78, result.longitude, 0.0001)
  }

  @Test
  fun `getCurrentLocation waits for listener when last known is null`() = runBlocking {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(null)

    // When requestLocationUpdates is called, immediately invoke the supplied listener with a Location.
    doAnswer { invocation ->
      val listener = invocation.arguments[3] as LocationListener
      val loc = Location(LocationManager.GPS_PROVIDER).apply {
        latitude = -1.23
        longitude = 4.56
      }
      listener.onLocationChanged(loc)
      null
    }.`when`(lm).requestLocationUpdates(
      eq(LocationManager.GPS_PROVIDER),
      anyLong(),
      anyFloat(),
      any(LocationListener::class.java)
    )

    val provider = GpsLocationProvider(context)
    val result = provider.getCurrentLocation()
    assertNotNull(result)
    assertEquals(-1.23, result!!.latitude, 0.0001)
    assertEquals(4.56, result.longitude, 0.0001)
  }

  @Test
  fun `getCurrentLocation throws SecurityException when requestLocationUpdates throws`() {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(null)

    doThrow(SecurityException::class.java).`when`(lm).requestLocationUpdates(
      eq(LocationManager.GPS_PROVIDER),
      anyLong(),
      anyFloat(),
      any(LocationListener::class.java)
    )

    val provider = GpsLocationProvider(context)
    try {
      runBlocking { provider.getCurrentLocation() }
      fail("Expected SecurityException to be thrown")
    } catch (se: SecurityException) {
      // expected
    }
  }

  @Test
  fun `getCurrentLocation returns null when getLastKnownLocation throws SecurityException`() = runBlocking {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenThrow(SecurityException::class.java)

    val provider = GpsLocationProvider(context)
    val result = provider.getCurrentLocation()
    assertNull(result)
    // ensure requestLocationUpdates was not attempted (optional verification)
    verify(lm, never()).requestLocationUpdates(
      anyString(),
      anyLong(),
      anyFloat(),
      any(LocationListener::class.java)
    )
  }
}
