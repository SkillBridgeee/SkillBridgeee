package com.android.sample.model.map

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class GpsLocationProviderTest {

  @Test
  fun `getCurrentLocation returns last known location when available`() = runBlocking {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)

    val last =
        Location(LocationManager.GPS_PROVIDER).apply {
          latitude = 12.34
          longitude = 56.78
        }
    `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(last)

    val provider = GpsLocationProvider(context)
    val result = withTimeout(1000L) { provider.getCurrentLocation(1000L) }
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

    // When requestLocationUpdates is called, immediately invoke the supplied listener with a
    // Location.
    doAnswer { invocation ->
          val listener = invocation.arguments[3] as LocationListener
          val loc =
              Location(LocationManager.GPS_PROVIDER).apply {
                latitude = -1.23
                longitude = 4.56
              }
          listener.onLocationChanged(loc)
          null
        }
        .`when`(lm)
        .requestLocationUpdates(
            eq(LocationManager.GPS_PROVIDER),
            anyLong(),
            anyFloat(),
            any(LocationListener::class.java))

    val provider = GpsLocationProvider(context)
    val result = withTimeout(1000L) { provider.getCurrentLocation(1000L) }
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

    doThrow(SecurityException::class.java)
        .`when`(lm)
        .requestLocationUpdates(
            eq(LocationManager.GPS_PROVIDER),
            anyLong(),
            anyFloat(),
            any(LocationListener::class.java))

    val provider = GpsLocationProvider(context)
    try {
      runBlocking { withTimeout(1000L) { provider.getCurrentLocation(1000L) } }
      fail("Expected SecurityException to be thrown")
    } catch (se: SecurityException) {
      // expected
    }
  }

  @Test
  fun `getCurrentLocation returns null when getLastKnownLocation throws SecurityException`() =
      runBlocking {
        val context = mock(Context::class.java)
        val lm = mock(LocationManager::class.java)
        `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
        `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER))
            .thenThrow(SecurityException::class.java)

        val provider = GpsLocationProvider(context)
        val result = withTimeout(1000L) { provider.getCurrentLocation(1000L) }
        assertNull(result)
        // ensure requestLocationUpdates was not attempted
        verify(lm, never())
            .requestLocationUpdates(
                anyString(), anyLong(), anyFloat(), any(LocationListener::class.java))
      }

  @Test
  fun `getCurrentLocation continues when getLastKnownLocation throws nonSecurityException`() =
      runBlocking {
        val context = mock(Context::class.java)
        val lm = mock(LocationManager::class.java)
        `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
        // Throw a generic exception from getLastKnownLocation; provider should continue to request
        // updates
        `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER))
            .thenThrow(IllegalStateException::class.java)

        doAnswer { invocation ->
              val listener = invocation.arguments[3] as LocationListener
              val loc =
                  Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = 7.89
                    longitude = 1.23
                  }
              listener.onLocationChanged(loc)
              null
            }
            .`when`(lm)
            .requestLocationUpdates(
                eq(LocationManager.GPS_PROVIDER),
                anyLong(),
                anyFloat(),
                any(LocationListener::class.java))

        val provider = GpsLocationProvider(context)
        val result = withTimeout(1000L) { provider.getCurrentLocation(1000L) }
        assertNotNull(result)
        assertEquals(7.89, result!!.latitude, 0.0001)
        assertEquals(1.23, result.longitude, 0.0001)
      }

  @Test
  fun `getCurrentLocation propagates nonSecurityException from requestLocationUpdates`() {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(null)

    doThrow(RuntimeException::class.java)
        .`when`(lm)
        .requestLocationUpdates(
            eq(LocationManager.GPS_PROVIDER),
            anyLong(),
            anyFloat(),
            any(LocationListener::class.java))

    val provider = GpsLocationProvider(context)
    try {
      runBlocking { withTimeout(1000L) { provider.getCurrentLocation(1000L) } }
      fail("Expected RuntimeException to be thrown")
    } catch (re: RuntimeException) {
      // expected
    }
  }

  @Test
  fun `getCurrentLocation cancels and removes updates on coroutine cancellation`() = runBlocking {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)).thenReturn(null)

    // Capture the listener but do not call it so we can cancel the coroutine.
    val listenerRef = AtomicReference<LocationListener?>()
    doAnswer { invocation ->
          val listener = invocation.arguments[3] as LocationListener
          listenerRef.set(listener)
          null
        }
        .`when`(lm)
        .requestLocationUpdates(
            eq(LocationManager.GPS_PROVIDER),
            anyLong(),
            anyFloat(),
            any(LocationListener::class.java))

    val provider = GpsLocationProvider(context)

    val job = launch {
      // call provider and suspend until cancellation (use a bounded timeout to avoid CI hangs)
      withTimeout(5000L) { provider.getCurrentLocation(5000L) }
    }

    // Give the provider some time to register the listener
    delay(50)
    // Cancel the caller; provider should invoke removal via invokeOnCancellation
    job.cancel()
    job.join()

    // Verify removal was attempted on cancellation
    verify(lm, atLeastOnce()).removeUpdates(any(LocationListener::class.java))
  }

  @Test
  fun `isLocationEnabled returns true when GPS provider is enabled`() {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
    `when`(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(false)

    val provider = GpsLocationProvider(context)
    assertTrue(provider.isLocationEnabled())
  }

  @Test
  fun `isLocationEnabled returns true when Network provider is enabled`() {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(false)
    `when`(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true)

    val provider = GpsLocationProvider(context)
    assertTrue(provider.isLocationEnabled())
  }

  @Test
  fun `isLocationEnabled returns true when both providers are enabled`() {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true)
    `when`(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(true)

    val provider = GpsLocationProvider(context)
    assertTrue(provider.isLocationEnabled())
  }

  @Test
  fun `isLocationEnabled returns false when both providers are disabled`() {
    val context = mock(Context::class.java)
    val lm = mock(LocationManager::class.java)
    `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(lm)
    `when`(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(false)
    `when`(lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)).thenReturn(false)

    val provider = GpsLocationProvider(context)
    assertFalse(provider.isLocationEnabled())
  }
}
