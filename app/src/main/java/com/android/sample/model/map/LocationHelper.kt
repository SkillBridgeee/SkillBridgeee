package com.android.sample.model.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.Locale

/**
 * Result of a GPS location fetch operation. Contains success or failure information.
 *
 * @property location The fetched location on success, null otherwise.
 * @property addressText The human-readable address text for the location.
 * @property errorMessage The error message if the operation failed.
 */
data class GpsLocationResult(
    val location: Location? = null,
    val addressText: String? = null,
    val errorMessage: String? = null
) {
  val isSuccess: Boolean
    get() = location != null && errorMessage == null
}

/** Helper object to handle GPS location fetching and geocoding. */
object LocationHelper {

  const val GPS_FAILED_MSG = "Failed to obtain GPS location"
  const val LOCATION_PERMISSION_DENIED_MSG = "Location permission denied"
  const val LOCATION_DISABLED_MSG =
      "Location services are disabled. Please enable location in your device settings."

  /**
   * Fetches the current GPS location and geocodes it to a human-readable address.
   *
   * This function attempts to retrieve the current GPS location using the provided
   * [GpsLocationProvider]. If successful, it uses a [Geocoder] to convert the latitude and
   * longitude into a human-readable address.
   *
   * @param provider The [GpsLocationProvider] used to obtain the current GPS location.
   * @param context The Android context used for geocoding.
   * @return [GpsLocationResult] containing the location, address, or error message.
   */
  @Suppress("DEPRECATION")
  suspend fun fetchLocationFromGps(
      provider: GpsLocationProvider,
      context: Context
  ): GpsLocationResult {
    return try {
      // Check if location services are enabled
      if (!provider.isLocationEnabled()) {
        return GpsLocationResult(errorMessage = LOCATION_DISABLED_MSG)
      }

      val androidLoc = provider.getCurrentLocation()
      if (androidLoc != null) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> =
            geocoder.getFromLocation(androidLoc.latitude, androidLoc.longitude, 1)?.toList()
                ?: emptyList()

        val addressText =
            if (addresses.isNotEmpty()) {
              val address = addresses[0]
              listOfNotNull(address.locality, address.adminArea, address.countryName)
                  .joinToString(", ")
            } else {
              "${androidLoc.latitude}, ${androidLoc.longitude}"
            }

        val mapLocation =
            Location(
                latitude = androidLoc.latitude,
                longitude = androidLoc.longitude,
                name = addressText)

        GpsLocationResult(location = mapLocation, addressText = addressText)
      } else {
        GpsLocationResult(errorMessage = GPS_FAILED_MSG)
      }
    } catch (_: SecurityException) {
      GpsLocationResult(errorMessage = LOCATION_PERMISSION_DENIED_MSG)
    } catch (_: Exception) {
      GpsLocationResult(errorMessage = GPS_FAILED_MSG)
    }
  }
}
