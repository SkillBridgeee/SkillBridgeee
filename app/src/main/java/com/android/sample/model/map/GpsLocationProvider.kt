package com.android.sample.model.map

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout

/**
 * Attempt to get a GPS fix. First tries lastKnownLocation, otherwise requests updates until the
 * first fix arrives.
 *
 * Notes:
 * - The [timeoutMs] parameter is honored: the call will throw a
 *   [kotlinx.coroutines.TimeoutCancellationException] if no location arrives within the timeout.
 * - If `getLastKnownLocation` throws a [SecurityException] the function resumes with `null`
 *   (best-effort, treating absence of a last known fix as no-location). In contrast, if
 *   `requestLocationUpdates` throws a [SecurityException] the coroutine resumes with that
 *   exception. This asymmetry is intentional (tests rely on differentiating "no last-known
 *   location" from an actual permission failure).
 */
open class GpsLocationProvider(private val context: Context) {
  open suspend fun getCurrentLocation(timeoutMs: Long = 10_000): Location? =
      withTimeout(timeoutMs) {
        suspendCancellableCoroutine { cont ->
          val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

          // Try last known
          try {
            val last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (last != null) {
              cont.resume(last)
              return@suspendCancellableCoroutine
            }
          } catch (_: SecurityException) {
            // Best-effort: no last-known location available due to missing permission.
            cont.resume(null)
            return@suspendCancellableCoroutine
          } catch (_: Exception) {
            // continue to request updates
          }

          val listener =
              object : LocationListener {
                override fun onLocationChanged(location: Location) {
                  if (cont.isActive) {
                    cont.resume(location)
                    try {
                      lm.removeUpdates(this)
                    } catch (_: Exception) {}
                  }
                }

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {}

                @Suppress("DEPRECATION")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
              }

          try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener)
          } catch (e: SecurityException) {
            // Permission failure while requesting updates: propagate as an exception.
            cont.resumeWithException(e)
            return@suspendCancellableCoroutine
          } catch (e: Exception) {
            cont.resumeWithException(e)
            return@suspendCancellableCoroutine
          }

          cont.invokeOnCancellation {
            try {
              lm.removeUpdates(listener)
            } catch (_: Exception) {}
          }
        }
      }
}
