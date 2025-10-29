package com.android.sample.model.map

import android.util.Log
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

open class NominatimLocationRepository(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://nominatim.openstreetmap.org",
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocationRepository {
  fun parseBody(body: String): List<Location> {

    val jsonArray = JSONArray(body)

    return List(jsonArray.length()) { i ->
      val jsonObject = jsonArray.getJSONObject(i)
      val lat = jsonObject.getDouble("lat")
      val lon = jsonObject.getDouble("lon")
      val name = jsonObject.getString("name")
      Location(latitude = lat, longitude = lon, name = name)
    }
  }

  override suspend fun search(query: String): List<Location> =
      withContext(ioDispatcher) {
        val url =
            baseUrl
                .toHttpUrlOrNull()!!
                .newBuilder()
                .addPathSegment("search")
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .build()

        // Create the request with a custom User-Agent and optional Referer
        val request =
            Request.Builder()
                .url(url)
                .header("User-Agent", "SkillBridgeee") // Set a proper User-Agent
                .build()

        try {
          val response = client.newCall(request).execute()
          response.use {
            if (!response.isSuccessful) {
              Log.d("NominatimLocationRepository", "Unexpected code $response")
              throw Exception("Unexpected code $response")
            }

            val body = response.body?.string()

            return@withContext body?.let { parseBody(it) } ?: emptyList()
          }
        } catch (e: IOException) {
          Log.e("NominatimLocationRepository", "Failed to execute request", e)
          throw e
        }
      }
}
