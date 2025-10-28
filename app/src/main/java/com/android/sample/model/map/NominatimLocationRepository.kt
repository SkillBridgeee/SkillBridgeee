package com.android.sample.model.map

import android.util.Log
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

open class NominatimLocationRepository(
    private val client: OkHttpClient,
    private val baseUrl: String = "https://nominatim.openstreetmap.org"
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
    //      try {
    //          val jsonArray = JSONArray(body)
    //          Log.d("Debug", "JSONArray parsed successfully: ${jsonArray.length()} elements")
    //          return List(jsonArray.length()) { i ->
    //              val obj = jsonArray.getJSONObject(i)
    //              Location(
    //                  latitude = obj.getDouble("lat"),
    //                  longitude = obj.getDouble("lon"),
    //                  name = obj.getString("display_name")
    //              )
    //          }
    //      } catch (e: Exception) {
    //          Log.e("Debug", "JSONException: ${e.message}")
    //          throw e
    //      }

  }

  override suspend fun search(query: String): List<Location> =
      withContext(Dispatchers.IO) {
        // Using HttpUrl.Builder to properly construct the URL with query parameters.

        // TODO mettre une exception si ça plante
        //          val base = baseUrl.toHttpUrlOrNull()!!
        //        val url =
        //            HttpUrl.Builder()
        //                .scheme(baseUrl.toHttpUrlOrNull()!!.scheme)
        //                .host(baseUrl.toHttpUrlOrNull()!!.host)
        //                .port(base.port)
        //                .addPathSegment("search")
        //                .addQueryParameter("q", query)
        //                .addQueryParameter("format", "json")
        //                .build()

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
                .header(
                    "User-Agent",
                    // TODO email mettre une autre address je pense
                    "SkillBridgeee") // Set a proper User-Agent
                // TODO trouver un referer à mettre et un site ou une ref (lien github?)
                .build()

        try {
          val response = client.newCall(request).execute()
          response.use {
            if (!response.isSuccessful) {
              Log.d("NominatimLocationRepository", "Unexpected code $response")
              throw Exception("Unexpected code $response")
            }

            val body = response.body?.string()
            if (body != null) {
              Log.d("NominatimLocationRepository", "Body: $body")
              return@withContext parseBody(body)
            } else {
              Log.d("NominatimLocationRepository", "Empty body")
              return@withContext emptyList()
            }
          }
        } catch (e: IOException) {
          Log.e("NominatimLocationRepository", "Failed to execute request", e)
          throw e
        }
      }
}
